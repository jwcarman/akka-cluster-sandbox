package com.carmanconsulting.sandbox.akka.cluster;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.contrib.pattern.DistributedPubSubExtension;
import akka.contrib.pattern.DistributedPubSubMediator;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeedNode
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SeedNode.class);

    public static void main(String[] args) {

        Config config = ConfigFactory.load(SeedNode.class.getClassLoader(), "seeds.conf");
        ActorSystem actorSystem = ActorSystem.create("EchoCluster", config);
        ActorRef mediator = DistributedPubSubExtension.get(actorSystem).mediator();
        ActorRef echo = actorSystem.actorOf(Props.create(EchoActor.class), "echo");
        mediator.tell(new DistributedPubSubMediator.Put(echo), null);
        LOGGER.info("Registered actor at {} for pub/sub.", echo.path().toStringWithoutAddress());
    }

    public static class EchoActor extends UntypedActor {
        @Override
        public void onReceive(Object message) throws Exception {
            LOGGER.info("Echoing back response '{}'...", message);
            getSender().tell(message, getSelf());
        }
    }
}
