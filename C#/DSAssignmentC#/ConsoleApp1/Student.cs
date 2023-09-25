using System;

namespace QueueServerNameSpace{
	public class Student
	{
		private string name; 
		private int ticket; 
		private string UUID; 
		private int heartbeat; 

		public Student(string student, int ticket, string UUID)
		{
			name = student; 
			this.ticket = ticket;
			this.UUID = UUID; 
		}

		public void setHeartbeat(int heartbeat){
			this.heartbeat = heartbeat;
		}

		public void setName(string name){
			this.name = name; 
		}

		public void setTicket(int ticket){
			this.ticket = ticket;
		}

		public void setUUID(string UUID){
			this.UUID = UUID; 
		}

		public string getName() {
			return this.name;
		}
		
		public int getTicket() {
			return this.ticket;
		}

		public string getUUID(){
			return this.UUID;
		}

		public int getHeartbeat(){
			return this.heartbeat; 
		}
	
	}
}