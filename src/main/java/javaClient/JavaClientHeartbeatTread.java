package javaClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JavaClientHeartbeatTread implements Runnable {
	
	public JavaClientHeartbeatTread (){
		
	}
	
	
	@Override
	public void run() {
//		  ScheduledExecutorService makingACoffee;
//        makingACoffee = Executors.newScheduledThreadPool(1);
//        makingACoffee.scheduleWithFixedDelay(heartbeat(), 2000, 2000, TimeUnit.MILLISECONDS);//make coffee every 2 seconds
		while(true) {
			heartbeat();
		}
	}
	
	private void heartbeat() {
		
		String heartBeat = "{\r\n"
				+ "    \"name\": \"JP\",\r\n"
				+ "    \"clientId\": \"10\"\r\n"
				+ "}"; 
		
		try(ZContext context = new ZContext()){
			
			ZMQ.Socket socket = context.createSocket(SocketType.REQ); 
			socket.connect("tcp://ds.iit.his.se:5556");
			socket.send(heartBeat.getBytes(ZMQ.CHARSET),0);
			byte[] reply = socket.recv(0); 
			
			ZMQ.Socket error = context.createSocket(SocketType.SUB);
			error.connect("tcp://ds.iit.his.se:5555");
			
			error.subscribe("error");
			
			String errorMessage =  new String(error.recv(), ZMQ.CHARSET);
			String msg =  new String(error.recv(), ZMQ.CHARSET);
			
			System.out.println(errorMessage);
			//System.out.println(msg);
		
		}
		
		
		System.out.println("Hello FROM heartbeat!");
		// return null; 
	}

}
