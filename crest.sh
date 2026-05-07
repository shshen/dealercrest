#/bin/bash

DEALER_HOME=$(dirname "${BASH_SOURCE[0]}")
cd $DEALER_HOME

ACME="./libs/acme"
CLASSPATH=$ACME/acme4j-client-3.5.1.jar:$ACME/bcpkix-jdk18on-1.84.jar:$ACME/bcprov-jdk18on-1.84.jar:$ACME/jose4j-0.9.6.jar
CLASSPATH=$CLASSPATH:./target/dealercrest-0.8.2.jar:./libs/netty-all-4.2.9.jar:./libs/resolver-4.2.9.Final.jar
CLASSPATH=$CLASSPATH:./libs/hikaricp-7.0.2.jar:./libs/postgresql-42.7.10.jar:./libs/slf4j-nop-2.0.17.jar:./libs/slf4j-api-2.0.17.jar

java -cp $CLASSPATH com.dealercrest.DealerCliMain $@