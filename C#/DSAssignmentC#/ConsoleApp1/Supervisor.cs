using System.Dynamic;
using System.Net;
using Newtonsoft.Json.Schema;

namespace QueueServerNameSpace
{
    public class Supervisor{

        static string undefined = "undefined";
        private string name; 
        private string status;
        private string clientName; 
        private int clientTicket; 

        private string message; 
 
        public Supervisor(string name, string status){
            this.name = name;
            this.status = status;
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

    }
}