package javaClient;

import java.util.UUID;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class SupervisorJavaClient extends JavaClient  {

    //private String user;

    public SupervisorJavaClient(JavaClientGui gui) {
        super(gui);
        
    }
	
	public void setUser(String user, String status) {
		this.user=user;
		currentSupervisorStatus = status; 
	}
    
	@Override
    public void placeInQueue() {
       
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
					socket.connect(/* "tcp://ds.iit.his.se:5557" */  /* "tcp://"+address+":"+outPort*/ "tcp://localhost:5557");
				} catch (Exception e) {
					System.out.println(e);
				} 
				
			String enterSupervisorQueue = " {\r\n" + 
			        "                        \"enterSupervisorQueue\": true,\r\n" + 
			        "                        \"name\": \""+user+"\",\r\n" + 
			        "                        \"status\": \""+currentSupervisorStatus+"\", \r\n" + 
			        "                        \"clientId\": \""+newUUID+"\"\r\n" + 
			        "                       }";
            //"{\"enterSupervisorQueue\": true, \"name\": \""+user+"\", \"clientId\": \"JP\"}";
                
			socket.send(enterSupervisorQueue.getBytes(ZMQ.CHARSET),0);

			byte[] reply = socket.recv(0);

			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));  // this should not be written out when application is finished only receive the reply

			// IN TESTING
			// JavaClientHeartbeatTread heartbeat = new JavaClientHeartbeatTread(user, address, outPort);
			// Thread heartbeatThread = new Thread(heartbeat);
			// heartbeatThread.start();
			// IN TESTING
			// THIS MUST BE TERMINATED WHEN supervisor removes student from list
			socket.close();
			context.close();
		}
	}

	protected void registerSupervisorMessage(String message){
		try(ZContext context = new ZContext()){

			ZMQ.Socket socket = context.createSocket(SocketType.REQ);			
			try {
					socket.connect(/* "tcp://ds.iit.his.se:5557" */  /* "tcp://"+address+":"+outPort*/ "tcp://localhost:5557");
				} catch (Exception e) {
					System.out.println(e);
				} 
				
			String enterSupervisorQueue = 	"{\r\n" + //
											"    \"supervisor\":\""+user+"\",\r\n" + //
											"    \"message\":\""+message+"\"\r\n" + //
											"} ";
       
			socket.send(enterSupervisorQueue.getBytes(ZMQ.CHARSET),0);

			byte[] reply = socket.recv(0);

			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));  // this should not be written out when application is finished only receive the reply

			socket.close();
			context.close();
		}
	}

	protected void changeSupervisorStatus(String status){

		currentSupervisorStatus = status; 
		try(ZContext context = new ZContext()){

			ZMQ.Socket socket = context.createSocket(SocketType.REQ);			
			try {
					socket.connect(/* "tcp://ds.iit.his.se:5557" */  /* "tcp://"+address+":"+outPort*/ "tcp://localhost:5557");
				} catch (Exception e) {
					System.out.println(e);
				} 
				// {
				// 	"statusChange": true,
				// 	"supervisor": "<name>",
				// 	"status": "<unique id string>"
				// }
			String statusChange = 	"{\r\n" + //
					"\t\"statusChange\": true,\r\n" + //
					"\t\"supervisor\": \""+user+"\",\r\n" + //
					"\t\"status\": \""+status+"\"\r\n" + //
					"}";

			socket.send(statusChange.getBytes(ZMQ.CHARSET),0);

			byte[] reply = socket.recv(0);

			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));  // this should not be written out when application is finished only receive the reply

			socket.close();
			context.close();
		}
	}

	// supervisor takes on the next student in the queue 
	protected void takeOnAStudent(){
		try(ZContext context = new ZContext()){

			ZMQ.Socket socket = context.createSocket(SocketType.REQ);			
			try {
					socket.connect(/* "tcp://ds.iit.his.se:5557" */  /* "tcp://"+address+":"+outPort*/ "tcp://localhost:5557");
				} catch (Exception e) {
					System.out.println(e);
				} 
				// {
				// 	"supervisor": "<name>",
				// 	"nextStudent": true
				// }
			String nextStudent = "{\"supervisor\": \""+user+"\", \"nextStudent\": true}";

			socket.send(nextStudent.getBytes(ZMQ.CHARSET),0);

			byte[] reply = socket.recv(0);

			System.out.println("this was recived: " + new String(reply, ZMQ.CHARSET));  // this should not be written out when application is finished only receive the reply

			socket.close();
			context.close();
		}
	}
}
