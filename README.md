# SecureRouting

Implemented as a research assistant, under [Prof. Haibin Zhang](https://www.csee.umbc.edu/~hbzhang/)

### About:
To define an efficient path-based Byzantine routing protocols that are secure against _fully_ Byzantine adversary. Our work is in sharp contrast to prior works which handle a weaker subset of Byzantine attacks. We implement and evaluate our protocols using [DeterLab](https://www.isi.deterlab.net/index.php3), demonstrating that our protocols are as efficient as those secure against weaker adversaries and our protocols can efficiently and correctly detect routers that fail arbitrarily.

### Prerequisites:
* Java 8
* Maven
* Distributed evironment

### How to execute: 
* Compile the project using Maven and `pom.xml`
* Create a folder named 'secureRouter\_lib' and copy the dependencies in this folder
* Copy the Java archive (jar) to the parent folder of 'secureRouter\_lib'
* Deploy one instance of this application, in every server
* Configure the IP addresses of the nodes/servers in the configuration
* Use `edu.umbc.bft.beans.crypto.MultiKeygenerator` class to generate Symmetric and Asymmetric keys for all the nodes
* Add it to the Java archive (Assuming that the key has been distributed securely to all the nodes)
* Use the below command to run the application on every server
``` java -jar secureRouter.jar ```

### [Publication](https://ieeexplore.ieee.org/document/8548163)
* Published in `2018 IEEE 17th International Symposium on Network Computing and Applications (NCA)`
