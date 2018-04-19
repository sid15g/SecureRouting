package edu.umbc.bft.router.main;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Hex;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import edu.umbc.bft.beans.gson.SerializingAdapter;
import edu.umbc.bft.beans.net.Datagram;
import edu.umbc.bft.beans.net.Packet;
import edu.umbc.bft.beans.net.header.Header;
import edu.umbc.bft.beans.net.payload.Payload;
import edu.umbc.bft.util.Logger;

public class DatagramFactory {

	private static final Charset charset = StandardCharsets.ISO_8859_1;
	private static final Hex hexcoder = new Hex(charset);
	private static Gson gson;
	
	static 	{
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(Header.class, new SerializingAdapter<Header>());
		builder.registerTypeAdapter(Payload.class, new SerializingAdapter<Payload>());
		gson = builder.create();
	}
	
	public static Gson getGsonWithAdapter()	{
		return gson;
	}
	
	public static DatagramPacket createDatagramToSend(String serverIP, int serverPort, Datagram message) {
		String json = DatagramFactory.gson.toJson(message);
		return DatagramFactory.createDatagramToSend(serverIP, serverPort, json);
	}//end of method
	
	public static DatagramPacket createDatagramToSend(String serverIP, int serverPort, String message) {
		try {
			InetAddress ip = InetAddress.getByName(serverIP);
			byte[] arr = message.trim().getBytes();
			return new DatagramPacket(arr, arr.length, ip, serverPort);
		}catch(UnknownHostException e) {
			Logger.error(DatagramFactory.class, e);
		}
		return null;
	}//end of method
	
	public static DatagramPacket createEmptyDatagram() {
		return DatagramFactory.createEmptyDatagram(1024);
	}//end of method
	
	public static DatagramPacket createEmptyDatagram(int size) {
		byte[] buf = new byte[1024];
		return new DatagramPacket(buf, 1024);
	}//end of method
	
	public static String serialize(Object obj)	{
		return DatagramFactory.gson.toJson(obj);
	}
	
	public static String hexString(Datagram dg)	{
		Packet p = new Packet(dg.getHeader(), dg.getPayload());
		return DatagramFactory.hexString(p);
	}
	
	public static String hexString(Packet p)	{
		byte[] a = p.getHeader().toByteArray();
		byte[] b = p.getPayload().toByteArray();
		ByteBuffer bf = ByteBuffer.allocate(a.length + b.length + 1);
		bf.put(a);
		bf.put(b);
		byte[] arr = hexcoder.encode(bf.array());
		return new String(arr, charset);
	}//end of method
	
}
