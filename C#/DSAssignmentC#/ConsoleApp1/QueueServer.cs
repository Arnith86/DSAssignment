using System.Xml;
using System.Collections.Generic;
using Newtonsoft.Json;
using System.Text.Json.Serialization;
using NetMQ.Sockets;
using NetMQ;
using System.Drawing.Text;
using System.Collections;

namespace QueueServerNameSpace{
    static partial class QueueServer{

        //Dictionary that hold the queue list
        static SortedDictionary<int, string> queueList = new SortedDictionary<int, string>();
        static Dictionary<string, int> heartbeatDic = new Dictionary<string, int>();
        public static void sendList()
        {
            
            //add three diffrent element to the dictionary, dictionary.Add(x, x) to add new elements.
            queueList.Add(2, "Two");
            queueList.Add(3, "Three");
            queueList.Add(1, "One");
            heartbeatDic.Add("One", 40);
            heartbeatDic.Add("Two", 400);
            heartbeatDic.Add("Three", 400);
            foreach (var kvp in queueList)
            {
                Console.WriteLine("ticket = {0}, name = {1}", kvp.Key, kvp.Value);
            }
            Console.WriteLine("-----------------------------------");
           
                Thread addToListThread = new Thread(new ThreadStart(addToList));
                addToListThread.Start();
                Thread countdownThread = new Thread(countdown);
                countdownThread.Start();
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
                        pub.SendMoreFrame("queue").SendFrame(js2);// Message
                                                                  //Console.WriteLine("lmao");
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
                    Thread removeFromListThread = new Thread(removeFromList);
                    string msg = server.ReceiveFrameString();
                    //Console.WriteLine("From Client: {0}", msg);
                    //server.SendFrame("{}");

                    
                    dynamic jsonObj = JsonConvert.DeserializeObject(msg);
                    string studentName = jsonObj.name;
                    Console.WriteLine(jsonObj);
                    if (!(queueList.ContainsValue(studentName)))
                    {
                        lock (queueList)
                        {
                            lock (heartbeatDic)
                            {
                                var maxKey = queueList.Keys.Max();
                                int newMaxKey = maxKey + 1;
                                queueList.Add(newMaxKey, studentName);
                                heartbeatDic.Add(studentName, 4);


                                server.SendFrame("{\"ticket\": " + newMaxKey + ", \"name\": \"" + studentName + "\"}");
                            }
                        }
                    }
                    else if (queueList.ContainsValue(studentName))
                    {
                        lock (heartbeatDic)
                        {
                            heartbeatDic[studentName] = 4;
                            //removeFromList(1);
                            //removeFromList(4);
                           // Console.WriteLine(studentName + " " +heartbeatDic[studentName]);
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
        public static void countdown()
        {
            Thread removeFromListThread = new Thread(removeFromList);
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
                            heartbeatDic.Remove((string)removedStudent);
                            // queueList.Remove(pa.Key);
                                    
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
        public static void removeFromList(object removedStudent)
        {

                    
        }
    }
}