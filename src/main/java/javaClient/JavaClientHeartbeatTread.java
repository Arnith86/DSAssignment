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
	
	
	public JavaClientHeartbeatTread (String user, String serverAddress, int outPort, UUID UUID){  
		this.user = user; 
		this.serverAddress = serverAddress;
		this.outPort = outPort;
		this.UUID = UUID; 
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
			byte[] reply = socket.recv(0); 
			
			socket.close();
			context.close();
		}
	}
}
