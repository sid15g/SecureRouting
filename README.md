# SecureRouting

Implemented as a research assistant, under [Prof. Haibin Zhang](https://www.csee.umbc.edu/~hbzhang/)

### About:
We describe efficient path-based Byzantine routing protocols that are secure against _fully Byzantine adversary. Our work is in sharp contrast to prior works which handle a weaker subset of Byzantine attacks. 
We provide a formal proof of correctness of our protocols which, to our knowledge, is the first of its kind. 
We implement and evaluate our protocols using DeterLab, demonstrating that our protocols are as efficient as those secure against weaker adversaries and our protocols can efficiently and correctly detect routers that fail arbitrarily.

### Prerequisites:
* Java 8
* Maven
* Distributed System

### How to execute: 
* Compile the project using Maven and `pom.xml`
* Create a folder named 'secureRouter\_lib' and copy the dependencies in this folder
* Copy the Java archive (jar) to the parent folder of 'secureRouter\_lib'
* Deploy one instance of this application, in every server
* Configure the IP addresses of the nodes/servers in the configuration
* Use `MultiKeyGenerator` class to generate Symmetric and Assymetric keys for all the nodes
* Add it to the Java archive (Assuming that the key has been distributed securely to all the nodes)
* Use the below command to run the application on every server
``` java -jar secureRouter.jar ```
