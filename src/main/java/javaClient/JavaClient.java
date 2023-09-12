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

public class JavaClient {
	private String user; 
	private JavaClientGui gui; 
	private ScheduledExecutorService heartbeatTimer;
	private LinkedList<Students> studentList; 
	
	public JavaClient (JavaClientGui gui){
		// IN TESTING 
		this.gui = gui; 
		
		// this timer gets an updated queue every 0.5 sec
		// THOUGH AT THE TIME THERE IS NOTHING THAT REMOVES THE OLD QUEUE!! so it gets addad to the old!
		ScheduledExecutorService queueUpdater;
		queueUpdater = Executors.newScheduledThreadPool(1);
		queueUpdater.scheduleWithFixedDelay(() -> getCurrentQueue(), 500 , 500 , TimeUnit.MILLISECONDS);
		
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
	
	public void setUser(String user) {
		this.user=user; 
		enterQueue(user);
	}
	
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
	// TODO!!!!!   WE NEED TO CREATE A UID SOMEHOW!!!!!!!!!!!!!!!!!!!!!!
	private void enterQueue(String user) {
		
		try(ZContext context = new ZContext()){
			
			ZMQ.Socket socket = context.createSocket(SocketType.REQ); 
			socket.connect("tcp://ds.iit.his.se:5556");
			
			System.out.println("Placing in Queue"); // remove this when application is finished 
			
			String enterQueue = "{\"enterQueue\": true, \"name\": \""+user+"\", \"clientId\": \"JP\"}";
			
			socket.send(enterQueue.getBytes(ZMQ.CHARSET),0);
			
			System.out.println("Placed in queue");   // remove this when application is finished 
			
			byte[] reply = socket.recv(0); 
			
			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));  // this should not be written out when application is finished only receive the reply 
			
			// IN TESTING 
			JavaClientHeartbeatTread heartbeat = new JavaClientHeartbeatTread(user);
			Thread heartbeatThread = new Thread(heartbeat);
			heartbeatThread.start(); 
			// IN TESTING 
		}
		
	}
	public static void main(String[] args) {
		
		JavaClientGui gui = new JavaClientGui(); 
		
		JavaClient javaClient = new JavaClient(gui);
		
		//javaClient.enterQueue();
		//javaClient.getCurrentQueue();
		
		
		
	}
}
