package javaClient;

public class SupervisorJavaClient extends JavaClient  {

    private String user;

    public SupervisorJavaClient(JavaClientGui gui) {
        super(gui);
        
    }

    @Override
    public void placeInQueue() {
        // TODO Auto-generated method stub
            System.out.println("placed in queue");
    }
    
}
