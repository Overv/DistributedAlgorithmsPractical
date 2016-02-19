import java.rmi.*;
import java.rmi.registry.*;
import java.net.*;

public class Test {
    public static void main(String[] args) {
        try {
            // Set up registry
            LocateRegistry.createRegistry(1099);

            // Test
            DA_MessageHandler obj1 = new DA_MessageHandler(1);
            DA_MessageHandler obj2 = new DA_MessageHandler(2);

            obj2.sendMessageDelayed("first message", 1, 1000);
            obj2.sendMessageDelayed("second message", 1, 10);
        } catch (RemoteException | AlreadyBoundException | MalformedURLException e) {
            e.printStackTrace();
        }
    }
}