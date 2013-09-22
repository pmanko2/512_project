This file might be useful, we can each add the commands specific to our file systems so that
we can copy/paste quickly while running testing.

Nicolas

Run Server:

java -Djava.security.policy=server.policy -Djava.rmi.server.codebase=file:/home/2011/nwebst1/comp512/servercode/ ResImpl.ResourceManagerImpl

Run Middleware:

java -Djava.security.policy=middle.policy -Djava.rmi.server.codebase=file:/home/2011/nwebst1/comp512/middleware/ MiddlewareImpl

Run Client:
java -Djava.security.policy=java.policy client teaching

