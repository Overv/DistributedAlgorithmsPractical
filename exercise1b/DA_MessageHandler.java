import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;

public class DA_MessageHandler extends UnicastRemoteObject implements DA_MessageHandler_RMI {
    private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(20);
    private static final Random random = new Random();

    private HashMap<Integer, Integer> clockVector;
    private HashMap<Integer, HashMap<Integer, Integer>> sentVector;

    private List<Message> messageBuffer;

    private int id;

    private static boolean smallerEqual(HashMap<Integer, Integer> a, HashMap<Integer, Integer> b) {
        for (Map.Entry<Integer, Integer> entry : a.entrySet()) {
            if (b.containsKey(entry.getKey()) && entry.getValue() <= b.get(entry.getKey())) {
                return false;
            }
        }

        return true;
    }

    public DA_MessageHandler(int id) throws RemoteException, AlreadyBoundException, MalformedURLException {
        super();

        clockVector = new HashMap<Integer, Integer>();
        clockVector.put(id, 0);

        sentVector = new HashMap<Integer, HashMap<Integer, Integer>>();

        messageBuffer = new ArrayList<Message>();

        this.id = id;

        // Register itself
        Naming.bind("rmi://localhost:1099/" + id, this);
    }

    // Called on a remote entity to send it a message
    // Returns boolean indicating if the message has been delivered right away
    @Override
    public synchronized boolean receiveMessage(String message, HashMap<Integer, Integer> timestamp, HashMap<Integer, HashMap<Integer, Integer>> prevMessageVector, int source, boolean alreadyBuffered) {
        if (prevMessageVector.containsKey(id) && smallerEqual(prevMessageVector.get(id), clockVector)) {
            if (!alreadyBuffered) {
                synchronized (messageBuffer) {
                    messageBuffer.add(new Message(message, timestamp, prevMessageVector, source));
                }
            }

            return false;
        } else {
            deliverMessage(message, timestamp, prevMessageVector, source, alreadyBuffered);

            return true;
        }
    }

    // Should only be called by receiveMessage()
    public synchronized void deliverMessage(String message, HashMap<Integer, Integer> timestamp, HashMap<Integer, HashMap<Integer, Integer>> prevMessageVector, int source, boolean isBufferedMessage) {
        System.out.println("(" + id + ") got message: " + message);

        // Update sent messages vector
        for (Map.Entry<Integer, HashMap<Integer, Integer>> entry : sentVector.entrySet()) {
            int k = entry.getKey();

            // Don't update our own sent messages vector
            if (k == id) {
                continue;
            }

            // Either copy the sent messages vector or update it to the highest values
            if (!sentVector.containsKey(k) && prevMessageVector.containsKey(k)) {
                sentVector.put(k, prevMessageVector.get(k));
            } else if (sentVector.containsKey(k) && prevMessageVector.containsKey(k)) {
                for (Map.Entry<Integer, Integer> entry2 : prevMessageVector.get(k).entrySet()) {
                    if (sentVector.get(k).containsKey(entry2.getKey())) {
                        int max = Math.max(
                            sentVector.get(k).get(entry2.getKey()),
                            entry2.getValue()
                        );

                        sentVector.get(k).put(entry2.getKey(), max);
                    } else {
                        sentVector.get(k).put(entry2.getKey(), entry2.getValue());
                    }
                }
            }
        }

        // Update vector clock
        clockVector.put(id, clockVector.get(id) + 1);

        // If this is a new message, try delivering the previously buffered messages
        if (!isBufferedMessage) {
            synchronized (messageBuffer) {
                List<Message> unprocessedMessages = new ArrayList<Message>();

                for (Message m : messageBuffer) {
                    boolean delivered = receiveMessage(m.message, m.timestamp, m.prevMessageVector, m.source, true);

                    if (!delivered) {
                        unprocessedMessages.add(m);
                    }
                }

                // Remove the delivered messages from the buffer
                messageBuffer = unprocessedMessages;
            }
        }
    }

    // Helper function to send message with a random delay up to maxDelay milliseconds
    public void sendMessageDelayed(final String message, final int destination, final int maxDelay) {
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                sendMessage(message, destination);
            }
        }, random.nextInt(maxDelay), TimeUnit.MILLISECONDS);
    }

    // Helper function to send a message to a remote entity
    public synchronized void sendMessage(String message, int destination) {
        DA_MessageHandler_RMI remote = null;

        try {
            remote = (DA_MessageHandler_RMI) Naming.lookup("rmi://localhost:1099/" + destination);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }

        // Increment local clock
        clockVector.put(id, clockVector.get(id) + 1);

        // Send message with timestamp and sent vectors
        try {
            remote.receiveMessage(message, clockVector, sentVector, id, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        // Update sent vector to include this message
        sentVector.put(id, new HashMap<Integer,Integer>(clockVector));
    }
}