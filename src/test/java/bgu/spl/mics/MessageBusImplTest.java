package bgu.spl.mics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MessageBusImplTest {

    private MessageBusImpl messageBus;
    private MicroService testMicroService;

    @BeforeEach
    public void setUp() {
        messageBus = MessageBusImpl.getInstance();
        testMicroService = new MicroService("TestMicroService") {
            @Override
            protected void initialize() {
                // No initialization logic needed for testing
            }
        };
        System.out.println("Setup completed for TestMicroService.");
    }

    @Test
    public void testSubscribeEvent() {
        System.out.println("Starting testSubscribeEvent...");
        class TestEvent implements Event<String> {}

        messageBus.register(testMicroService);
        System.out.println("MicroService registered: " + testMicroService.getName());

        messageBus.subscribeEvent(TestEvent.class, testMicroService);
        System.out.println("MicroService subscribed to event: TestEvent");

        TestEvent event = new TestEvent();
        Future<String> future = messageBus.sendEvent(event);
        System.out.println("Event sent: " + event);

        assertNotNull(future, "Future should not be null");
        assertDoesNotThrow(() -> {
            Message receivedMessage = messageBus.awaitMessage(testMicroService);
            System.out.println("Message received: " + receivedMessage);
            assertEquals(event, receivedMessage, "The received message should match the sent event");
        });

        messageBus.complete(event, "Success");
        System.out.println("Event completed with result: Success");
        assertTrue(future.isDone(), "Future should be resolved");
        assertEquals("Success", future.get(), "Future result should match");
        System.out.println("testSubscribeEvent completed successfully.");
    }

    @Test
    public void testSubscribeBroadcast() {
        System.out.println("Starting testSubscribeBroadcast...");
        class TestBroadcast implements Broadcast {}

        messageBus.register(testMicroService);
        System.out.println("MicroService registered: " + testMicroService.getName());

        messageBus.subscribeBroadcast(TestBroadcast.class, testMicroService);
        System.out.println("MicroService subscribed to broadcast: TestBroadcast");

        TestBroadcast broadcast = new TestBroadcast();
        messageBus.sendBroadcast(broadcast);
        System.out.println("Broadcast sent: " + broadcast);

        assertDoesNotThrow(() -> {
            Message receivedMessage = messageBus.awaitMessage(testMicroService);
            System.out.println("Message received: " + receivedMessage);
            assertEquals(broadcast, receivedMessage, "The received message should match the sent broadcast");
        });
        System.out.println("testSubscribeBroadcast completed successfully.");
    }

    @Test
    public void testRoundRobinEventDispatching() {
        System.out.println("Starting testRoundRobinEventDispatching...");
        class TestEvent implements Event<String> {}

        MicroService ms1 = new MicroService("MicroService1") {
            @Override
            protected void initialize() {}
        };
        MicroService ms2 = new MicroService("MicroService2") {
            @Override
            protected void initialize() {}
        };

        messageBus.register(ms1);
        messageBus.register(ms2);
        System.out.println("MicroServices registered: MicroService1, MicroService2");

        messageBus.subscribeEvent(TestEvent.class, ms1);
        messageBus.subscribeEvent(TestEvent.class, ms2);
        System.out.println("MicroServices subscribed to event: TestEvent");

        TestEvent event1 = new TestEvent();
        TestEvent event2 = new TestEvent();
        messageBus.sendEvent(event1);
        messageBus.sendEvent(event2);
        System.out.println("Events sent: event1, event2");

        assertDoesNotThrow(() -> {
            Message receivedByMs1 = messageBus.awaitMessage(ms1);
            System.out.println("Message received by MicroService1: " + receivedByMs1);
            assertEquals(event1, receivedByMs1, "MicroService1 should receive event1");

            Message receivedByMs2 = messageBus.awaitMessage(ms2);
            System.out.println("Message received by MicroService2: " + receivedByMs2);
            assertEquals(event2, receivedByMs2, "MicroService2 should receive event2");
        });
        System.out.println("testRoundRobinEventDispatching completed successfully.");
    }

    @Test
    public void testUnregisteredMicroServiceCannotReceiveMessages() {
        System.out.println("Starting testUnregisteredMicroServiceCannotReceiveMessages...");
        class TestBroadcast implements Broadcast {}

        TestBroadcast broadcast = new TestBroadcast();
        messageBus.sendBroadcast(broadcast);
        System.out.println("Broadcast sent: " + broadcast);

        assertThrows(IllegalStateException.class, () -> messageBus.awaitMessage(testMicroService),
                "Unregistered microservice should not be able to receive messages");
        System.out.println("testUnregisteredMicroServiceCannotReceiveMessages completed successfully.");
    }
}

