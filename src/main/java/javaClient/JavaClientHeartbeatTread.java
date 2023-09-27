package javaClient;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JavaClientHeartbeatTread {
	
	private String user;
	private UUID UUID; 
	private String serverAddress;
	private int outPort;
	private JavaClient javaClient;
	
	// this class supplies the heartbeat to the server, it only sends a single beat per call.
	public JavaClientHeartbeatTread (String user, String serverAddress, int outPort, UUID UUID, JavaClient javaClient){  
		this.user = user; 
		this.serverAddress = serverAddress;
		this.outPort = outPort;
		this.UUID = UUID;
		this.javaClient = javaClient; 
	}
	
	protected void heartbeat() {
		
		String heartBeat = "{\r\n"
				+ "    \"name\": \""+user+"\",\r\n"
				+ "    \"clientId\": \""+UUID+"\"\r\n"
				+ "}"; 
		
		try(ZContext context = new ZContext()){
			
			ZMQ.Socket socket = context.createSocket(SocketType.REQ); 
			socket.connect("tcp://"+serverAddress+":"+outPort );
			socket.send(heartBeat.getBytes(ZMQ.CHARSET),0);
			ZMQ.Poller poller = context.createPoller(1);
	        poller.register(socket, ZMQ.Poller.POLLIN);
	        int rc = -1;
	        while (rc == -1) {
	            rc = poller.poll(2000);
	        }
	        poller.pollin(0);
	        if (poller.pollin(0) == true) {
	        	byte[] reply = socket.recv(0); 
	        }
	        else {
	        	System.out.println("error");
	        }
			
			socket.close();
			context.close();
		}
	}
}
