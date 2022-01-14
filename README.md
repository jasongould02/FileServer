# FileServer

This project was created to act as a LAN file transfer program to move files from my main computer to my storage computer.
Still a work in progress.

## Maven

This project is dependent on the the following:

```
<dependency>
	<groupId>org.json</groupId>
	<artifactId>json</artifactId>
	<version>20090211</version>
</dependency>
```

Once you are in the FileServer folder, clean the maven project:

`mvn clean`

### Building the Modules
To build each module:

Commons Module:
`mvn install -pl fileserver_commons -am`

Client Module:
`mvn install -pl fileserver_client -am`

Server Module:
`mvn install -pl fileserver_server -am`

**Note:** By building client/server modules, the compiled/built commons module should be placed in the newly built client/server module. This is my first Maven project so I am not 100% sure that my setup is proper.

