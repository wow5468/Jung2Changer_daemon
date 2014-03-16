package com.nexters.jung2.daemon.server;

import org.apache.log4j.Logger;

import com.nexters.jung2.daemon.sentence.ConvertThread;

public class ServerMain {
	private static Logger logger = Logger.getLogger(ServerMain.class.getName());

	public static void main(String[] args)  {
		logger.info("########## Jung2 Daemon이 시작됩니다. ##########");
		ConvertThread ct = new ConvertThread();
		
		Thread t1 = new Thread(ct);
		
		t1.start();
	}

}
