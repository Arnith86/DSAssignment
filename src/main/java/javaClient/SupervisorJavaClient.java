package javaClient;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class SupervisorJavaClient extends JavaClient  {

    //private String user;

    public SupervisorJavaClient(JavaClientGui gui) {
        super(gui);
        
    }

    @Override
    public void placeInQueue() {
        // TODO Auto-generated method stub
            System.out.println("placed in queue");
            enterSupervisorQueue(super.user, super.serverAddress, super.outPortNummber);
    }
    // places supplied user in the server queue
	// TODO!!!!!   WE NEED TO CREATE A UID SOMEHOW!!!!!!!!!!!!!!!!!!!!!!
	private void enterSupervisorQueue(String user, String address, int outPort) {
        System.out.println(user+" "+address+" "+outPort);
		try(ZContext context = new ZContext()){

			ZMQ.Socket socket = context.createSocket(SocketType.REQ);			
			try {
					socket.connect(/* "tcp://ds.iit.his.se:5556" */  /* "tcp://"+address+":"+outPort*/ "tcp://localhost:5557");
				} catch (Exception e) {
					System.out.println(e);
				} 
				
			String enterSupervisorQueue = "{\"enterSupervisorQueue\": true, \"name\": \""+user+"\", \"clientId\": \"JP\"}";

			socket.send(enterSupervisorQueue.getBytes(ZMQ.CHARSET),0);

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
