using System.Xml;
using System.Collections.Generic;
using Newtonsoft.Json;
using System.Text.Json.Serialization;
using NetMQ.Sockets;
using NetMQ;

namespace QueueServerNameSpace{
    static partial class QueueServer{

        //Dictionary that hold the queue list
        static SortedDictionary<int, string> queueList = new SortedDictionary<int, string>();
        public static void sendList()
        {
            
            //add three diffrent element to the dictionary, dictionary.Add(x, x) to add new elements.
            queueList.Add(2, "Two");
            queueList.Add(3, "Three");
            queueList.Add(1, "One");
            foreach (var kvp in queueList)
            {
                Console.WriteLine("ticket = {0}, name = {1}", kvp.Key, kvp.Value);
            }
            Console.WriteLine("-----------------------------------");
           
                Thread addToListThread = new Thread(new ThreadStart(addToList));
                addToListThread.Start();
                using (var pub = new PublisherSocket())
                {
                    pub.Bind("tcp://*:5555");
                    while (true)
                    {


                        static string MyDictionaryToJson(IDictionary<int, string> dict)
                        {

                            var x = dict.Select(d =>
                                string.Format("\"ticket\": {0}, \"name\": \"{1}\"", d.Key, string.Join(",", d.Value)));
                            return "[{" + string.Join("},{", x) + "}]";
                        }
                        string js2 = MyDictionaryToJson(queueList);
                        pub.SendMoreFrame("queue").SendFrame(js2);// Message
                        Console.WriteLine("lmao");

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
                    Console.WriteLine("From Client: {0}", msg);
                    server.SendFrame("{}");

                    dynamic jsonObj = JsonConvert.DeserializeObject(msg);
                    string studentName = jsonObj.name;
                    if (!(queueList.ContainsValue(studentName)))
                    {
                        var maxKey = queueList.Keys.Max();
                        int newMaxKey = maxKey + 1;
                        queueList.Add(newMaxKey, studentName);
                        removeFromListThread.Start(newMaxKey);
                        
                    }
                    else
                    {
                        Console.WriteLine(studentName + " was already in the list");
                    }
                }
            }
        }

        public static void removeFromList(object keyValue)
        {
            //heartbeat listener, plan is to start a thread/algorithm that checks every index for a match from heartbeat.
            //if no match is found after 4 seconds delete the index

            //removes object with key 1 from ditonary
            //queueList.Remove(1);

            //foreach(KeyValuePair<int, string> entry in  queueList)
            //{
            //    Console.WriteLine(entry.Value);
            // }
            Console.WriteLine("new key added to list:" + keyValue);
        }
    }
}