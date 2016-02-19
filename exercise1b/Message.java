import java.util.*;

public class Message {
	String message;
	Map<Integer, Integer> timestamp;
	Map<Integer, Map<Integer, Integer>> prevMessageVector;
	int source;

	public Message(String message, Map<Integer, Integer> timestamp, Map<Integer, Map<Integer, Integer>> prevMessageVector, int source) {
		this.message = message;
		this.timestamp = timestamp;
		this.prevMessageVector = prevMessageVector;
		this.source = source;
	}
}