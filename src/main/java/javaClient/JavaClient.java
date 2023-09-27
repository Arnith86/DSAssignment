package javaClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JavaClient {

	// variables used in the class
	private static String undefined = "undefined";
	private String reconnectNotify = "reconnectNotify";
	protected UUID newUUID = null; 
	protected String user;
	protected String serverAddress;
	protected int inPortNummber;
	protected int outPortNummber;
	protected String currentSupervisorStatus;

	// gives access to the gui
	private JavaClientGui gui;
	
	// the used to supply the queues
	private LinkedList<Students> studentList;
	private LinkedList<Supervisors> supervisorList;

	// related to the timed execution of events 
	private ScheduledExecutorService queueUpdater;
	protected ScheduledExecutorService heartbeatThread;
	protected JavaClientHeartbeatTread heartbeat; 

	// sets upp the client
	public JavaClient (JavaClientGui gui){
		this.gui = gui;
		queueUpdater = Executors.newScheduledThreadPool(2);
	}

	public void setUser(String user) {
		this.user=user;
	}

	public void placeInQueue(){
		enterQueue(user,serverAddress, outPortNummber);
	}

	// performes the ZMQ subscriber connection for a specific topic, 
	// returns json-string 
	protected String subscribe(String topic){
		
		String jsonMsg;

		try(ZContext context = new ZContext()){
			ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);

			try {
					subscriber.connect("tcp://"+serverAddress+":"+inPortNummber );
				} catch (Exception e) {
					System.out.println(e);
				} 
			
			subscriber.subscribe(topic);

			try {
				String subscriberTopic =  new String(subscriber.recv(), ZMQ.CHARSET);	
				} catch (Exception e) {	
			 		System.out.println(e);
			}

			jsonMsg = new String(subscriber.recv(), ZMQ.CHARSET);
			subscriber.close();
			context.close();
		}

		return jsonMsg;
	}

	// Checks if there are active threads, if yes, there shutdown
	private void cheackAndShutdownThreads(ScheduledExecutorService threadpool){

		ThreadPoolExecutor executor = (ThreadPoolExecutor) threadpool;
			int activeThreadCount = executor.getActiveCount();
			System.out.println("active: "+activeThreadCount);
			if(activeThreadCount > 0){
				
				threadpool.shutdown();
				System.out.println("not shutdown: active: "+activeThreadCount);
			}
			System.out.println("active: "+activeThreadCount);
	}

	// sets the server related information
	// also activates the timed events related to updating the queues
	public void setAddressAndPorts(String address, int inPort, int outPort){
		
		serverAddress = address;
		inPortNummber = inPort;
		outPortNummber = outPort;

		// if any active thread exist this will shut them down
		if(!queueUpdater.isShutdown()){
		
			cheackAndShutdownThreads(queueUpdater);
		}	
		
		queueUpdater = Executors.newScheduledThreadPool(1);
		queueUpdater.scheduleWithFixedDelay(() -> getCurrentQueue(), 0 , 500 , TimeUnit.MILLISECONDS);  
		queueUpdater.scheduleWithFixedDelay(() -> getCurrentSupervisors(), 0 , 550 , TimeUnit.MILLISECONDS);
	}

	// creates a hashmap of the supplied JSONObject and returns it
	private HashMap<String, Object> createObjectHashMap(JSONObject jsonObject){
		
		HashMap<String, Object> map = new HashMap<>();
					Iterator<String> iter2 = jsonObject.keys();
					while(iter2.hasNext()) {
						String key = iter2.next();
						map.put(key, jsonObject.get(key));
					}

		return map; 
	}

	// Collects available supervisors
	private Runnable getCurrentSupervisors() {
	
		supervisorList = new LinkedList<Supervisors>();
		String msg =  subscribe("supervisors");
		JSONArray jsonMsg = new JSONArray(msg);
		JSONObject supervisorMsgObject = new JSONObject(); // will be used when checking for supervisor messages
		
		// extracts the String value name from json objects housed in the jsonarray
		for (int i = 0; i < jsonMsg.length(); i++) {

			// converts Json array elements into Json objects
			JSONObject supervisor = new JSONObject(jsonMsg.get(i).toString());
			HashMap<String, Object> map = createObjectHashMap(supervisor);
			
			String name = (String) map.get("name");
			String status = (String) map.get("status");
			Supervisors supervisorsObject = new Supervisors(name, status);

			// Check if "client" is a JSONObject
			JSONObject clientObject = supervisor.optJSONObject("client");
			if (clientObject != null) {

				// "client" is a JSONObject, handle object
				JSONObject client = supervisor.getJSONObject("client");
				supervisorsObject.setSupervising(client);
			
			} else {
				//client is not a JSONObject or does not exist
				String client = (String) map.get("client");
				supervisorsObject.setSupervising();
			}

			supervisorList.add(supervisorsObject);

			// checks if there is a message for the user
			if((clientObject != null) && (clientObject.getString("name").equals(user))) {
				
				String msg2 = subscribe(user);
				supervisorMsgObject = new JSONObject(msg2);
				HashMap<String, Object> map2 = createObjectHashMap(supervisorMsgObject);

				String supervisorMessage = (String) map2.get("message");
				
				if (supervisorMessage != null) {
					supervisorsObject.setSupervisorMessage(supervisorMessage);	 
				}
			}
		}

		gui.setCurrentSupervisors(supervisorList);  	
		
		return null;
	}

	// collects the contents of the student queue
	private Runnable getCurrentQueue() {
		
		studentList = new LinkedList<Students>();

		String msg = subscribe("queue");
		JSONArray jsonMsg = new JSONArray(msg);

		// makes sure that the string is not empty 
		if(!msg.equals("[{}]")){
			// extracts the String value name from json objects housed in the jsonarray
			for (int i = 0; i < jsonMsg.length(); i++) {

				// converts Json array elements into Json objects
				JSONObject student = new JSONObject(jsonMsg.get(i).toString());
				HashMap<String, Object> map = createObjectHashMap(student);
			
				// extract the value "name" and "ticket" from object and create a new object using that value
				// these are placed in a linked list.
				String name = (String) map.get("name");
				int ticket = (int) map.get("ticket");
				Students studentObject = new Students(name, ticket);
				studentList.add(studentObject);
			}
		}

		gui.setStudentQueue(studentList);
		return null;
	}

	// places supplied user in the server queue
	private void enterQueue(String user, String address, int outPort) {
		
		if(newUUID == null){
			newUUID = UUID.randomUUID();
		}
		 

		try(ZContext context = new ZContext()){

			ZMQ.Socket socket = context.createSocket(SocketType.REQ);			
			try {
					socket.connect("tcp://"+address+":"+outPort);
				} catch (Exception e) {
					System.out.println(e);
				} 
				
			String enterQueue = "{\"enterQueue\": true, \"name\": \""+user+"\", \"clientId\": \""+newUUID+"\"}";

			socket.send(enterQueue.getBytes(ZMQ.CHARSET),0);

			byte[] reply = socket.recv(0);

			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));
			
			// makes sure that there is only one active version of the heartbeat thread active at a time
			if(heartbeatThread != null && !heartbeatThread.isShutdown()){
				heartbeatThread.shutdown(); 
				heartbeat = new JavaClientHeartbeatTread(user, serverAddress, outPortNummber, newUUID, this);
			} else {
				heartbeat = new JavaClientHeartbeatTread(user, serverAddress, outPortNummber, newUUID, this);
			}
			
			heartbeatThread = Executors.newScheduledThreadPool(1);
			heartbeatThread.scheduleWithFixedDelay(() -> heartbeat(heartbeat), 0 , 500 , TimeUnit.MILLISECONDS);  
			
			socket.close();
			context.close();
		}
	}

	protected void reconnectingNotification(){
		gui.notifications(reconnectNotify);
	}
	
	private Runnable heartbeat(JavaClientHeartbeatTread heartbeat){
		heartbeat.heartbeat();
		return null; 
	} 
}
