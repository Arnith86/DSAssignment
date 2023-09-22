package javaClient;

public class JavaSupervisorClientExecutor {
    	public static void main(String[] args) {

		JavaClientGui gui = new JavaClientGui("supervisor");
        
        JavaClient client = new JavaClient(gui); 
		SupervisorJavaClient supervisorJavaClient = new SupervisorJavaClient(gui);

        gui.setSupervisorClientObject(supervisorJavaClient);
        gui.setClientObject(client); 
		//ScheduledExecutorService queueUpdater;
		// queueUpdater = Executors.newScheduledThreadPool(1);
		// // queueUpdater.scheduleWithFixedDelay(() -> javaClient.getCurrentQueue(), 0 , 500 , TimeUnit.MILLISECONDS);
		// queueUpdater.scheduleWithFixedDelay(() -> javaClient.getCurrentSupervisors(), 0 , 500 , TimeUnit.MILLISECONDS);

	}
}
