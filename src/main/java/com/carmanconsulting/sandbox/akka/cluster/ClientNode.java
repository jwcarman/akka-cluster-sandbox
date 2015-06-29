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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ClientNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientNode.class);
    private static final AtomicLong SENT_COUNT = new AtomicLong();
    private static final AtomicLong RCVD_COUNT = new AtomicLong();

    public static void main(String[] args) {
        Config config = ConfigFactory.load(SeedNode.class.getClassLoader(), "clients.conf");
        ActorSystem actorSystem = ActorSystem.create("EchoCluster", config);
        ActorRef mediator = DistributedPubSubExtension.get(actorSystem).mediator();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        ActorRef echoClient = actorSystem.actorOf(Props.create(EchoClient.class));
        executor.scheduleAtFixedRate(() -> {
            LOGGER.info("Sending message...");
            SENT_COUNT.incrementAndGet();
            mediator.tell(new DistributedPubSubMediator.Send("/user/echo", "Hello!"), echoClient);
        }, 5, 5, TimeUnit.SECONDS);
    }

    public static class EchoClient extends UntypedActor {
        @Override
        public void onReceive(Object message) throws Exception {
            RCVD_COUNT.incrementAndGet();

            LOGGER.info("Received message '{}' (sent/received = {}/{}).", message, SENT_COUNT.get(), RCVD_COUNT.get());

        }
    }

}
