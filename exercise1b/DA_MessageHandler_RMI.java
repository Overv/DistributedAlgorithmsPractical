import java.rmi.*;
import java.util.*;

public interface DA_MessageHandler_RMI extends Remote {
    public void receiveMessage(String message, Map<Integer, Integer> timestamp, Map<Integer, Map<Integer, Integer>> prevMessageVector, int source, boolean alreadyBuffered);
}