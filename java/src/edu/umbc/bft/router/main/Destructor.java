package edu.umbc.bft.router.main;

import edu.umbc.bft.router.engine.Engine;
import edu.umbc.bft.util.Logger;

public class Destructor extends Thread {

	private PortListener listener;
	private Engine engine;
	
	public Destructor()		{}//end of constructor
	
	public void setListener(PortListener listener) {
		this.listener = listener;
	}
	
	public void setEngine(Engine engine) {
		this.engine = engine;
	}
	
	@Override
	public void run() {
		this.listener.stop();
		this.engine.stop();
		Logger.info(this.getClass(), "Closing application router...");
		System.out.println(" ---Application closed--- ");
	}//end of thread
	
}
