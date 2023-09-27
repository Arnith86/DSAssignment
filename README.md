# DSAssignment
Jean-Paul Hanna: a21jeaha
Adam Noble: c21adano

## Client App
Language used: **_Java_** 
Package Manager: Maven
Java Version: 17

Dependencies:  

These are installed by adding the code below to the xml file pom.xml, which can be found in the root folder of the project, under ```<version>0.0.1-SNAPSHOT</version>``` but over ```</project>```.

```xml
<dependencies>
	  
    <!-- https://mvnrepository.com/artifact/org.zeromq/jeromq -->
    <dependency>
        <groupId>org.zeromq</groupId>
        <artifactId>jeromq</artifactId>
        <version>0.5.3</version>
    </dependency>
    
	<!-- https://mvnrepository.com/artifact/org.json/json -->
	<dependency>
	    <groupId>org.json</groupId>
	    <artifactId>json</artifactId>
	    <version>20230618</version>
	</dependency>

  </dependencies>
```

## Server
Language used: **_C#_** 

DotNet version 6.0 

Dependencies: 

Both NetMQ and Newtonsoft.Json were installed through the visual studio nuget package manager.
NetMQ is used to connect the c# server through zeromq.
Newtonsoft.Json is used to create and deserialize json strings.

```xml
<Project Sdk="Microsoft.NET.Sdk">

  <PropertyGroup>
    <OutputType>Exe</OutputType>
    <TargetFramework>net6.0</TargetFramework>
    <ImplicitUsings>enable</ImplicitUsings>
    <Nullable>enable</Nullable>
  </PropertyGroup>

  <ItemGroup>
    <PackageReference Include="NetMQ" Version="4.0.1.13" />
    <PackageReference Include="Newtonsoft.Json" Version="13.0.3" />
  </ItemGroup>

</Project>
```

## Configured, compiled, and executed

To be able to execute all parts of this distributed system there are three files that need to be started. first is the server. We started it using _visual studio 2022_. Just open the project and run the file ```Program.cs``` found in ```.\C#\DSAssignmentC#\ConsoleApp1```. Next up are the two different versions of the client. Both are executed using _eclips_ and running the files ```JavaClientExecutor.java``` and ```JavaSupervisorClientExecutor.java``` found in ```.\src\main\java\javaClient```. 




## -- TinyQueue API --  
## Broadcast messages

Broadcast messages are published by server to all subscribed clients using the ZMQ pub/sub pattern.

#### Queue status

Sent to all clients in response to any changes in the queue, for example new clients entering the queue or students receiving supervision. The queue status is an ordered array of Queue tickets, where the first element represent the first student in the queue.

Sent by: server
Topic: queue

```json
[ 
    {"ticket": <index>, "name": "<name>"}, ... 
]   
```


#### Supervisor status

Sent to all clients in response to any changes in the list of supervisors, for example new supervisors connecting or when the status of a supervisor changes.

Sent by: server
Topic: supervisors
```json
[ 
    {"name": <name>, "status": "pending"|"available"|"occupied", "client": undefined|{"ticket":<index>,"name":"<name>"}}, ... 
]

```

#### User messages

The server will also publish messages directly to individual users (students). These messages should received by all clients representing that user. These messages are typically indicating that it's the user's turn to receive supervision, with instructions on how/where to find the supervisor.

Sent by: server
Topic: <name of user>
```json
{
    "supervisor":"<name of supervisor>",
    "message":"<message from supervisor>"
}
```


## Request/reply messages

Requests are sent by clients with individual responses from the server.

Each request must specify both a clientId and a user name. The clientId is a unique identifier for each client, while the name may be shared between several clients. Two or more clients connected with the same name represent the same user, and thus holds a single shared place in the queue.


#### Enter queue

Indicates that a user with specified name want to enter the queue.

A single user may connect through several clients. If another client with the same name is already connected, both clients hold the same place in the queue.
Sent by: client.
Expected response from server: Queue ticket.

```json
{
    "enterQueue": true,
    "name": "<name>",
    "clientId": "<unique id string>"
}
```


#### Queue ticket

Indicates that the client with specified name and ticket has entered the queue. 
Sent by: server.

```json

{
    "ticket": <index>,
    "name": "<name>"
}
```

#### Heartbeat

All clients are expected to send a regular messages (heartbeats) to indicate that they want to maintain their plaice in the queue. Clients with a heartbeat interval larger than 4 seconds will be considered inactive, and will be removed from queue.
Send by: client.
Expected response from server: {}.

```json
{
    "name": "<name>",
    "clientId": "<unique id string>"
}
```


#### Error message

Sent in response to any client message that does not follow the specified API. The server may also use this message type to indicate other types of errors, for example invalid name strings.
Sent by: server.

```json
{
    "error": "<error type>",
    "msg": "<error description>"
}
```


### Supervisor Requests

#### Enter supervisor queue

Indicates that a supervisor with specified name, status, and possible client want to enter the supervisor queue. One of the following statuses must be entererd "pending", "available", "occupied".

Sent by: supervisor client.
Expected response from server: Supervisor queue respons.

```json
{
    "enterSupervisorQueue": true,
    "name": "<name>",
    "status": "<status>", 
    "clientId": "<unique id string>"
}
```

### Supervisor queue respons

Indicates that the supervisor with a sepecific name and status has entered the queue. 
Sent by: Server 

```json
{
    "name": "<supervisor name>",
    "status": "<status>"
}
```

### Send supervisor message

 This is the "User messages" that will be displayed for a specific user. It is sent when the supervisor whiches to add or update the message. 

Sent by: supervisor client
Expected response from server: Message changed.

```json
{
    "supervisor": "supervisor name",
    "message": "<message>"
}
```

### Message changed

Indicates that the message was added or changed, and for who.
Sent by: Server

```json
{
    "supervisor": "supervisor name",
    "message": "<message>"
}
```

### Change status 

Whenever a supervisor client regesters a change in supervisor status it sends this message to the server. The available statuses are "pending", "available", "occupied". 

Sent by: supervisor client
Expected response from server: Status Changed.

```json
{
    "statusChange": true,
    "supervisor": "<supervisor name>",
    "status": "<status>"
}
```

### Status changed

Indicates that the status for the specified supervisor has changed, to the supplied status. 

Sent by: Server

```json
{
    "supervisor": "<supervisor name>",
    "status": "<status>"
}
```

### Take on a new student

Whenever a supervisor wants to accept a new student for supervion this message is sent. 

Sent by: supervisor client
Expected response from server: Next student recived

```json
{
    "supervisor": "<supervisor name>",
    "nextStudent": true
}
```

### Next student recived

Indicates that the next student in the queue has been accepted. 

Sent by: Server 

```json
{
    "supervisor": "<supervisor name>",
    "nextStudent": true
}
```