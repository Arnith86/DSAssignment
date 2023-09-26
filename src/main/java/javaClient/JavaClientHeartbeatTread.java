package javaClient;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JavaClientHeartbeatTread /*implements Runnable*/ {
	
	private String user;
	private UUID UUID; 
	private String serverAddress;
	private int outPort;
	
	
	public JavaClientHeartbeatTread (String user, String serverAddress, int outPort, UUID UUID){  // UID needs to be received when we figure out what it should be..
		this.user = user; 
		this.serverAddress = serverAddress;
		this.outPort = outPort;
		this.UUID = UUID; 
	}
	
	
	// @Override
	// public void run() {
	// 	heartbeat();
	// 	// ScheduledExecutorService pulseHeartbeat;
	// 	// pulseHeartbeat = Executors.newScheduledThreadPool(1);
	// 	// pulseHeartbeat.scheduleWithFixedDelay(() -> heartbeat(), 2000 , 2000 , TimeUnit.MILLISECONDS);
	// }
	
	protected void /*Runnable*/ heartbeat() {
		// System.out.println("Badump!");
		// System.out.println("user: "+user+" address: "+serverAddress+" outPort: "+outPort);
		String heartBeat = "{\r\n"
				+ "    \"name\": \""+user+"\",\r\n"
				+ "    \"clientId\": \""+UUID+"\"\r\n"
				+ "}"; 
		
		try(ZContext context = new ZContext()){
			
			ZMQ.Socket socket = context.createSocket(SocketType.REQ); 
			socket.connect(/* "tcp://ds.iit.his.se:5556"*/ "tcp://"+serverAddress+":"+outPort );
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
			socket.close();
			context.close();
		}
		
		
		// System.out.println("Hello FROM heartbeat!");
		//  return null; 
	}
}
