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

namespace QueueServerNameSpace{
    static partial class QueueServer{

        static string undefined = "undefined";
        //Dictionary that hold the queue list
        static SortedDictionary<int, string> queueList = new SortedDictionary<int, string>();
        static ConcurrentDictionary <string, int> heartbeatDic = new ConcurrentDictionary<string, int>();
        //static Dictionary<string, int> heartbeatDic = new Dictionary<string, int>();
        static Supervisor supervisor;
        static ConcurrentDictionary <string, Supervisor> supervisorQueue = new ConcurrentDictionary<string, Supervisor>();
        public static void sendList(IDictionary<string, Supervisor> supervisorQueue)
        {

            //add three diffrent element to the dictionary, dictionary.Add(x, x) to add new elements.

            

            lock (queueList)
            {
                lock (heartbeatDic)
                {
                    
                    queueList =
                    JsonConvert.DeserializeObject<SortedDictionary<int, string>>
                                         (File.ReadAllText(@".\queueListSave.txt"));

                    heartbeatDic.Clear();

                    foreach (KeyValuePair<int, string> pair in queueList)
                    {
                        heartbeatDic[pair.Value] = 4;
                    }
                }
            }
            
            // heartbeatDic.Add("One", 40);
            // heartbeatDic.Add("Two", 400);
            // heartbeatDic.Add("Three", 400);
            Console.Clear();
            Console.WriteLine("-----------------------------------");
            Console.WriteLine("");
            Console.WriteLine("-----------------------------------");
            foreach (var kvp in queueList)
            {
                Console.WriteLine("ticket = {0}, name = {1}", kvp.Key, kvp.Value);
            }
            Console.WriteLine("-----------------------------------");
            Thread addToListThread = new Thread(new ThreadStart(addToList));
            addToListThread.Start();
            Thread countdownThread = new Thread(countdown);
            countdownThread.Start();
            Thread addToSupervisorListThread = new Thread(addToSupervisorList);
            addToSupervisorListThread.Start();

            using (var pub = new PublisherSocket())
            {
                pub.Bind("tcp://*:5555");
                while (true)
                {

                    lock (queueList)
                    {
                        static string MyDictionaryToJson(IDictionary<int, string> dict)
                        {
                            //Thread.Sleep(3);
                            var x = dict.Select(d =>
                                    string.Format("\"ticket\": {0}, \"name\": \"{1}\"", d.Key, string.Join(",", d.Value)));
                            return "[{" + string.Join("},{", x) + "}]";
                        }
                        string js2 = MyDictionaryToJson(queueList);

                        // Publishes the student queue
                        pub.SendMoreFrame("queue").SendFrame(js2);
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
                    }
                }
            }  
        }

        public static void addToList()
        {
            using (var server = new ResponseSocket())
            {
                server.Bind("tcp://*:5556");
                while (true)
                {
                    string msg = server.ReceiveFrameString();
                    
                    dynamic jsonObj = JsonConvert.DeserializeObject(msg);
                    string studentName = jsonObj.name;
                    if (!(queueList.ContainsValue(studentName)) && jsonObj != null && jsonObj.ContainsKey("enterQueue"))
                    {
                        lock (queueList)
                        {
                            lock (heartbeatDic)
                            {
                                int newMaxKey;
                                
                                if(queueList.Count() > 0 ){
                                    var maxKey = queueList.Keys.Max();
                                    newMaxKey = maxKey + 1;   
                                } else { newMaxKey = 1; }
                                
                                queueList.Add(newMaxKey, studentName);
                                heartbeatDic.AddOrUpdate(studentName, 4, (existingKey, existingValue) => 4);
                                //heartbeatDic.AddOrUpdate<> (studentName, 4); 

                                server.SendFrame("{\"ticket\": " + newMaxKey + ", \"name\": \"" + studentName + "\"}");
                                Console.Clear();
                                Console.WriteLine("-----------------------------------");
                                Console.WriteLine(studentName + " was added to the queue");
                                Console.WriteLine("-----------------------------------");
                                foreach (var kvp in queueList)
                                {
                                    Console.WriteLine("ticket = {0}, name = {1}", kvp.Key, kvp.Value);
                                }
                                Console.WriteLine("-----------------------------------");
                                string json = JsonConvert.SerializeObject(queueList);
                                File.WriteAllText(@".\queueListSave.txt", json);
                            }
                        }
                    }
                    else if (queueList.ContainsValue(studentName))
                    {
                        lock (heartbeatDic)
                        {
                            heartbeatDic[studentName] = 4;
                            server.SendFrame("{}");
                        }
                    }
                    else
                    {
                        server.SendFrame("{}");
                    }
                }
            }
        }

        public static void addToSupervisorList()
        {  
            using (var server = new ResponseSocket())
            {
                server.Bind("tcp://*:5557");
                while (true)
                {
                    // recives a Json as bellow :: CAN BE CHANGED
                    // {
                    //     "enterSupervisorQueue": true,
                    //     "name": "<name>",
                    //     "status": "<status>", 
                    //     "clientId": "<unique id string>"
                    // }
                    
                    string request = server.ReceiveFrameString();
                    
                    dynamic jsonObj = JsonConvert.DeserializeObject(request);
                                       
                    if(jsonObj !=null && jsonObj.ContainsKey("enterSupervisorQueue"))
                    {
                        string supervisorName = jsonObj.name;
                        string isAQueueRequest = jsonObj.enterSupervisorQueue;  
                        string status = jsonObj.status;
                        string id = jsonObj.clientId;   // DONT KNOW IF THIS IS GOING TO BE USED 
                        
                        if (!supervisorQueue.ContainsKey(supervisorName) && isAQueueRequest.Equals("True"))
                        {
                            lock (supervisorQueue)
                            {
                                lock (heartbeatDic)
                                {
                                    supervisor = new Supervisor(supervisorName, status);
                                    supervisorQueue[supervisorName] = supervisor;

                                    heartbeatDic.AddOrUpdate(supervisorName, 4, (existingKey, existingValue) => 4);
                                    //heartbeatDic.Add(supervisorName, 4);
                                    
                                    server.SendFrame("{\r\n" + 
                                            "          \"name\": \""+supervisorName+"\",\r\n" + 
                                            "          \"status\": \""+status+"\", \r\n" + 
                                            "          }");
                                }
                            }
                        } 
                        else if (supervisorQueue.ContainsKey(supervisorName))
                        {
                            lock (heartbeatDic)
                            {
                                heartbeatDic[supervisorName] = 4;
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
                        string message = jsonObj.message;
                        
                        if(supervisorQueue.ContainsKey(name)){
                           
                            lock (supervisorQueue)
                            {
                                lock (heartbeatDic) // IS THIS LOOK NEEDED? 
                                {
                                    supervisorQueue[name].setSupervisorMessage(message);                                        
                                    server.SendFrame("{\r\n" + 
                                            "          \"supervisor\": \""+name+"\",\r\n" + 
                                            "          \"message\": \""+message+"\", \r\n" + 
                                            "          }");
                                }
                            }
                        }
                        else
                        {
                            server.SendFrame("{}");
                        }
                    }
                    else if (jsonObj != null && jsonObj.ContainsKey("statusChange")){
                        
                        string name = jsonObj.supervisor;
                        string status = jsonObj.status;
                        
                        if(supervisorQueue.ContainsKey(name)){
                           
                            lock (supervisorQueue)
                            {
                                lock (heartbeatDic) // IS THIS LOOK NEEDED? 
                                {
                                    supervisorQueue[name].setStatus(status);                                        
                                    server.SendFrame("{\r\n" + 
                                            "          \"supervisor\": \""+name+"\",\r\n" + 
                                            "          \"status\": \""+status+"\", \r\n" + 
                                            "          }");
                                }
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
                        
                        if(supervisorQueue.ContainsKey(supervisor)){
                            
                            lock(queueList){
                                lock(supervisorQueue){

                                    if(queueList.Count > 0 ){
                                        KeyValuePair<int,string> firstElement = queueList.First();
                                        int studentTicket = firstElement.Key;
                                        string studentName = firstElement.Value;
                                        
                                        supervisorQueue[supervisor].setSupervising(studentName, studentTicket);
                                       
                                        queueList.Remove(firstElement.Key);
                                        // Console.WriteLine(queueList.Count);
                                        
                                        lock(heartbeatDic){
                                            heartbeatDic.TryRemove(studentName, out int removedValue);
                                            //heartbeatDic.Remove(firstElement.Value);
                                        }
                                        string json = JsonConvert.SerializeObject(queueList);
                                        File.WriteAllText(@".\queueListSave.txt", json);
                                        // KeyValuePair<int,string> firstElement = queueList.First();
                                        // int studentTicket = firstElement.Key;
                                        // string studentName = firstElement.Value;

                                    }               
                                }
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

        public static void countdown()
        {
            string removedStudent;
            while (true)
            {
                foreach (KeyValuePair<string, int> pair in heartbeatDic)
                {
                    if (pair.Value <= 0)
                    {
                        lock (queueList)
                        {
                            lock (heartbeatDic)
                            {
                            removedStudent = pair.Key;
                                
                            var itemsToRemove = queueList.Where(f => f.Value == removedStudent).ToArray();
                            foreach (var item in itemsToRemove)
                                queueList.Remove(item.Key);
                                heartbeatDic.TryRemove(removedStudent, out int removedValue);
                                Console.Clear();
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

                            }
                        }
                    }
                    else
                    {
                        removedStudent = pair.Key;
                        heartbeatDic[pair.Key] = heartbeatDic[pair.Key] - 1;
                        //Console.WriteLine(removedStudent +" : "+ heartbeatDic[pair.Key]);
                    }
                }
                Thread.Sleep(1000);
            }
        }

        // THIS IS EMPTY if we are not going to use it, remove it.
        public static void removeFromList(object removedStudent)
        {

                    
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
                    new JProperty("name", new JValue(kvp.Key)),
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

         public static void setupSupervisorQueueDic(){
            /// everything within these comments are to be removed when the supervisor client can send data instead
            supervisor = new Supervisor("Simon", "Available");
            supervisor.setSupervisorMessage("This is a serius message with supervising instructions");
            supervisorQueue[supervisor.getName()] = supervisor;
            supervisor = new Supervisor("Erik", "Available");
            supervisorQueue[supervisor.getName()] = supervisor;
            
            /////////////////////////////////////////////////////////////////////////////////////////////////////////
            sendList(supervisorQueue);
        }
    }
}