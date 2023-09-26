package javaClient;
// this class is used to keep track of the student values

public class Students {

	private String name; 
	private int ticket; 
	
	public Students(String student, int ticket) {
		name = student;
		this.ticket = ticket; 
	}
	
	public String getName() {
		return this.name;
	}
	
	public int getTicket() {
		return this.ticket;
	}
	
}
