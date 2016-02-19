import java.rmi.*;

public interface DA_MessageHandler_RMI extends Remote {
    public void receiveMessage(String message, int[] timestamp, int[] vectorClock);
}