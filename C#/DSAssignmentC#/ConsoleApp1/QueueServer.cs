using System.Xml;
using System.Collections.Generic;
using Newtonsoft.Json;
using System.Text.Json.Serialization;
using NetMQ.Sockets;
using NetMQ;
using System.Drawing.Text;
using System.Collections;
using Newtonsoft.Json.Linq;
using System.Collections.Concurrent;
using Microsoft.VisualBasic;

namespace QueueServerNameSpace{
    static partial class QueueServer{

        static string undefined = "undefined";
        static ConcurrentDictionary <string, Student> queueList = new ConcurrentDictionary<string, Student>();
        static Supervisor supervisor;
        static Student student; 
        static ConcurrentDictionary <string, Supervisor> supervisorQueue = new ConcurrentDictionary<string, Supervisor>();
        public static void sendList(IDictionary<string, Supervisor> supervisorQueue)
        {

            //add three diffrent element to the dictionary, dictionary.Add(x, x) to add new elements.

            

            lock (queueList)
            {
                // lock (heartbeatDic)
                // {
                    
                    queueList =
                    JsonConvert.DeserializeObject<ConcurrentDictionary<string, Student>>
                                         (File.ReadAllText(@"queueListSave.txt"));

                    foreach (KeyValuePair<string, Student> pair in queueList)
                    {
                        queueList[pair.Key].setHeartbeat(4);  //.setHeartbeat = 4;
                    }
                // }
            }
            student = new Student("JP", 1, "UUID3", 400);
            queueList[student.getName()+student.getUUID()] = student;
            student = new Student("Adam", 2, "UUID4", 400);
            queueList[student.getName()+student.getUUID()] = student;

            //Console.Clear();       <----------------------------------------/// THIS ROW COUSES ERRORS 
            Console.WriteLine("-----------------------------------");
            Console.WriteLine("");
            Console.WriteLine("-----------------------------------");
            // USED FOR TESTING 
            foreach (var kvp in queueList)
            {
                Console.WriteLine("ticket = "+kvp.Value.getTicket()+ " name = "+kvp.Value.getName()+ " UUID: "+kvp.Value.getUUID() );
            }
             foreach (var kvp in supervisorQueue)
            {
                Console.WriteLine("name = "+kvp.Value.getName()+ " UUID: "+kvp.Value.getUUID() );
            }
            Console.WriteLine("-----------------------------------");
            Console.WriteLine("-----------------------------------");
            Thread addToListThread = new Thread(new ThreadStart(checkRequests));
            addToListThread.Start();
            Thread countdownThread = new Thread(countdown);        
            countdownThread.Start();

            using (var pub = new PublisherSocket())
            {
                pub.Bind("tcp://*:5555");
                while (true)
                {

                    // lock (queueList)    <<-------------------------- WOULD NOT GET PAST THIS LOCK 
                    // {
                        
                        // Publishes the student queue
                        pub.SendMoreFrame("queue").SendFrame(sendStudentList());
                        // Publishes the supervisor queue
                        pub.SendMoreFrame("supervisors").SendFrame(sendSupervisorList());
                        // Publishes a message from the supervisor to the currently supervised student
                        foreach (KeyValuePair<string, Supervisor> kvp in supervisorQueue)
                        {
                            string clientName = null;
                            string supervisorMessage = null; 
                            string supervisorName = null; 

                            if (kvp.Value.getClientName() != undefined)
                            {
                                supervisorName = kvp.Key; 
                                clientName = kvp.Value.getClientName();
                                supervisorMessage = kvp.Value.getMessage();
                                pub.SendMoreFrame(clientName).SendFrame(sendSupervisorMessage(supervisorMessage, supervisorName));
                            }
                        }                                                                
                    // }
                }
            }  
        }

        public static void checkRequests()
        {
            using (var server = new ResponseSocket())
            {
                server.Bind("tcp://*:5556");
                while (true)
                {
                    string msg = server.ReceiveFrameString();
                    
                    dynamic jsonObj = JsonConvert.DeserializeObject(msg);
                    
                    // Contains entries to student/supervisor queues or heartbeat 
                    if ( jsonObj != null && jsonObj.ContainsKey("name"))
                    {
                        string name = jsonObj.name;
                        string UUID = jsonObj.clientId;

                        //////////////////////////////////////////////////////////////////////////
                        // performs entries on the student queue, or updates the heartbeat value 
                        if (!queueList.ContainsKey(name+UUID) && jsonObj != null && jsonObj.ContainsKey("enterQueue"))
                        {
                        //    lock (queueList)            //                <-------------- WOULD NOT GET PAST THIS LOCK 
                        //     {

                            Boolean nameIsInList = false;     
                            int newMaxKey;
                            foreach (var kvp in queueList)
                            {
                                if(kvp.Value.getName().Equals(name)){
                                    nameIsInList = true; 
                                }
                            }

                            if(queueList.Count() > 0 )
                            {
                                var maxKey = queueList.Count;
                                newMaxKey = maxKey + 1;   
                            } else { newMaxKey = 1; }

                            Student student = new Student(name, newMaxKey, UUID, 4);

                            if(nameIsInList == false)
                                    { student.setIsDouble(false); }
                            else { student.setIsDouble(true); }
                             
                            queueList[name+UUID] = student;
                            // HERE FOR TESTING REASONS, remove when done 
                            foreach (var kvp in queueList)
                            {
                                Console.WriteLine("key= "+kvp.Key+" ticket = "+kvp.Value.getTicket()+ " name = "+kvp.Value.getName()+ " UUID: "+kvp.Value.getUUID() );
                            }

                            server.SendFrame("{\"ticket\": " + newMaxKey + ", \"name\": \"" + name + "\"}");
                            //  Console.Clear();                      <---------------- couses an error              
                            Console.WriteLine("-----------------------------------");
                            Console.WriteLine(name + " was added to the queue");
                            Console.WriteLine("-----------------------------------");
                            foreach (var kvp in queueList)
                            {
                                Console.WriteLine("ticket = "+kvp.Value.getTicket()+" name = "+kvp.Value.getName());
                            }
                            Console.WriteLine("-----------------------------------");
                            string json = JsonConvert.SerializeObject(queueList);
                            File.WriteAllText(@".\queueListSave.txt", json);
                            // }
                        // }
                        // updates the heartbeat value 
                        } 
                        else if (jsonObj != null && queueList.ContainsKey(name+UUID))
                        {
                            queueList[name+UUID].setHeartbeat(4);
                            server.SendFrame("{}");
                        }
                        //////////////////////////////////////////////////////////////////////////
                        // performs entries on the supervisor, or updates the heartbeat value 
                        else if (jsonObj !=null && jsonObj.ContainsKey("enterSupervisorQueue"))
                        {
                            Boolean nameIsInList = false;
                            string supervisorName = jsonObj.name;
                            string isAQueueRequest = jsonObj.enterSupervisorQueue;
                            string status = jsonObj.status;
                            string id = jsonObj.clientId;   // THIS ALSO SHOW THE UUID REMOVE IT LATER
                            
                            foreach (var kvp in supervisorQueue)
                            {
                                if(kvp.Value.getName().Equals(supervisorName))
                                {
                                    nameIsInList = true; 
                                }
                            }

                            if (!supervisorQueue.ContainsKey(supervisorName+UUID) && isAQueueRequest.Equals("True"))
                            {
                                lock (supervisorQueue)
                                {                             
                                    supervisor = new Supervisor(supervisorName, status, UUID, 4);
                                    supervisorQueue[supervisorName+UUID] = supervisor;
                                    // HERE FOR TESTING REASONS, remove when done 
                                    foreach (var kvp in supervisorQueue)
                                    {
                                        Console.WriteLine("key= "+kvp.Key+" name = "+kvp.Value.getName()+ " UUID: "+kvp.Value.getUUID() );
                                    }
                                    
                                    if(nameIsInList == false)
                                        { supervisor.setIsDouble(false); }
                                    else { supervisor.setIsDouble(true); }

                                    server.SendFrame("{\r\n" + 
                                            "          \"name\": \""+supervisorName+"\",\r\n" + 
                                            "          \"status\": \""+status+"\", \r\n" + 
                                            "          }");
                                }
                            }
                        }        
                        else if (jsonObj != null && supervisorQueue.ContainsKey(name+UUID))
                        {
                            lock (supervisorQueue)
                            {
                                supervisorQueue[name+UUID].setHeartbeat(4);
                                server.SendFrame("{}");
                            }
                        }
                        else
                        {
                           server.SendFrame("{}");    
                        }
                    }
                    else if(jsonObj != null && jsonObj.ContainsKey("supervisor") && jsonObj.ContainsKey("message"))
                    {
                        string name = jsonObj.supervisor;
                        string UUID = getSupervisorUUID(name); 
                        string message = jsonObj.message;

                        if(supervisorQueue.ContainsKey(name+UUID)){
                            
                            lock (supervisorQueue)
                            {
                                supervisorQueue[name+UUID].setSupervisorMessage(message);                                        
                                server.SendFrame("{\r\n" + 
                                        "          \"supervisor\": \""+name+"\",\r\n" + 
                                        "          \"message\": \""+message+"\", \r\n" + 
                                        "          }");
                            }
                        }
                        else
                        {
                            server.SendFrame("{}");
                        }
                    }
                    else if (jsonObj != null && jsonObj.ContainsKey("statusChange")){
                        
                        string name = jsonObj.supervisor;
                        string UUID = getSupervisorUUID(name); 
                        string status = jsonObj.status;
                        
                        if(supervisorQueue.ContainsKey(name+UUID)){
                            
                            lock (supervisorQueue)
                            {

                                supervisorQueue[name+UUID].setStatus(status);                                        
                                server.SendFrame("{\r\n" + 
                                        "          \"supervisor\": \""+name+"\",\r\n" + 
                                        "          \"status\": \""+status+"\", \r\n" + 
                                        "          }");
                            }
                        }
                        else
                        {
                            server.SendFrame("{}");    
                        }
                    }
                    else if(jsonObj != null && jsonObj.ContainsKey("nextStudent"))
                    {
                        string supervisor = jsonObj.supervisor;
                        string UUID = getSupervisorUUID(supervisor); 
                        
                        if(supervisorQueue.ContainsKey(supervisor+UUID)){
                            
                        //    lock(queueList){                  <<<-------- stops the execution
                                lock(supervisorQueue){

                                    if(queueList.Count > 0 ){
                                        
                                        KeyValuePair<string, Student> firstElement = queueList.First();
                                        int studentTicket = firstElement.Value.getTicket();
                                        string studentName = firstElement.Value.getName();
                                        string studentUUID = firstElement.Value.getUUID();

                                        supervisorQueue[supervisor+UUID].setSupervising(studentName, studentTicket);
                                        
                                        queueList.TryRemove(studentName+studentUUID, out Student removedValue);
                                        // Console.WriteLine(queueList.Count);
                                        
                                            
                                        // lock(heartbeatDic){
                                        //     heartbeatDic.TryRemove(studentName, out int removedValue);
                                        //     //heartbeatDic.Remove(firstElement.Value);
                                        // }
                                        string json = JsonConvert.SerializeObject(queueList);
                                        File.WriteAllText(@".\queueListSave.txt", json);
                                        // KeyValuePair<int,string> firstElement = queueList.First();
                                        // int studentTicket = firstElement.Key;
                                        // string studentName = firstElement.Value;

                                    }               
                                //}
                            }

                            server.SendFrame("{\"supervisor\": \""+supervisor+"\", \"nextStudent\": true}");
                        }
                        else
                        {  
                            server.SendFrame("{}");
                        }
                    }
                    else 
                    {   // handles an unexpected json
                        server.SendFrame("{}");
                    }
                }
            }
        }
        
        // Checks to see if the name exist in the Dictunery, returns the UUID 
        private static string getSupervisorUUID(string name){
            string UUID = null;

            foreach (var kvp in supervisorQueue)
            {
                if(kvp.Value.getName().Equals(name))
                {
                    UUID = kvp.Value.getUUID(); 
                }
            }
            return UUID; 
        } 
        // public static void addToSupervisorList()
        // {  
        //     using (var server = new ResponseSocket())
        //     {
        //         server.Bind("tcp://*:5557");
        //         while (true)
        //         {
        //             // recives a Json as bellow :: CAN BE CHANGED
        //             // {
        //             //     "enterSupervisorQueue": true,
        //             //     "name": "<name>",
        //             //     "status": "<status>", 
        //             //     "clientId": "<unique id string>"
        //             // }
                    
        //             string request = server.ReceiveFrameString();
                    
        //             dynamic jsonObj = JsonConvert.DeserializeObject(request);
                                       
        //             if(jsonObj !=null && jsonObj.ContainsKey("enterSupervisorQueue"))
        //             {
        //                 string supervisorName = jsonObj.name;
        //                 string isAQueueRequest = jsonObj.enterSupervisorQueue;
        //                 string UUID = jsonObj.UUID;   
        //                 string status = jsonObj.status;
        //                 string id = jsonObj.clientId;   // DONT KNOW IF THIS IS GOING TO BE USED 
        //                 Boolean nameIsInList = false; 

        //                 foreach (var kvp in supervisorQueue)
        //                     {
        //                         if(kvp.Value.getName().Equals(supervisorName)){
        //                             nameIsInList = true; 
        //                         }
        //                     }

        //                 if (!supervisorQueue.ContainsKey(supervisorName) && isAQueueRequest.Equals("True"))
        //                 {
        //                     lock (supervisorQueue)
        //                     {
    
        //                         // lock (heartbeatDic)
        //                         // {
        //                             supervisor = new Supervisor(supervisorName, status, UUID, 4);
        //                             supervisorQueue[supervisorName] = supervisor;

        //                             //supervisorQueue[supervisorName+UUID].setHeartbeat(4);
        //                             //heartbeatDic.Add(supervisorName, 4);
                                    
        //                             server.SendFrame("{\r\n" + 
        //                                     "          \"name\": \""+supervisorName+"\",\r\n" + 
        //                                     "          \"status\": \""+status+"\", \r\n" + 
        //                                     "          }");
        //                         }
        //                     // }
        //                 } 
        //                 else if (supervisorQueue.ContainsKey(supervisorName))
        //                 {
        //                     lock (supervisorQueue)
        //                     {
        //                     // lock (heartbeatDic)
        //                     // {
        //                         supervisorQueue[supervisorName+UUID].setHeartbeat(4);
        //                         server.SendFrame("{}");
        //                     }
        //                 }
        //                 else
        //                 {
        //                     server.SendFrame("{}");
        //                 }
        //             } 
        //             else if(jsonObj != null && jsonObj.ContainsKey("supervisor") && jsonObj.ContainsKey("message"))
        //             {
        //                 string name = jsonObj.supervisor;
        //                 string UUID = jsonObj.UUID; 
        //                 string message = jsonObj.message;
                        
        //                 if(supervisorQueue.ContainsKey(name+UUID)){
                           
        //                     lock (supervisorQueue)
        //                     {
        //                         // lock (heartbeatDic) // IS THIS LOOK NEEDED? 
        //                         // {
        //                             supervisorQueue[name+UUID].setSupervisorMessage(message);                                        
        //                             server.SendFrame("{\r\n" + 
        //                                     "          \"supervisor\": \""+name+"\",\r\n" + 
        //                                     "          \"message\": \""+message+"\", \r\n" + 
        //                                     "          }");
        //                         //}
        //                     }
        //                 }
        //                 else
        //                 {
        //                     server.SendFrame("{}");
        //                 }
        //             }
        //             else if (jsonObj != null && jsonObj.ContainsKey("statusChange")){
                        
        //                 string name = jsonObj.supervisor;
        //                 string UUID = jsonObj.UUID; 
        //                 string status = jsonObj.status;
                        
        //                 if(supervisorQueue.ContainsKey(name+UUID)){
                           
        //                     lock (supervisorQueue)
        //                     {
        //                         // lock (heartbeatDic) // IS THIS LOOK NEEDED? 
        //                         // {
        //                             supervisorQueue[name+UUID].setStatus(status);                                        
        //                             server.SendFrame("{\r\n" + 
        //                                     "          \"supervisor\": \""+name+"\",\r\n" + 
        //                                     "          \"status\": \""+status+"\", \r\n" + 
        //                                     "          }");
        //                         // }
        //                     }
        //                 }
        //                 else
        //                 {
        //                     server.SendFrame("{}");    
        //                 }
        //             }
        //             else if(jsonObj != null && jsonObj.ContainsKey("nextStudent"))
        //             {
        //                 string supervisor = jsonObj.supervisor;
        //                 string UUID = jsonObj.UUID; 
                        
        //                 if(supervisorQueue.ContainsKey(supervisor+UUID)){
                            
        //                     lock(queueList){
        //                         lock(supervisorQueue){

        //                             if(queueList.Count > 0 ){
                                        
        //                                 KeyValuePair<string, Student> firstElement = queueList.First();
        //                                 int studentTicket = firstElement.Value.getTicket();
        //                                 string studentName = firstElement.Value.getName();
        //                                 string studentUUID = firstElement.Value.getUUID();

        //                                 supervisorQueue[supervisor].setSupervising(studentName, studentTicket);
                                       
        //                                 queueList.TryRemove(studentName+studentUUID, out Student removedValue);
        //                                 // Console.WriteLine(queueList.Count);
                                        
                                         
        //                                 // lock(heartbeatDic){
        //                                 //     heartbeatDic.TryRemove(studentName, out int removedValue);
        //                                 //     //heartbeatDic.Remove(firstElement.Value);
        //                                 // }
        //                                 string json = JsonConvert.SerializeObject(queueList);
        //                                 File.WriteAllText(@".\queueListSave.txt", json);
        //                                 // KeyValuePair<int,string> firstElement = queueList.First();
        //                                 // int studentTicket = firstElement.Key;
        //                                 // string studentName = firstElement.Value;

        //                             }               
        //                         }
        //                     }

        //                     server.SendFrame("{\"supervisor\": \""+supervisor+"\", \"nextStudent\": true}");
        //                 }
        //                 else
        //                 {  
        //                     server.SendFrame("{}");
        //                 }
        //             }
        //             else 
        //             {   // handles an unexpected json
        //                 server.SendFrame("{}");
        //             }
        //         }
        //     }
        // }

        public static void countdown()
        {
            ConcurrentDictionary <string, Student> queueListCopy;
            
            lock(queueList){
                queueListCopy = queueList;
            

            string removedStudent;
            while (true)
            {
                foreach (KeyValuePair<string, Student> pair in queueListCopy)
                {
                    if (pair.Value.getHeartbeat() <= 0)
                    {
                        // lock (queueList)
                        // {
                            // lock (heartbeatDic)
                            // {
                            removedStudent = pair.Key;
                                
                            // var itemsToRemove = queueList.Where(f => f.Key. == removedStudent).ToArray();

                            //foreach (var item in itemsToRemove)
                                queueList.TryRemove(pair.Key, out Student returnValue);
                                //heartbeatDic.TryRemove(removedStudent, out int removedValue);
                            //    Console.Clear();                               // < ------------------------COUSES AN ERROR 
                                Console.WriteLine("-----------------------------------");
                                Console.WriteLine(removedStudent + " was removed from the queue");
                                Console.WriteLine("-----------------------------------");
                                foreach (var kvp in queueList)
                                {
                                    Console.WriteLine("ticket = {0}, name = {1}", kvp.Key, kvp.Value);
                                }
                                Console.WriteLine("-----------------------------------");
                                string json = JsonConvert.SerializeObject(queueList);
                                File.WriteAllText(@".\queueListSave.txt", json);

                            //}
                        // }
                    }
                    else
                    {
                        removedStudent = pair.Key;
                        int currentHeartbeat = queueList[pair.Key].getHeartbeat() - 1;
                        queueList[pair.Key].setHeartbeat(currentHeartbeat); // = queueList[pair.Key].getHeartbeat() - 1;
                        //Console.WriteLine(removedStudent +" : "+ heartbeatDic[pair.Key]);
                    }
                }
                Thread.Sleep(1000);
                }
            }
        }

        public static string sendSupervisorMessage(string message, string supervisorName){
            
            // builds a JSON string out of the list of supervisors, as such  
            //      {
            //          "supervisor":"<name of supervisor>",
            //          "message":"<message from supervisor>"
            //      }
            
            JObject supervisorObject = new JObject(); 

            foreach (KeyValuePair<string, Supervisor> kvp in supervisorQueue)
            {
                if (kvp.Value.getName() == supervisorName)
                {
                    supervisorObject = new JObject(
                        new JProperty("name", new JValue(supervisorName)),
                        new JProperty("message", new JValue(message))
                    );        
                }
            }

            return supervisorObject.ToString(); 
        }

        public static string sendSupervisorList()
        {
            // builds a JSON string out of the list of supervisors, as such 
            //  [ 
            //    {
            //     "name": <name>, 
            //     "status": "pending"|"available"|"occupied", 
            //     "client": undefined|{
            //                           "ticket":<index>,
            //                            "name":"<name>"
            //                          }
            //    },  
            //  ]

            JArray sypervisorQueueJArray = new JArray();

            foreach (KeyValuePair<string, Supervisor> kvp in supervisorQueue)
            {
                JObject supervisorObject = new JObject(
                    new JProperty("name", new JValue(kvp.Value.getName())),
                    new JProperty("status", new JValue(kvp.Value.getStatus()))
                );

                JObject supervisorsClientObject = new JObject();
                JProperty client;

                if (kvp.Value.getClientName() != undefined)
                {
                    supervisorsClientObject.Add(new JProperty("ticket", new JValue(kvp.Value.getClientTicket())));
                    supervisorsClientObject.Add(new JProperty("name", new JValue(kvp.Value.getClientName())));
                    client = new JProperty("client", supervisorsClientObject);
                }
                else
                {
                    client = new JProperty("client", undefined);
                }

                supervisorObject.Add(client);
                sypervisorQueueJArray.Add(supervisorObject);
            }

            return sypervisorQueueJArray.ToString();

        }

            public static string sendStudentList()
            {

            // builds a JSON string out of the list of supervisors, as such
            // [ 
            //    {"ticket": <index>, "name": "<name>"}, ... 
            // ]
             
            JArray studentQueueJArray = new JArray();

            foreach (KeyValuePair<string, Student> kvp in queueList)
            {
                if(kvp.Value.getIsDouble() == false){
                
                    JObject supervisorObject = new JObject(
                        new JProperty("ticket", new JValue(queueList[kvp.Key].getTicket())),
                        new JProperty("name", new JValue(queueList[kvp.Key].getName()))
                    );

                    JObject supervisorsClientObject = new JObject();
        
                    studentQueueJArray.Add(supervisorObject);
                }
                
            }

            return studentQueueJArray.ToString();

        }

         public static void setupSupervisorQueueDic(){
            /// everything within these comments are to be removed when the supervisor client can send data instead
            supervisor = new Supervisor("Simon", "Available", "UUID1", 400);
            supervisor.setHeartbeat(4);
            supervisor.setSupervising("JP", 1);
            supervisor.setSupervisorMessage("This is a serius message with supervising instructions");
            supervisorQueue[supervisor.getName()] = supervisor;
            supervisor = new Supervisor("Erik", "Available", "UUID2", 400);
            supervisor.setHeartbeat(4);
            supervisorQueue[supervisor.getName()] = supervisor;
            
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            sendList(supervisorQueue);
        }
    }
}