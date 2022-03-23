# DistributedChatServer

## To execute the server

```java -jar DistributedChatServer-1.0-SNAPSHOT-jar-with-dependencies.jar -sid serverid -conf config_file_path```

### Distributed chat server application
Distributed chat application with multiple chat servers working together and some chat clients, 
which can run on different hosts. Chat clients can create an identity and join the main hall of any server. 
Then they can request to create, delete, join or quit a room from the connected server. 
We did two improvements on top of the basic communication protocol of this chat system.

* Leader Election - One server is assigned as the leader in this system to coordinate the chat services among the servers. When a server gets a request such as to create a new identity or to create a new chat room, that server has to ask from the leader and get approval. But this leader can be a single point of failure. If the leader server drops, the system has to select a new leader to coordinate the services. We used the fast bully algorithm [1] for this leader selection process.


* Gossipping - Then to provide eventual consistency, the newly appointed leader should have all the information of the servers. We used Adaptive Push-Then-Pull Gossip Algorithm [2] to achieve this. This algorithm provides the servers the ability to be a leader at any time since all of them have lists of all clients and chat rooms in the system.

In this system, server-server communication and server-client communication is handled using TCP connections. TCP connections between clients and servers are established and maintained throughout the client-server interaction, whereas TCP connections between servers are established and terminated as needed.

### Chat Client application can be found on [here](https://github.com/KushanChamindu/CS4262_ChatClient.git).

## References
[1] Seok-Hyoung Lee and Hoon Choi. “The Fast Bully Algorithm: For Electing a Coordinator Process in Distributed Systems”. In: Revised Papers from the International Conference on Information Networking, Wireless Communications Technologies and Network Applications-Part II. ICOIN ’02.

[2]	Gupta R., Maali A.C., and Singh Y.N.. "Adaptive Push-Then-Pull Gossip Algorithm for Scale-free Networks." arXiv preprint arXiv:1310.5985 (2013).
