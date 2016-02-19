import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class DA_MessageHandler extends UnicastRemoteObject implements DA_MessageHandler_RMI {
    private static int processCount = 5;

    private Map<Integer, Integer> clockVector;
    private Map<Integer, Map<Integer, Integer>> sentVector;

    private List<Message> messageBuffer;

    private int id;

    public DA_MessageHandler(int id) throws RemoteException {
        super();

        clockVector = new HashMap<Integer, Integer>();
        clockVector.put(id, 0);

        sentVector = new HashMap<Integer, HashMap<Integer, Integer>>();

        messageBuffer = new ArrayList<Message>();

        this.id = id;
    }

    // Called on a remote entity to send it a message
    public synchronized void receiveMessage(String message, Map<Integer, Integer> timestamp, Map<Integer, Map<Integer, Integer>> prevMessageVector, int source, boolean alreadyBuffered) {
        // TODO: Implement smaller than check
        if (prevMessageVector.contains(id) && prevMessageVector.get(id) < clockVector) {
            if (!alreadyBuffered) {
                messageBuffer.add(new Message(message, timestamp, prevMessageVector, source));
            }
        } else {
            deliverMessage(message, timestamp, prevMessageVector, source);
        }
    }

    // Should only be called by receiveMessage()
    public void deliverMessage(String message, Map<Integer, Integer> timestamp, Map<Integer, Map<Integer, Integer>> prevMessageVector, int source) {
        // Update sent messages vector
        for (Map.Entry<Integer, Map<Integer, Integer>> entry : sentVector.entrySet()) {
            int k = entry.getKey();

            // Don't update our own sent messages vector
            if (k == id) {
                continue;
            }

            // Either copy the sent messages vector or update it to the highest values
            if (!sentVector.contains(k) && prevMessageVector.contains(k)) {
                sentVector.put(k, prevMessageVector.get(k));
            } else if (sentVector.contains(k) && prevMessageVector.contains(k)) {
                for (Map.Entry<Integer, Integer> entry2 : prevMessageVector.get(k)) {
                    if (sentVector.get(k).contains(entry2.getKey())) {
                        int max = Math.max(
                            sentVector.get(k).get(entry2.getKey()),
                            entry2.getValue();
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

        // Try delivering buffered messages
        for (Message m : messageBuffer) {
            // TODO: Remove message from buffer
            receiveMessage(m.message, m.clockVector, m.sentVector, m.id, true);
        }
    }

    // Helper function to send a message to a remote entity
    public synchronized void sendMessage(String message, int destination) {
        // Increment local clock
        clockVector.put(id, clockVector.get(id) + 1);

        // Send message with timestamp and sent vectors
        remote.receiveMessage(message, clockVector, sentVector, id, false);

        // Update sent vector to include this message
        sentVector.put(id, clockVector.clone());
    }
}