package javaClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

// import org.zeromq.ZMQ.Socket;

public class JavaClient implements Runnable {
	
	JavaClientGui gui; 
	ScheduledExecutorService heartbeatTimer;
	private LinkedList<Students> studentList; 
	
	public JavaClient (JavaClientGui gui){
		// IN TESTING 
		this.gui = gui; 
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
		studentList = new LinkedList<Students>();
		
		try(ZContext context = new ZContext()){
			ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
			subscriber.connect("tcp://ds.iit.his.se:5555");
			
			subscriber.subscribe("queue");
			
			String topic =  new String(subscriber.recv(), ZMQ.CHARSET);
			
			
			JSONArray jsonMsg = new JSONArray(new String(subscriber.recv(), ZMQ.CHARSET));
		
			
			for (int i = 0; i < jsonMsg.length(); i++) {
				 
				JSONObject student = new JSONObject(jsonMsg.get(i).toString());
				HashMap<String, Object> map = new HashMap<>();
				Iterator<String> iter = student.keys();
				
				while(iter.hasNext()) {
					String key = iter.next();
					map.put(key, student.get(key));
				}
				
				String name = (String) map.get("name");
				Students studentObject = new Students(name);
				studentList.add(studentObject);
			}
			
			gui.setStudentQueue(studentList);
		}
		
	}
	
	// places supplied user in the TinyQueue 
	private void enterQueue() {
		
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
		
		JavaClientGui gui = new JavaClientGui(); 
		
		JavaClient javaClient = new JavaClient(gui);
		
		javaClient.enterQueue();
		javaClient.getCurrentQueue();
		
		
		
	}
}
