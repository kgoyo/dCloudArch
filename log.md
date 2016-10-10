# Excercise Index:
- [iteration 0: Understanding the SkyCave Architecture.](#iteration-0)
- [iteration 1: Staging, and Operations in the Cloud using Docker. Integration Points.](#iteration-1)
- [iteration 2: External Services and Stability Patterns.](#iteration-2)
- [iteration 3: MongoDB and Redundancy.](#iteration-3)
- [iteration 4: Scalability, Messaging, and Session Management.](#iteration-4)
- [iteration 5: Performance.](#iteration-5)
- [iteration 6: Exam Dry Run. Sharding.](#iteration-6)

- - -
# General

## ivy stuff
 - mvnrepository.com has libraries that can be added with ivy
 - the dependency code can be copied from the site and then the dependency can be added to the ivy.xml file
 - "ant resolve" resolves the dependencies adding all the libraries to the project (in the lib folder)
 - eclipse might need to be reconfiqured
    - first refresh
    - then add the new libraries to the build path (and removing old ones if needed, they need to be removed if they are marked with red)

## sky cave general
 - 3 abstractions: player, cave and room
 - marshalling convert from internal format to one we can send with inter-process communication (IPC)
 - Point3 is a position object
 - .cpf files are used to determine which configurations to use, can set the abstractfactories
 - PlayerServant objects are created on login in the CaveServant
 - when running the cmd/daemon, the ordering of arguments are invalid

-------------------------------------------------------------------
# iteration 0
[Understanding the SkyCave Architecture](https://cs.au.dk/~baerbak/c/cloud/mandatory0.html)

## 'cave-architecture'

### local.cpf
 - set ups inter process communication
 - server is a test double, everything runs locally
 - with this configuration the ClientRequestHandler (AllTestDoubleRequestHandler) calls the invoker directly, which means we skip the step of the ServerRequestHandler and no ICP is done.

### full trace of look (getting room description) [Paper title: Communication]:
look
1. get request object with method key "GET_SHORT_ROOM_DESCRIPTION_METHOD_KEY"
2. retrieve request object
3. call requestAndAwaitReply on the ClientCommon static class, which calls sendRequestAndBlockUntillReply on the ClientRequestHandler which is an instance variable in the PlayerProxy
4. IPC with server, specifically the ServerRequestHandler created by the ServerFactory, this calls the readMessageAndDispatch method in the ServerRequestHandler
5.  parse the input into a JSON object using the parse method on the parser instance variable (instance of the JSONParser class)
6. calls handleRequest on the invoker (the invoker is gotten from the objectManager instance (instance of ObjectManager) with the getInvoker method) the request (in json format) is given as an argument
7. demarshall the jsonobject and save the information into variables. This includes: playerID, sessionId, methodKey...
8. create dispatcher with the methodKey, which dispatcher to use depends on the prefix of the methodKey, in the GET_SHORT_ROOM_DESCRIPTION the prefix is PLAYER_TYPE_PREFIX. If the prefix is valid we call the dispatch method on the dispatcher (PlayerDisPatcher class, becuase of the PLAYER_TYPE_PREFIX)
9. get the player (PlayerServant) object from the playerID and verify the session is the same
10. call method on the player depending on the method key. In this instance we call getShortRoomDescription to get the description of the current room.
11. reply is returned from the dispatcher
12. reply is returned from the invoker
13. reply is returned to the ClientRequestHandler with out.println
14. get reply and parse it, then return it
15. demarshall and get the return value and save it as a string.

*in the local.cpf configuration there is no step 4 and 5 the ClientRequestHandler calls the invoker directly*


### ant -Dcpf=local.cpf cmd

1. trace [Paper title: Trace 1]:
	- Steps betweens PlayerProxy and PlayerServant are omitted, as they are the same for all player methods as the above

	- Comparisons to broker:
	 - the PlayerProxy and PlayerProxy both implement the Player interface
	 - Marshalling creates the request objects
	 - SocketClientRequestHandler sends the information to the ServerRequestHandler
	    - in the local.cpf configuarion this role is also taken by the ClientRequestHandler
	 - The invoker handles the request by calling the dispatcher, it also demarshall the call
	 - the dispatcher calls the methods on the Servant, in a sence the Invoker in Broker is the Invoker and Dispatcher in SkyCave (the Invoker role is split into 2)

2. JSON format:
	- the json format consists of 5 attribute which are put in in the createRequestObject method of the Marshalling class
	- the attributes are:
   - player id
   - session id
   - version number
   - method key (used to determine which method the client is calling on the server)
   - parameters for the method to be called
	- the method keys are of 2 types player and cave, which are determined by the prefixes "player-" and "cave-"
	- there are also keys for the return values, which are used to determine success and errors of the operation the client is trying to execute on the server

3. Cave and Player interfaces
 - The client side implementation is just a standin for the actual implementation. Every method call to it makes it call the equivelant method on the server.
 - The Server side implementation of the player actually does the methods and alters the game state, for example the move method actually moves the player to a new room.

4. Distribution architecture
 - The ClientRequestHandler acts as both the Client- and ServerRequestHandler and  calls the invoker directly
 - it also sets up the factory that configures the server
 - the invoker and dispatcher are the normal ones
 - The Client and Server are connected via regular method calls locally

5. Test Doubles
 - FakeCaveStorage  for CaveStorage
	- hashmap of positions as keys and rooms as values
	- comes with 5 premade rooms
 - TestStubSubscriptionService for SubscriptionService
	- has the mikkel, magnus & mathilde users in a hashmap, where they can be looked up
 - TestStubWeatherService for WeatherService
	- always returns the same hardcoded data
 - NullServerRequestHandler for ServerRequestHandler
	- does nothing in every method, role is handled by ClientRequestHandler instead
 - SimpleInMemoryCache for PlayerSessionCache
	- uses hashmaps for storage
 - NullInspector for Inspector
	- does nothing in every method

### ant -Dcpf=local.cpf daemon

1. Trace
 - see the full look trace (step 4 and 5 are not omitted this time)
 - trace 1 stills hows communication between PlayerProxy and PlayerServant
 - note this configuration always logs in as Mikkel

2. Socket impl.
 - they communicate by using the socket as an Input/OutputStream
 - exceptions are not handled properly (as commented by Bærbak)
 - host and port number are gotten from the configuration files (.cpf)

## Exercise 'cave-testing'
- test cases review:
 - uses a local configuration like local.cpf with the ClientRequestHandler called LocalMethodCallClientRequestHandler

- client:
 - tests server response, valid moves should return a true boolean
 - Also tests failure cases

- server:
 - tests with multiple different users, cant have the same multiple times
 - tests all kinds of player movement

- coverage:
 - no failed login test
 - no failed logout test
 - no player session expired test
 - player equals not tested

a lot of the weak spots have got to do with connection failures, where a saboteur would be needed (implementing this is probably a learning goal)

## Exercise 'ivy'
- `ivy.xml` lists where to get the needed libraries
- `ant resolve` retrieves all the dependencies listed in `ivy.xml`
- `ant report` generates a html webpage with information about the dependencies

## Exercise 'logging'

- logging is handled by an external library, LoggerFactory.
- when the logger is gotten, the type of the class is told, so we can read what class it logged from
  - example: `logger = LoggerFactory.getLogger(StandardInvoker.class);`
- logging statements are placed in failure states.

I don't understand the setup of the log4j.properties, other than it seems it writes to a file, which is the skycave.log


## Exercise 'configuration'

- The objectManager is given a factory from the propertyreader, which reads from the .cpf file
- The objectManager contains all the objects the server needs to reference, so that they are all accesible from a single object.


## Exercise 'inspecting'

the inspector is meant for writing to something (maybe a file), so that it can be read from the outside

1. In the objectmanager the inspector could be used to specify the specific configuration of Servant, Invoker, etc.
2. "execute InspectCommand read ipc", didn't work where do I write it?
3. ???
4. TODO make in group (requires writing code)


## Exercise 'weather-client'

server needed to handle the weather method key
and call getWeather on the playerServant
playerProxy call is like the getPosition

-------------------------------------------------------------------
# iteration 1
[Staging, and Operations in the Cloud using Docker. Integration Points.](https://cs.au.dk/~baerbak/c/cloud/mandatory1.html)

## Exercise 'image-classic'

we run:
docker run hello-world

which validates the installation by contacting the hub.docker, downloading the image and starting the container
(Notes: BIOS should enable hyper-V and service is started on linux by running sudo service docker start)

containers is browsed via 'docker ps -a' and is saved by running 'docker commit [container ID] [commit name]' which we choose to 'css-14-init'.

Add entry-point.sh script, that you can invoke from outside off docker and choose role for container (daemon/cmd)

Made containers run on same network, and talk together.

## Exercise 'image-dockerfile'

WORKDIR creates dir and makes it the 'working directory'
COPY doesnt dopy folders (just content recursivley), instead use ADD
ENTRYPOINT should be in exec form ["foo", "bar"]
CMD is optional, we can run the commands specififed in the exercise without it

The images should be as slim as possible, therefore no TEST-RESULTS should be present!
Also consider if Build.src is overkill?

## Exercise 'skycave-image'

first run "docker login" to login to the hub.docker user (there is only this option for a provider)
images must be tagged before commiting with "docker tag [image-id] [reponame:tagname]"

## Docker Stuff
Enter container:
    docker exec -i -t containerNameOrID /bin/bash

-------------------------------------------------------------------
# iteration 2
[External Services and Stability Patterns](https://cs.au.dk/~baerbak/c/cloud/mandatory2.html)

## Exercise 'subscription-service'

we use the apache httpclient libraries to do http request, this was chosen because it is a very popular library.
It is also on the top ten of used external libraries listed [here](http://blog.takipi.com/the-top-100-java-libraries-in-2016-after-analyzing-47251-dependencies/).

ServerConfgurations has a list of ServerData has contains the actual server information, this list seems to always have a size of 1

in the .cpf files server addresses should not have "http://" prefix, but it MUST be added in the code later when we do the request

we might need to add more error codes to the code

if server error -> return null

## Exercise 'weather-service'

cant test the default case in our StandardWeatherService formatRegion method

timeout is default 60-70 seconds, read timeout is default infinity

maybe we dont need to pull out the values and put into a new JSONObject

## Exercise 'weather-timeout'

maybe redo HttpRequester so that it's an object we can get from objectmanager

[Httpclient timeout types](http://www.baeldung.com/httpclient-timeout):
the Connection Timeout (http.connection.timeout) – the time to establish the connection with the remote host
the Socket Timeout (http.socket.timeout) – the time waiting for data – after the connection was established; maximum time of inactivity between two data packets
the Connection Manager Timeout (http.connection-manager.timeout) – the time to wait for a connection from the connection manager/pool

dont know what "Increase availability using Nygard's 'Timeout' on the weather service integration point in the Player implementation." means

-------------------------------------------------------------------
# iteration 3
[MongoDB and Redundancy](https://cs.au.dk/~baerbak/c/cloud/mandatory3.html)

## Exercise 'mongo-storage'

docker image is created from pulling it (docker pull mongo)

database command format `db.[collection name].[command]`

every document has a unique `_id key`

find command without input returns everything

update without `$set` overrides the document other keys and values
update only updates the first it finds (using unique identifiers for the query is advised)

mount host storage on vm:

	$ docker run --name some-mongo -v /my/own/datadir:/data/db -d mongo:tag

To delete a collection, you can use the drop function. BE ADVISED! You will get NO warning prompt!

    db.collection.drop();

the MongoDB is not visible from outside sources beccause we use expose in docker, this does not publish them to the host machine.


## Excercise 'docker-compose-db'

    docker-compose -f docker-compose-db.yml up -d

## Excercise 'mongo-replica-set'

Inspiration: http://www.sohamkamani.com/blog/2016/06/30/docker-mongo-replica-set/

Create shared network (which can be viewed at `docker network ls`

    docker network create --driver bridge repl_network

Start docker containers running `mongod` with flag `--replSet "[repl-name]"`

```
docker run -p 30001:27017 --net repl_network --name mr1 -d mongo --smallfiles --noprealloc --replSet rs0
docker run -p 30002:27017 --net repl_network --name mr2 -d mongo --smallfiles --noprealloc --replSet rs0
docker run -p 30003:27017 --net repl_network --name mr3 -d mongo --smallfiles --noprealloc --replSet rs0
```

Enter the mongo shell on mr1

    docker exec -ti mr1 mongo


Setup config for `rs.initiate();`

    config = {"_id" : "rs0", "members" : [{"_id" : 0, "host" : "mr1:27017"},{"_id" : 1,"host" : "mr2:27017"},{"_id" : 2,"host" : "mr3:27017"}]}


Initiate voting by running

    rs.initiate(config);


Run the daemon on same virtual network

    docker run -ti -p 37123:37123 -v /home/amao/workspace/ivy2_docker/:/root/.ivy2 --net repl_network andreasmalling/dcloudarch_css-14 daemon -Dcpf=mongo-replica-set.cpf

### Observations:
 - You can't run `find()` on a secondary machine (unless you do it)
    docker logs -f [container-name]
 show the console output without during interactive.

when connecting to the replica set with the mongoclient we have to use the hostname defined by the replicaset
this also means that the daemon has to be run in a docker container in the same network as the mongo containers

## Excercise 'availability-failover'

    docker run -ti -v /home/amao/workspace/ivy2_docker/:/root/.ivy2 --net repl_network andreasmalling/dcloudarch_css-14 load.mongo -Dcpf=availability-failover.cpf

To get log, use tail with follow flag, and piped to grep for filtering

    tail -F skycave.log skycave.log.1 skycave.log.2 | grep "ERROR"

-------------------------------------------------------------------
# iteration 4
[Scalability, Messaging and Session Management](https://cs.au.dk/~baerbak/c/cloud/mandatory4.html)

## Exercise 'rabbitmq-requesthandler'

the rabbitmq image we use:
    docker pull rabbitmq:3.6.5-management
map to port 15672
    docker run -d -p 15672:15672 -p5672:5672 rabbitmq:3.6.5-management

we dont need to close our connections according to Henrik
