package bgu.spl.mics;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only one public method (in addition to getters which can be public solely for unit testing) may be added to this class
 * All other methods and members you add the class must be private.
 */
public class MessageBusImpl implements MessageBus {

	// Event subscriptions
	public final ConcurrentHashMap<Class<? extends Event<?>>, ConcurrentLinkedQueue<MicroService>> eventSubscribers;

	// Broadcast subscriptions
	public final ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> broadcastSubscribers;

	// MicroService message queues
	private final ConcurrentHashMap<MicroService, BlockingQueue<Message>> microServiceQueues;

	// Futures associated with events
	private final ConcurrentHashMap<Event<?>, Future<?>> eventFutures;


	// Constructor (private for singleton pattern)
	private MessageBusImpl() {
		eventSubscribers = new ConcurrentHashMap<>();
		broadcastSubscribers = new ConcurrentHashMap<>();
		microServiceQueues = new ConcurrentHashMap<>();
		eventFutures = new ConcurrentHashMap<>();
	}

	// Singleton holder pattern for lazy initialization and thread safety
	private static class MessageBusHolder {
		private static final MessageBusImpl INSTANCE = new MessageBusImpl();
	}

	public static MessageBusImpl getInstance() {
		return MessageBusHolder.INSTANCE;
	}

	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		// Add the microservice to the list of subscribers for the given event type
		if (type != null && m != null) {
			eventSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
			eventSubscribers.get(type).add(m);
		}

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		// Add the microservice to the list of subscribers for the given broadcast type
		if (type != null && m != null) {
			broadcastSubscribers.putIfAbsent(type, new ConcurrentLinkedQueue<>());
			broadcastSubscribers.get(type).add(m);
		}

	}

	@Override
	public <T> void complete(Event<T> e, T result) {
		if (e != null && result != null) {
			Future<T> future = (Future<T>) eventFutures.get(e);
			if (future != null) {
				future.resolve(result);
			}
		}

	}

	@Override
	public void sendBroadcast(Broadcast b) {
		if (b != null) {
			ConcurrentLinkedQueue<MicroService> subscribers = broadcastSubscribers.get(b.getClass());
			if (subscribers != null) {
				for (MicroService microService : subscribers) {
					BlockingQueue<Message> queue = microServiceQueues.get(microService);
					if(queue != null) {
						try {
							queue.put(b); // Blocking operation if the queue is full
						} catch (InterruptedException ex) {
							Thread.currentThread().interrupt();
						}
					}
				}
			}
		}

	}


	@Override
	public <T> Future<T> sendEvent(Event<T> e) {
		if (e != null) {

			// Check if there are subscribers for the event type
			ConcurrentLinkedQueue<MicroService> subscribers = eventSubscribers.get(e.getClass());
			if (subscribers!= null && !subscribers.isEmpty()) {
				// Create and store the Future only if there are subscribers
				Future<T> future = new Future<>();
				eventFutures.put(e, future);

				// Use round-robin to dispatch the event
				MicroService m = subscribers.poll(); // Remove the first subscriber
				if (m != null) {
					subscribers.add(m); // Add it back to the end of the queue

					// Add the event to the subscriber's message queue
					BlockingQueue<Message> queue = microServiceQueues.get(m);
					try {
						queue.put(e); // Blocking operation if the queue is full
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}

					return future;
				}
			}
		}
		return null;
	}

	@Override
	public void register(MicroService m) {
		if (m != null) {
			microServiceQueues.putIfAbsent(m, new LinkedBlockingQueue<>());
		}

	}

	@Override
	public void unregister(MicroService m) {
		if (m != null) {
			microServiceQueues.remove(m);

			// Remove the microservice from all event subscriptions
			for (ConcurrentLinkedQueue<MicroService> queue : eventSubscribers.values()) {
				queue.remove(m);
			}

			// Remove the microservice from all broadcast subscriptions
			for (ConcurrentLinkedQueue<MicroService> queue : broadcastSubscribers.values()) {
				queue.remove(m);
			}
		}
	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		if (m == null || !microServiceQueues.containsKey(m)) {
			throw new IllegalStateException("MicroService is not registered.");
		}

		// Retrieve the MicroService's message queue
		BlockingQueue<Message> queue = microServiceQueues.get(m);

		// Take the next message (blocking until available)
		return queue.take();
	}

}
