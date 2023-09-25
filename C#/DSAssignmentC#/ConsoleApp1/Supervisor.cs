using System.Dynamic;
using System.Net;
using Newtonsoft.Json.Schema;

namespace QueueServerNameSpace
{
    public class Supervisor{

        static string undefined = "undefined";
        private string UUID; 
        private string name; 
        private string status;
        private string clientName; 
        private int clientTicket; 
        private string message;
        private int heartbeat; 
 
        public Supervisor(string name, string status, string UUID){
            this.name = name;
            this.status = status;
            this.UUID = UUID; 
            clientName = undefined;    
        }

        // method is still being worked on, supposed to collect client information 
        public void setSupervising(string clientName, int clientTicket){
            this.clientName = clientName;
            this.clientTicket = clientTicket;
        } 

        public void setStatus(string status){
            this.status = status;  
        }
        public void setSupervisorMessage(string message){
            this.message = message;
        }

        public void setUUID(string UUID){
            this.UUID = UUID;
        }

        public void setHeartbeat(int heartbeat){
            this.heartbeat = heartbeat;
        }

        public string getClientName(){
            return clientName;
        }

        public int getClientTicket(){
            return clientTicket; 
        }

        public string getName(){
            return name; 
        }

        public string getStatus(){
            return status; 
        }

        public string getMessage(){
            return message; 
        }

        public string getUUID(){
            return this.UUID;
        }

        public int getHeartbeat(){
            return this.heartbeat;
        }

    }
}