import java.util.*;

public class Message {
    String message;
    HashMap<Integer, Integer> timestamp;
    HashMap<Integer, HashMap<Integer, Integer>> prevMessageVector;
    int source;

    public Message(String message, HashMap<Integer, Integer> timestamp, HashMap<Integer, HashMap<Integer, Integer>> prevMessageVector, int source) {
        this.message = message;
        this.timestamp = timestamp;
        this.prevMessageVector = prevMessageVector;
        this.source = source;
    }
}