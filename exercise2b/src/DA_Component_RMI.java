import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DA_Component_RMI extends Remote {
	public abstract void sendRequest(int source, int sequence) throws RemoteException;
	public abstract void sendToken(Token token) throws RemoteException;
}
