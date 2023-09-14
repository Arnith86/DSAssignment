package javaClient;

import org.json.JSONObject;

public class Supervisors {
	
	private String name; 
	private String status; 
	
	private String studentName;
	private int studentTickit; 
	
	public Supervisors(String name, String status) {

		this.name = name;
		this.status = status;
		studentName = null; 
	}
	
	public void setSupervising(JSONObject client) {

		this.studentName = client.getString("name");
		this.studentTickit = client.getInt("ticket"); 
	}

	public String getSupervisorName(){
		return name; 
	}

	public String getStudentName(){
		return studentName; 
	}

	public String getStatus(){
		return status; 
	}

	public int getTicket(){
		return studentTickit;
	}
}
