package javaClient;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

public class JavaClient {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("hello world");
		
		try(ZContext context = new ZContext();
			Socket socket = context.createSocket(SocketType.SUB)){
			socket.connect("tcp://ds.iit.his.se:5555");
			socket.subscribe("queue");
			
			String topic = new String(socket.recv(), ZMQ.CHARSET);
			String msg = new String(socket.recv(), ZMQ.CHARSET);
			
			System.out.println(topic);
			System.out.println(msg);
		}
	}
}
