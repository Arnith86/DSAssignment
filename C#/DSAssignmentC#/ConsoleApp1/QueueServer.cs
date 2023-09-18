using System.Xml;
using System.Collections.Generic;
using Newtonsoft.Json;
using System.Text.Json.Serialization;
using NetMQ.Sockets;
using NetMQ;
using System.Drawing.Text;

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
            heartbeatDic.Add("Two", 4);
            heartbeatDic.Add("Three", 4);
            heartbeatDic.Add("One", 4);
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
                        Thread.Sleep(3);
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
                    if (!(queueList.ContainsValue(studentName)))
                    {
                        var maxKey = queueList.Keys.Max();
                        int newMaxKey = maxKey + 1;
                        queueList.Add(newMaxKey, studentName);
                        heartbeatDic.Add(studentName, 4);
                        removeFromListThread.Start(newMaxKey);

                        server.SendFrame("{\"ticket\": " + newMaxKey + ", \"name\": \"" + studentName + "\"}");
                    }
                    else if (queueList.ContainsValue(studentName))
                    {
                        heartbeatDic[studentName] = 4;
                        //removeFromList(1);
                        //removeFromList(4);
                        Console.WriteLine("test");
                        Console.WriteLine(heartbeatDic[studentName]);
                        server.SendFrame("{}");
                    }
                    else
                    {
                        server.SendFrame("{}");
                    }

                }
            }
        }

        public static void removeFromList(object keyValue)
        {
            

        Console.WriteLine("new key added to list:" + keyValue);
        
        }
    }
}