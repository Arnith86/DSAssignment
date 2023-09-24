package javaClient;

public class JavaSupervisorClientExecutor {
    	public static void main(String[] args) {

		JavaClientGui gui = new JavaClientGui("supervisor");
        
        JavaClient client = new JavaClient(gui); 
		SupervisorJavaClient supervisorJavaClient = new SupervisorJavaClient(gui);

        gui.setSupervisorClientObject(supervisorJavaClient);
        gui.setClientObject(client); 
	}
}
