package javaClient;

import org.json.JSONObject;

public class Supervisors {
	
	private String name; 
	private String status; 
	private String undefined = "undefined";
	private String studentName;
	private int studentTickit;
	private String message;  
	
	public Supervisors(String name, String status) {

		this.name = name;
		this.status = status;
		studentName = null;
		message = null;  
	}
	
	public void setSupervising(JSONObject client) {

		this.studentName = client.getString("name");
		this.studentTickit = client.getInt("ticket"); 
	}

	public void setSupervisorMessage(String message){
		this.message = message;
	}

	public void setSupervising() {
		this.studentName = undefined;
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

	public String getSupervisorMessage(){
		return message; 
	}
}