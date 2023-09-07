package javaClient;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
// import org.zeromq.ZMQ.Socket;

public class JavaClient implements Runnable {
	
	ScheduledExecutorService heartbeatTimer;
	
	public JavaClient (){
		// IN TESTING 
		heartbeatTimer = Executors.newScheduledThreadPool(1);
		//heartbeatTimer.execute(null);
		
		
	}
	

	

	@Override
	public void run() {
//		enterQueue();
//		getCurrentQueue();
		//heartbeatTimer.scheduleWithFixedDelay(heartbeat(), 500, 500, TimeUnit.MILLISECONDS);
		
		
	}
	
//	private Runnable heartbeat() {
//		
//		String heartBeat = "{\r\n"
//				+ "    \"name\": \"JP\",\r\n"
//				+ "    \"clientId\": \"JP\"\r\n"
//				+ "}"; 
//		
//		try(ZContext context = new ZContext()){
//			
//			ZMQ.Socket socket = context.createSocket(SocketType.REQ); 
//			socket.connect("tcp://ds.iit.his.se:5556");
//			socket.send(heartBeat.getBytes(ZMQ.CHARSET),0);
//			
//		}
//		return null; 
//	}
	
	private void getCurrentQueue() {
		// SEEMS TO WORK FINE 
		try(ZContext context = new ZContext()){
			ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
			subscriber.connect("tcp://ds.iit.his.se:5555");
			
			subscriber.subscribe("queue");
			
			String topic =  new String(subscriber.recv(), ZMQ.CHARSET);
			String msg =  new String(subscriber.recv(), ZMQ.CHARSET);
			
			System.out.println(topic);
			System.out.println(msg);
		}
		
	}
	
	private void enterQueue() {
		// NOT WORKING CORRECTLY 
		try(ZContext context = new ZContext()){
			
			ZMQ.Socket socket = context.createSocket(SocketType.REQ); 
			socket.connect("tcp://ds.iit.his.se:5556");
			
			System.out.println("Placing in Queue");
			
			String enterQueue = "{\"enterQueue\": true, \"name\": \"JP\", \"clientId\": \"JP\"}";
			
			socket.send(enterQueue.getBytes(ZMQ.CHARSET),0);
			System.out.println("Placed in queue");
			
			byte[] reply = socket.recv(0); 
			
			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));
			
			// IN TESTING 
			JavaClientHeartbeatTread heartbeat = new JavaClientHeartbeatTread();
			Thread heartbeatThread = new Thread(heartbeat);
			heartbeatThread.start(); 
			// IN TESTING 
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
		System.out.println("hello world");
		JavaClient javaClient = new JavaClient();
		
		javaClient.enterQueue();
		javaClient.getCurrentQueue();
		
		while(true) {}
		
	}
}
