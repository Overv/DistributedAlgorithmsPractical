import java.rmi.*;

public interface DA_test_RMI extends Remote {
    public void processMessage(String message) throws RemoteException;

    public int addNumbers(int a, int b) throws RemoteException;
}