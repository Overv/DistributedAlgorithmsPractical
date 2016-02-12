import java.rmi.*;
import java.rmi.registry.*;
import java.net.*;

public class DA_test_server_main {
    public static void main(String[] args) {
        try {
            // Set up remote object and expose access to it
            DA_test remoteObj = new DA_test();
            Naming.bind("rmi://localhost:1099/test_remote_obj", remoteObj);
        } catch (RemoteException | AlreadyBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}