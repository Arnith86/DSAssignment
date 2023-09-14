package javaClient;

public class Supervisors {
	
	private String name; 
	private String status; 
	
	private String studentName;
	private int studentTickit; 
	
	public Supervisors(String name, String status, String[] client) {
		// WE ARE NOT reciving any values yet
		this.name = name;
		this.status = status;
		studentName = null; 
	}
	
	public void setSupervising(String supervising, int tickit) {
		this.studentName = supervising;
		this.studentTickit = tickit; 
	}
}
