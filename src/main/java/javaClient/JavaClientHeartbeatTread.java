package javaClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JavaClientHeartbeatTread implements Runnable {
	
	private String user;
	private String UID; 
	
	public JavaClientHeartbeatTread (String user){  // UID needs to be received when we figure out what it should be..
		this.user = user; 
	}
	
	
	@Override
	public void run() {
		ScheduledExecutorService pulseHeartbeat;
		pulseHeartbeat = Executors.newScheduledThreadPool(1);
		pulseHeartbeat.scheduleWithFixedDelay(() -> heartbeat(user), 2000 , 2000 , TimeUnit.MILLISECONDS);
	}
	
	private Runnable heartbeat(String user) {
		
		String heartBeat = "{\r\n"
				+ "    \"name\": \""+user+"\",\r\n"
				+ "    \"clientId\": \"JP\"\r\n"
				+ "}"; 
		
		try(ZContext context = new ZContext()){
			
			ZMQ.Socket socket = context.createSocket(SocketType.REQ); 
			socket.connect("tcp://ds.iit.his.se:5556");
			socket.send(heartBeat.getBytes(ZMQ.CHARSET),0);
			byte[] reply = socket.recv(0); 
			
			
			//cant receive error message 
			//ZMQ.Socket error = context.createSocket(SocketType.SUB);
			//error.connect("tcp://ds.iit.his.se:5555");
			//error.subscribe("error");
			
			//String errorMessage =  new String(error.recv(), ZMQ.CHARSET);
			//String msg =  new String(error.recv(), ZMQ.CHARSET);
			
			//System.out.println(errorMessage);
			//System.out.println(msg);
		
		}
		
		
		// System.out.println("Hello FROM heartbeat!");
		 return null; 
	}
}
