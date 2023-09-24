package javaClient;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
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

	private static String undefined = "undefined";
	protected String user;
	protected String serverAddress;
	protected int inPortNummber;
	protected int outPortNummber;
	protected String currentSupervisorStatus;

	private JavaClientGui gui;
	private LinkedList<Students> studentList;
	private LinkedList<Supervisors> supervisorList;

	private ScheduledExecutorService queueUpdater;
	protected JavaClientHeartbeatTread heartbeat; 
	protected Thread heartbeatThread; 


	public JavaClient (JavaClientGui gui){
		// IN TESTING
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
			 	//System.out.println("what topic:"+topic);	
				} catch (Exception e) {	
			 		System.out.println(e);
			}

			jsonMsg = new String(subscriber.recv(), ZMQ.CHARSET);
			subscriber.close();
			context.close();
		}

		return jsonMsg;
	}


	public void setAddressAndPorts(String address, int inPort, int outPort){
		
		serverAddress = address;
		inPortNummber = inPort;
		outPortNummber = outPort;

		// if any existing threads are active this will shut them down, and clean the current lists
		if(!queueUpdater.isShutdown()){
		
			ThreadPoolExecutor executor = (ThreadPoolExecutor) queueUpdater;
			int activeThreadCount = executor.getActiveCount();
			if(activeThreadCount > 0){
				queueUpdater.shutdown();
				// studentList.clear();
				// supervisorList.clear();
				System.out.println("not shutdown: active: "+activeThreadCount);
			}
		}	
		
		queueUpdater = Executors.newScheduledThreadPool(1);
		queueUpdater.scheduleWithFixedDelay(() -> getCurrentQueue(), 0 , 500 , TimeUnit.MILLISECONDS);  
		queueUpdater.scheduleWithFixedDelay(() -> getCurrentSupervisors(), 0 , 550 , TimeUnit.MILLISECONDS);
	}

	// Will display available supervisors
	// STILL IN TESTING !!!!!!!!!!!!!!!!
	private Runnable getCurrentSupervisors() {
	
		supervisorList = new LinkedList<Supervisors>();
		String msg =  subscribe("supervisors");
		JSONArray jsonMsg = new JSONArray(msg);
		JSONObject supervisorMsgObject = new JSONObject(); // will be used when checking for supervisor messages
		
		// extracts the String value name from json objects housed in the jsonarray
		for (int i = 0; i < jsonMsg.length(); i++) {

			// converts Json array elements into Json objects
			JSONObject supervisor = new JSONObject(jsonMsg.get(i).toString());
			HashMap<String, Object> map = new HashMap<>();
			Iterator<String> iter = supervisor.keys();

			// makes a hashmap out of the JSONobject
			// THIS METHOD SHOULD BE A SINGLE METHOD
			while(iter.hasNext()) {
				String key = iter.next();
				map.put(key, supervisor.get(key));
			}

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


			//System.out.println(supervisorList.toString());
			supervisorList.add(supervisorsObject);

				// checks if there is a message for the user
				if((clientObject != null) && (clientObject.getString("name").equals(user))) {
					
					String msg2 = subscribe(user);
					supervisorMsgObject = new JSONObject(msg2);
					
					// makes a hashmap out of the JSONobject
					// THIS METHOD SHOULD BE A SINGLE METHOD
					HashMap<String, Object> map2 = new HashMap<>();
					Iterator<String> iter2 = supervisorMsgObject.keys();
					while(iter2.hasNext()) {
						String key = iter2.next();
						map2.put(key, supervisorMsgObject.get(key));
					}

					String supervisorMessage = (String) map2.get("message");
					
					if (supervisorMessage != null) {
						supervisorsObject.setSupervisorMessage(supervisorMessage);	 
					}
					// supervisorsObject.setSupervisorMessage((String) map2.get("message"));
				}
		}

		gui.setCurrentSupervisors(supervisorList);  	
		
		return null;
	}

	private Runnable getCurrentQueue() {
		
		studentList = new LinkedList<Students>();

		String msg = subscribe("queue");
		JSONArray jsonMsg = new JSONArray(msg);

		// extracts the String value name from json objects housed in the jsonarray
		if(!msg.equals("[{}]")){
			for (int i = 0; i < jsonMsg.length(); i++) {

				// converts Json array elements into Json objects
				JSONObject student = new JSONObject(jsonMsg.get(i).toString());
				HashMap<String, Object> map = new HashMap<>();
				Iterator<String> iter = student.keys();

				// makes a hashmap out of the JSONobject
				// THIS METHOD SHOULD BE A SINGLE METHOD
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
		}

		gui.setStudentQueue(studentList);
		return null;
	}

	// places supplied user in the server queue
	// TODO!!!!!   WE NEED TO CREATE A UID SOMEHOW!!!!!!!!!!!!!!!!!!!!!!
	private void enterQueue(String user, String address, int outPort) {

		try(ZContext context = new ZContext()){

			ZMQ.Socket socket = context.createSocket(SocketType.REQ);			
			try {
					socket.connect(/* "tcp://ds.iit.his.se:5556" */  "tcp://"+address+":"+outPort /*"tcp://localhost:5556" */ );
				} catch (Exception e) {
					System.out.println(e);
				} 
				
			String enterQueue = "{\"enterQueue\": true, \"name\": \""+user+"\", \"clientId\": \"JP\"}";

			socket.send(enterQueue.getBytes(ZMQ.CHARSET),0);

			byte[] reply = socket.recv(0);

			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));  // this should not be written out when application is finished only receive the reply

			// IN TESTING
			heartbeat = new JavaClientHeartbeatTread(user, serverAddress, outPortNummber);
			heartbeatThread = new Thread(heartbeat);
			heartbeatThread.start();
			// IN TESTING
			// THIS MUST BE TERMINATED WHEN supervisor removes student from list
			//context.destroy();
		}

	}





}
