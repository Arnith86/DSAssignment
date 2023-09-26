package javaClient;
// used to execute the student version of the Java Client
public class JavaClientExecutor {
 
    	public static void main(String[] args) {

		JavaClientGui gui = new JavaClientGui("student");
		JavaClient javaClient = new JavaClient(gui);
		gui.setClientObject(javaClient);
	}
}
