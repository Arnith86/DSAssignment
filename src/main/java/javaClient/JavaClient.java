package javaClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JavaClient {
	private String user; 
	private JavaClientGui gui; 
	private LinkedList<Students> studentList; 
	
	public JavaClient (JavaClientGui gui){
		// IN TESTING 
		this.gui = gui; 
	}
	
	
	public void setUser(String user) {
		this.user=user; 
		enterQueue(user);
	}
	
	private Runnable getCurrentQueue() {
		
		studentList = new LinkedList<Students>();
		
		try(ZContext context = new ZContext()){
			ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
			subscriber.connect("tcp://ds.iit.his.se:5555");
			subscriber.subscribe("queue");
			
			String topic =  new String(subscriber.recv(), ZMQ.CHARSET);
			JSONArray jsonMsg = new JSONArray(new String(subscriber.recv(), ZMQ.CHARSET));
		
			// extracts the String value name from json objects housed in the jsonarray
			for (int i = 0; i < jsonMsg.length(); i++) {
				
				// converts Json array elements into Json objects 
				JSONObject student = new JSONObject(jsonMsg.get(i).toString());
				HashMap<String, Object> map = new HashMap<>();
				Iterator<String> iter = student.keys();
				
				// makes a hashmap out of the JSONobject 
				while(iter.hasNext()) {
					String key = iter.next();
					map.put(key, student.get(key));
				}
				
				// extract the value "name" and "ticket" from object and create a new object using that value
				// these are placed in a linked list.
				String name = (String) map.get("name");
				int ticket = (int) map.get("ticket");
				Students studentObject = new Students(name, ticket);
				studentList.add(studentObject);
			}
			
			gui.setStudentQueue(studentList);
			studentList.clear();
			context.close();
		}
		return null; 
	}
	
	// places supplied user in the TinyQueue 
	// TODO!!!!!   WE NEED TO CREATE A UID SOMEHOW!!!!!!!!!!!!!!!!!!!!!!
	private void enterQueue(String user) {
		
		try(ZContext context = new ZContext()){
			
			ZMQ.Socket socket = context.createSocket(SocketType.REQ); 
			socket.connect("tcp://ds.iit.his.se:5556");
			
			String enterQueue = "{\"enterQueue\": true, \"name\": \""+user+"\", \"clientId\": \"JP\"}";
			
			socket.send(enterQueue.getBytes(ZMQ.CHARSET),0);
		
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
		ScheduledExecutorService queueUpdater;
		queueUpdater = Executors.newScheduledThreadPool(1);
		queueUpdater.scheduleWithFixedDelay(() -> javaClient.getCurrentQueue(), 0 , 500 , TimeUnit.MILLISECONDS);
		
		//javaClient.enterQueue();
	
	}
}
