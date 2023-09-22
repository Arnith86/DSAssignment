package javaClient;

import java.util.concurrent.ScheduledExecutorService;

public class JavaClientExecutor {
 
    	public static void main(String[] args) {

		JavaClientGui gui = new JavaClientGui("student");

		JavaClient javaClient = new JavaClient(gui);
		ScheduledExecutorService queueUpdater;
		// queueUpdater = Executors.newScheduledThreadPool(1);
		// // queueUpdater.scheduleWithFixedDelay(() -> javaClient.getCurrentQueue(), 0 , 500 , TimeUnit.MILLISECONDS);
		// queueUpdater.scheduleWithFixedDelay(() -> javaClient.getCurrentSupervisors(), 0 , 500 , TimeUnit.MILLISECONDS);

	}
}
