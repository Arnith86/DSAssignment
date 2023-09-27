package javaClient;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

// this class is an extension of the JavaClien class, it contains only code related 
// to the supervisor version of the client

public class SupervisorJavaClient extends JavaClient  {

    public SupervisorJavaClient(JavaClientGui gui) {
        super(gui);
        
    }
	
	public void setUser(String user, String status) {
		this.user=user;
		currentSupervisorStatus = status; 
	}
    
	// places supplied name in the supervisor queue
    public void placeInSupervisorQueue() {
		enterSupervisorQueue(super.user, super.serverAddress, super.outPortNummber);
    }

    // places supplied user in the server queue
	private void enterSupervisorQueue(String user, String address, int outPort) {
		
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
				
			String enterSupervisorQueue = " {\r\n" + 
			        "                        \"enterSupervisorQueue\": true,\r\n" + 
			        "                        \"name\": \""+user+"\",\r\n" + 
			        "                        \"status\": \""+currentSupervisorStatus+"\", \r\n" + 
			        "                        \"clientId\": \""+newUUID+"\"\r\n" + 
			        "                       }";
                
			socket.send(enterSupervisorQueue.getBytes(ZMQ.CHARSET),0);

			byte[] reply = socket.recv(0);

			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));  
			
			// makes sure theres only ever one version of the heartbeat thread active at a given time
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

	// this method starts one heartbeat
	private Runnable heartbeat(JavaClientHeartbeatTread heartbeat){
		heartbeat.heartbeat();
		return null; 
	} 

	// sends the registered supervisor message
	protected void registerSupervisorMessage(String message){
		try(ZContext context = new ZContext()){

			ZMQ.Socket socket = context.createSocket(SocketType.REQ);			
			try {
					socket.connect("tcp://"+serverAddress+":"+outPortNummber );
				} catch (Exception e) {
					System.out.println(e);
				} 
				
			String enterSupervisorQueue = 	"{\r\n" + //
											"    \"supervisor\":\""+user+"\",\r\n" + //
											"    \"message\":\""+message+"\"\r\n" + //
											"} ";
       
			socket.send(enterSupervisorQueue.getBytes(ZMQ.CHARSET),0);

			byte[] reply = socket.recv(0);

			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET)); 

			socket.close();
			context.close();
		}
	}

	// sends the registered change to the supervisors status 
	protected void changeSupervisorStatus(String status){

		currentSupervisorStatus = status; 
		try(ZContext context = new ZContext()){

			ZMQ.Socket socket = context.createSocket(SocketType.REQ);			
			try {
					socket.connect("tcp://"+serverAddress+":"+outPortNummber);
				} catch (Exception e) {
					System.out.println(e);
				} 
	
			String statusChange = 	"{\r\n" + //
					"\t\"statusChange\": true,\r\n" + //
					"\t\"supervisor\": \""+user+"\",\r\n" + //
					"\t\"status\": \""+status+"\"\r\n" + //
					"}";

			socket.send(statusChange.getBytes(ZMQ.CHARSET),0);

			byte[] reply = socket.recv(0);

			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));  

			socket.close();
			context.close();
		}
	}

	// supervisor takes on the next student in the queue 
	protected void takeOnAStudent(){
		try(ZContext context = new ZContext()){

			ZMQ.Socket socket = context.createSocket(SocketType.REQ);			
			try {
					socket.connect("tcp://"+serverAddress+":"+outPortNummber);
				} catch (Exception e) {
					System.out.println(e);
				} 

			String nextStudent = "{\"supervisor\": \""+user+"\", \"nextStudent\": true}";

			socket.send(nextStudent.getBytes(ZMQ.CHARSET),0);

			byte[] reply = socket.recv(0);

			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));  

			socket.close();
			context.close();
		}
	}
}
