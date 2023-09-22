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
	private String user;
	private String serverAddress;
	private int inPortNummber;
	private int outPortNummber;

	private Boolean fullAddressSupplied;

	private JavaClientGui gui;
	private LinkedList<Students> studentList;
	private LinkedList<Supervisors> supervisorList;

	private ScheduledExecutorService queueUpdater;

	public JavaClient (JavaClientGui gui){
		// IN TESTING
		this.gui = gui;
		queueUpdater = Executors.newScheduledThreadPool(2);
		//fullAddressSupplied = false;
	}


	public void setUser(String user) {
		this.user=user;
	}

	public void placeInQueue(){
		enterQueue(user,serverAddress, outPortNummber);
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
				studentList.clear();
				supervisorList.clear();
				System.out.println("not shutdown: active: "+activeThreadCount);
			}
		}	
		
		queueUpdater = Executors.newScheduledThreadPool(1);
		queueUpdater.scheduleWithFixedDelay(() -> getCurrentQueue(address, inPort), 0 , 500 , TimeUnit.MILLISECONDS);   //// DOOOOOONT FORGRET TO ACTIVATE AGAIN
		queueUpdater.scheduleWithFixedDelay(() -> getCurrentSupervisors(address, inPort), 0 , 550 , TimeUnit.MILLISECONDS);
		// this.fullAddressSupplied = true;  THIS IS CURRENTLY NOT FUNCTIONING
	}

	// Will display available supervisors
	// STILL IN TESTING !!!!!!!!!!!!!!!!
	// Cannot test right now though... no supervisors present..
	private Runnable getCurrentSupervisors(String address, int inPort ) {
		//if (fullAddressSupplied == true){     // WE NEED TO STOP A CONNECTION FROM HAPPENING IF NO ADDRESS HAS BEEN GIVEN
		// ^THIS IS CURRENTLY NOT FUNCTIONING

			supervisorList = new LinkedList<Supervisors>();

			try(ZContext context = new ZContext()){

				// gets the list of Current Supervisors
				ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);
				
				try {
					
					subscriber.connect( /*"tcp://	:5555" */ "tcp://"+address+":"+inPort/*"tcp://localhost:5555" */ );
				} catch (Exception e) {
					System.out.println(e);
				} 
				// finally {
				// 	subscriber.close();
				// 	context.close();
				// }
				
				subscriber.subscribe("supervisors");

				String topic =  new String(subscriber.recv(), ZMQ.CHARSET);
				//String msg =  new String(subscriber.recv(), ZMQ.CHARSET);
				//System.out.println("we are here");
				//System.out.println(topic); // for tests
				//System.out.println(msg);	// for tests

				JSONArray jsonMsg = new JSONArray(new String(subscriber.recv(), ZMQ.CHARSET));
				//System.out.println("after jsonmsg");
				//System.out.println(jsonMsg);
				
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
						//System.out.println(supervisorsObject.getStudentName());
						//System.out.println(supervisorsObject.getTicket());
					} else {
						//System.out.println("client is not a JSONObject or does not exist.");
						String client = (String) map.get("client");
						supervisorsObject.setSupervising();
						//System.out.println(supervisorsObject.getStudentName());
					}


					//System.out.println(supervisorList.toString());
					supervisorList.add(supervisorsObject);

					// checks if there is a message for the user
					// STILL IN EARLY TESTING !!!!!!!!!!!!!!!!
					// if(clientObject)){

					// }
					if((clientObject != null) && (clientObject.getString("name").equals(user))) {
					
						ZMQ.Socket supervisorMessage = context.createSocket(SocketType.SUB);
						supervisorMessage.connect(/*"tcp:// :5555"*/"tcp://"+address+":"+inPort);
						supervisorMessage.subscribe(user);

						String newTopic =  new String(supervisorMessage.recv(), ZMQ.CHARSET);	
						// System.out.println("topic: "+ newTopic);
						supervisorMsgObject = new JSONObject(new String(supervisorMessage.recv(), ZMQ.CHARSET));
						
						// makes a hashmap out of the JSONobject
						// THIS METHOD SHOULD BE A SINGLE METHOD
						HashMap<String, Object> map2 = new HashMap<>();
						Iterator<String> iter2 = supervisorMsgObject.keys();
						while(iter2.hasNext()) {
							String key = iter2.next();
							map2.put(key, supervisorMsgObject.get(key));
						}
						
						supervisorsObject.setSupervisorMessage((String) map2.get("message"));
						//System.out.println("supervisor message: "+supervisorMsgObject.toString()); // TESTITEST
					}
				}
		
				gui.setCurrentSupervisors(supervisorList);  // this should be moved to the end of the method when it is finisehed


				subscriber.close();
				context.close();
						
		}
		
		return null;
	}

	private Runnable getCurrentQueue(String address, int inPort) {
		
		studentList = new LinkedList<Students>();

		try(ZContext context = new ZContext()){
			ZMQ.Socket subscriber = context.createSocket(SocketType.SUB);

			try {
					subscriber.connect( /* "tcp://ds.iit.his.se:5555"*/  "tcp://"+address+":"+inPort /*"tcp://localhost:5555" */ );
				} catch (Exception e) {
					System.out.println(e);
				} 
			
			subscriber.subscribe("queue");

			try {
				String topic =  new String(subscriber.recv(), ZMQ.CHARSET);	
			 	//System.out.println("what topic:"+topic);	
				} catch (Exception e) {	
			 		System.out.println(e);
			}
			
			
			JSONArray jsonMsg = new JSONArray(new String(subscriber.recv(), ZMQ.CHARSET));

			// extracts the String value name from json objects housed in the jsonarray
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

			gui.setStudentQueue(studentList);
			subscriber.close();
			context.close();
		}
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
			JavaClientHeartbeatTread heartbeat = new JavaClientHeartbeatTread(user, address, outPort);
			Thread heartbeatThread = new Thread(heartbeat);
			heartbeatThread.start();
			// IN TESTING
			// THIS MUST BE TERMINATED WHEN supervisor removes student from list
			//context.destroy();
		}

	}





}
