import java.rmi.*;
import java.rmi.registry.*;
import java.net.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Set up registry
            LocateRegistry.createRegistry(1099);

            // Create sites
            DA_Component site0 = new DA_Component(0);
            DA_Component site1 = new DA_Component(1);

            // Test request
            site0.requestToken();
            site0.requestToken();
            site1.requestToken();
            site0.requestToken();
        } catch (RemoteException | AlreadyBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}