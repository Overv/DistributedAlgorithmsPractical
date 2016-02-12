import java.rmi.*;
import java.rmi.server.UnicastRemoteObject;

public class DA_test extends UnicastRemoteObject implements DA_test_RMI {
    public DA_test() throws RemoteException {
        super();
    }

    public synchronized void processMessage(String message) {
        System.out.println("got message: " + message);
    }

    public int addNumbers(int a, int b) {
        return a + b;
    }
}