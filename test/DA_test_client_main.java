import java.rmi.*;
import java.rmi.registry.*;
import java.net.*;

public class DA_test_client_main {
    public static void main(String[] args) {
        try {
            // Access remote object
            DA_test_RMI remoteObj = (DA_test_RMI) Naming.lookup("rmi://localhost:1099/test_remote_obj");

            // Call method on remote object
            remoteObj.processMessage("Hello, world!");

            // Get value back from remote object
            int result = remoteObj.addNumbers(3, 5);
            System.out.println("result from remoteObj.addNumbers = " + result);
        } catch (RemoteException | NotBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}