package edu.umbc.bft.router.main;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.xml.DOMConfigurator;

import edu.umbc.bft.beans.crypto.Cipher;
import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.header.Header;
import edu.umbc.bft.beans.net.payload.FaultPayload;
import edu.umbc.bft.beans.net.route.Route;
import edu.umbc.bft.beans.timer.FaultManager;
import edu.umbc.bft.exceptions.InvalidClassInstanceException;
import edu.umbc.bft.exceptions.KeyNotFoundException;
import edu.umbc.bft.router.engine.MessageEngine;
import edu.umbc.bft.router.extras.KeyManager;
import edu.umbc.bft.router.extras.MessageInitiator;
import edu.umbc.bft.router.extras.NodeDetail;
import edu.umbc.bft.router.extras.RouteDiscovery;
import edu.umbc.bft.router.extras.TopologyManager;
import edu.umbc.bft.util.Logger;

public class Router {
	
	public static String serverIP;
	public static int nodeID, maxHops;
	private static Destructor destroyer;
	private static Properties configMap;
	private static KeyManager keyManager;
	private static RouteDiscovery routeFinder;
	private static MessageInitiator initiator;
	private static TopologyManager topologyManager;
	private static Map<String, FaultManager> timers;
	
	/** IP->Node & ID->Node */
	private static Map<String, NodeDetail> nodes;
	/** SequenceNumber -> Faults */
	private static Map<Long, Set<Datagram>> faultAnnouncements;
	
	
	static	{
		Router.maxHops = 8;
		Router.configMap = new Properties();
		Router.timers = new ConcurrentHashMap<String, FaultManager>();
		Router.faultAnnouncements = new ConcurrentHashMap<Long, Set<Datagram>>();
	}
	
	public static void main(String[] args) {
		
		Router.configureLOG4J();
		Router.addDestructor();
		Router.load();
		
		int port = Router.getPropertyAsInteger("server.port");
		
		Router.topologyManager = new TopologyManager();
		
		try {
			
			PortListener l = new PortListener(port);
			MessageEngine e = new MessageEngine();
			
			Router.initiator = new MessageInitiator(l);
			Router.routeFinder = new RouteDiscovery(Router.topologyManager);
			Router.initiator.setDicovery(Router.routeFinder);
			Router.destroyer.setListener(l);
			Router.destroyer.setEngine(e);
			
			e.setListener(l);
			l.setEngine(e);
			
			e.start();
			l.start();
			Router.initiator.start();
			
			
		}catch(SocketException e) {
			Logger.error(Router.class, e);
		}
		
		System.out.println(" ---Application started--- ");
		
	}//end of main
	
	
	public static void load() {
		Router.loadProperties();
		Router.fetchNodes();
		
		Router.serverIP = Router.getProperty("server.ip").trim();
		Router.nodeID = Router.getPropertyAsInteger("server.id");
		int total = Router.getPropertyAsInteger("total.nodes") * 2;
		Router.maxHops = total>maxHops?total:maxHops;

		Router.keyManager = new KeyManager();
	}//end of method
	
	
	private static void configureLOG4J()		{

		File log4j = new File("./resource/log4j.xml");

		if( log4j.exists() )			{
			DOMConfigurator.configure("./resource/log4j.xml");
		}else			{
			try		{
				int bit=0;
				InputStream is = Router.class.getClassLoader().getResourceAsStream("log4j.xml");

				String path = System.getProperty("user.dir");
				File tempLog4j = new File(path, "resource");
				tempLog4j.mkdirs();
				tempLog4j.setReadable(true, false);
				tempLog4j.setWritable(true, false);

				path += (File.separator +"resource"+ File.separator);
				tempLog4j = new File(path, "log4j.xml");
				tempLog4j.createNewFile();

				FileOutputStream fos = new FileOutputStream(tempLog4j);
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				do		{
					bit = is.read();
					if( bit == -1 )		{
						break;
					}
					bos.write(bit);
				}while( bit != -1 );

				is.close();
				bos.close();
				DOMConfigurator.configure("./resource/log4j.xml");

			}catch(Exception e)	{
				System.err.println(" ERROR - Initalizing LOG4J configuration!! ==> "+ e.getMessage());
			}//End Of Try Catch
		}//End Of IF Else		

	}//end of method


	private static void addDestructor()			{

		Router.destroyer = new Destructor();
		Runtime.getRuntime().addShutdownHook(destroyer);

	}//end of method

	
	public static void loadProperties()		{
		try			{

			File config = new File("./resource/config.properties");
			BufferedInputStream bis = null;

			if( config.exists() == false )			{
				System.err.println("Config file NOT found..!");
				InputStream is = Router.class.getClassLoader().getResourceAsStream("config.properties");
				bis = new BufferedInputStream(is);
			}else	{
				bis = new BufferedInputStream(new FileInputStream(config));
			}
		
			Router.configMap.clear();
			Router.configMap.load(bis);
			bis.close();

		}catch(Exception e)		{
			Logger.error(Router.class, " ERROR - Loading config.properties :: "+ e.getMessage());
		}//End Of Try Catch
		
	}//end of method
	
	
	public static Set<Object> getAllProperties()		{
		return Router.configMap.keySet();
	}
	
	public static String getProperty(String key)		{
		if( key!=null )			{
			return Router.configMap.getProperty(key.trim());
		}
		return null;
	}//end of method
	
	
	public static Integer getPropertyAsInteger(String key)		{
		if( key!=null )			{
			try		{
				return Integer.parseInt(Router.configMap.getProperty(key.trim()));
			}catch(Exception e){
				Logger.error(Router.class, " Unexpected Value for Property = "+ key);
				return 0;
			}
		}
		return null;
	}//end of method
	
	
	public static int getNextSequenceNumber()	{
		return Router.initiator.getNextSequenceNumber();
	}//end of method
	
	
	public static void fetchNodes()	{
		
		Router.nodes = new HashMap<String, NodeDetail>(5);
		int total = Router.getPropertyAsInteger("total.nodes");

		for( int i=1; i<=total; i++ )	{
			
			String ip = Router.getProperty("node."+i+".ip").trim();
			int port = Router.getPropertyAsInteger("node."+i+".port");
			
			if( ip!=null && ip.length()>0 && port>0 )	{
				NodeDetail nd = new NodeDetail(i, ip, port);
				nodes.put(ip, nd);
				nodes.put(String.valueOf(i), nd);
			}else	{
				Logger.error(Router.class, " Invalid server ip/port found ");
			}
			
		}//end of loop
		
	}//end of method
	
	
	public static int getNodePort(String ip) {
		if( ip!=null && Router.nodes.containsKey(ip) )	{
			return Router.nodes.get(ip).getPort();
		}else	{
			return -1;
		}
	}//end of method
	
	public static int getNodeID(String ip) {
		if( ip!=null && Router.nodes.containsKey(ip) )	{
			return Router.nodes.get(ip).getId();
		}else	{
			return -1;
		}
	}//end of method
	
	public static String getNodeIP(int nodeid) {
		String id = String.valueOf(nodeid);
		if( Router.nodes.containsKey(id) )	{
			return Router.nodes.get(id).getIp();
		}else	{
			return null;
		}
	}//end of method
	
	public static NodeDetail getNodeDetails(String id) {
		return id!=null&&Router.nodes.containsKey(id)?Router.nodes.get(id):null;
	}

	public static Set<String> getNodes() {
		return Router.nodes.keySet();
	}//end of method

	public static Cipher getMyAsymmetricKey() {
		return Router.keyManager.getMyAsymmetricCipher();
	}//end of method
	
	public static String getSymmetricKeyFor(String serverip)	{
		try {
			return Router.keyManager.getSymmetricKeyFor(serverip);
		}catch(KeyNotFoundException e) {
			Logger.error(Router.class, e.getMessage() );
			return null;
		}
	}//end of method
	
	public static String getPublicKeyOf(String serverip)	{
		try {
			Logger.info(Router.class, " Getting public key of "+ serverip);
			return Router.keyManager.getPublicKeyOf(serverip.trim());
		}catch(KeyNotFoundException e) {
			Logger.error(Router.class, e.getMessage() );
			return null;
		}
	}//end of method
	
	
	public static Route findRoute(Header h) {
		Route r = Router.routeFinder.find(h.getSource(), h.getDestination());
		r.setSource(Router.serverIP);
		h.setRoute(r);
		return r;
	}//end of method
	
	
	public static boolean startAckTimer(NetworkInterface inf, Datagram d) {
		
		String dest = d.getHeader().getDestination();
		
		if( dest!=null && Router.timers.containsKey(dest)==false )	{
			FaultManager manager = new FaultManager();
			Router.timers.put(dest, manager);
		}
		
		FaultManager manager = Router.timers.get(dest);
		
		try	{
			if( manager.startAckTimer(inf, d) != null )
				return true;
		}catch(InvalidClassInstanceException e) {
			Logger.error(Router.class, " Unable to start timer : \n"+ Logger.getStack(e) );
		}
		return false;
	}//end of method
	
	public static boolean cancelAckTimer(Datagram d) {
		
		String dest = d.getHeader().getSource();
		
		if( dest!=null && Router.timers.containsKey(dest) )	{

			FaultManager manager = Router.timers.get(dest);
			return manager.cancelAckTimer(d);
			
		}else	{
			Logger.error(Router.class, " No Timer Manager found for the ACK ");
		}
		
		return false;
		
	}//end of method
	
	
	public static boolean addFaultAnnouncement(Datagram d) {
		
		if( d!=null && d.getPayload() instanceof FaultPayload )	{
			
			long seq = d.getHeader().getSequenceNumber();
			
			if( Router.faultAnnouncements.containsKey(seq) )	{
				Set<Datagram> set = Router.faultAnnouncements.get(seq);
				set.add(d);
			}else	{
				Set<Datagram> set = new HashSet<Datagram>();
				Router.faultAnnouncements.put(seq, set);
				set.add(d);
			}
			return true;
		}else	{
			Logger.error(Router.class, " UnExpected Payload type to store F.A. ");
		}
		return false;
	}//end of method
	
	
	public static Set<Datagram> getFaultAnnouncements(Datagram d) {
		
		if( d!=null && d.getPayload() instanceof FaultPayload )	{
			
			long seq = d.getHeader().getSequenceNumber();			
			return Router.faultAnnouncements.get(seq);
			
		}else	{
			Logger.error(Router.class, " UnExpected Payload type to store F.A. ");
		}
		return null;
	}//end of method
	

	public static void clearFaultAnnouncements(long key) 	{
		
		Set<Datagram> set = Router.faultAnnouncements.get(key);
		
		if( set!=null && set.size()>0 )
			set.clear();
		
	}//end of method
	

	public static TopologyManager getTopologyManager() {
		return Router.topologyManager;
	}
	
}