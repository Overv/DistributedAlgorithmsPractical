import java.rmi.*;
import java.util.*;

public interface DA_MessageHandler_RMI extends Remote {
    public abstract boolean receiveMessage(String message, HashMap<Integer, Integer> timestamp, HashMap<Integer, HashMap<Integer, Integer>> prevMessageVector, int source, boolean alreadyBuffered) throws RemoteException;
}