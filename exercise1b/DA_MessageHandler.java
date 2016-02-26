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
            int otherValue = 0;
            if (b.containsKey(entry.getKey())) otherValue = b.get(entry.getKey());

            if (entry.getValue() > otherValue) {
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
        System.out.println("considering message = " + message);

        System.out.println("vector in message:");
        printVectorVector(prevMessageVector);

        System.out.println("my clock:");
        printVector(clockVector);

        try {
            System.out.println("delivery condition: " + prevMessageVector.containsKey(id) + " and " + smallerEqual(prevMessageVector.get(id), clockVector));
        } catch (Exception e) {
            System.out.println("delivery condition (fail): " + prevMessageVector.containsKey(id));
        }

        boolean result;

        if (!prevMessageVector.containsKey(id) || (prevMessageVector.containsKey(id) && smallerEqual(prevMessageVector.get(id), clockVector))) {
            deliverMessage(message, timestamp, prevMessageVector, source, alreadyBuffered);

            result = true;
        } else {
            if (!alreadyBuffered) {
                synchronized (messageBuffer) {
                    messageBuffer.add(new Message(message, timestamp, prevMessageVector, source));
                }
            }

            result = false;
        }

        if (!alreadyBuffered) {
            // Update vector clock
            clockVector.put(id, clockVector.get(id) + 1);
        }

        System.out.println("----------------------------------------------");

        return result;
    }

    // Should only be called by receiveMessage()
    public synchronized void deliverMessage(String message, HashMap<Integer, Integer> timestamp, HashMap<Integer, HashMap<Integer, Integer>> prevMessageVector, int source, boolean isBufferedMessage) {
        System.out.println("############## delivering -> " + message + " ################");

        prevMessageVector.put(source, timestamp);

        System.out.println("updating my vector:");
        printVectorVector(sentVector);
        System.out.println("with vector in message:");
        printVectorVector(prevMessageVector);

        // Update sent messages vector
        for (Map.Entry<Integer, HashMap<Integer, Integer>> entry : prevMessageVector.entrySet()) {
            int k = entry.getKey();

            // Don't update our own sent messages vector
            if (k == id) {
                continue;
            }

            // Either copy the sent messages vector or update it to the highest values
            if (!sentVector.containsKey(k)) {
                sentVector.put(k, entry.getValue());
            } else {
                for (Map.Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
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

        System.out.println("my new vector:");
        printVectorVector(sentVector);

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

    private void printVector(HashMap<Integer, Integer> vector) {
        for (Map.Entry<Integer, Integer> entry : vector.entrySet()) {
            System.out.println("\tprocess #" + entry.getKey() + " -> " + entry.getValue());
        }
    }

    private void printVectorVector(HashMap<Integer, HashMap<Integer, Integer>> vectorVector) {
        for (Map.Entry<Integer, HashMap<Integer, Integer>> entry : vectorVector.entrySet()) {
            System.out.println("\tprocess #" + entry.getKey());

            for (Map.Entry<Integer, Integer> entry2 : entry.getValue().entrySet()) {
                System.out.println("\t\t" + entry2.getKey() + " -> " + entry2.getValue());
            }
        }
    }    

    // Helper function to send a message to a remote entity
    public synchronized void sendMessageDelayed(final String message, final int destination, final int maxDelay) {
        DA_MessageHandler_RMI remote = null;

        try {
            remote = (DA_MessageHandler_RMI) Naming.lookup("rmi://localhost:1099/" + destination);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }

        // Increment local clock
        clockVector.put(id, clockVector.get(id) + 1);

        //System.out.println("After incrementing local clock:");
        //printVector(clockVector);

        // Send message with timestamp and sent vectors
        final DA_MessageHandler_RMI remoteFin = remote;
        final HashMap<Integer, Integer> vectorCopy = clockVector;
        final HashMap<Integer, HashMap<Integer, Integer>> sentVectorCopy = sentVector;

        clockVector = new HashMap<Integer, Integer>(clockVector);
        sentVector = new HashMap<Integer, HashMap<Integer, Integer>>(sentVector);

        executor.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    /*System.out.println("Sending message with timestamp:");
                    printVector(vectorCopy);
                    System.out.println("and vector clock:");
                    printVectorVector(sentVectorCopy);*/

                    remoteFin.receiveMessage(message, vectorCopy, sentVectorCopy, id, false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }, random.nextInt(maxDelay), TimeUnit.MILLISECONDS);

        // Update sent vector to include this message
        sentVector.put(destination, vectorCopy);

        //System.out.println("After updating sent vector:");
        //printVectorVector(sentVector);
    }
}