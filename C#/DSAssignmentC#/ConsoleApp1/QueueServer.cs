using System.Xml;
using System.Collections.Generic;
using Newtonsoft.Json;
using NetMQ.Sockets;
using NetMQ;

namespace QueueServerNameSpace{
    static partial class QueueServer{
        public static void List()
        {
            //Dictionary that hold the queue list
            Dictionary<int, string> queueList = new Dictionary<int, string>();

            //add three diffrent element to the dictionary, dictionary.Add(x, x) to add new elements.
            queueList.Add(1, "One");
            queueList.Add(2, "Two");
            queueList.Add(3, "Three");

            static string MyDictionaryToJson(IDictionary<int, string> dict)
            {
                var x = dict.Select(d =>
                    string.Format("\"ticket\": {0}, \"name\": \"{1}\"", d.Key, string.Join(",", d.Value)));
                return "[{" + string.Join("},{", x) + "}]";
            }
            string js2 = MyDictionaryToJson(queueList);

            Console.WriteLine(js2);

            while (true)
            {
                using (var pub = new PublisherSocket())
                {
                    pub.Bind("tcp://*:5555");


                    while (true)
                    {
                        pub.SendMoreFrame("queue").SendFrame(js2); // Message


                    }
                }
            }
        }
    }
}