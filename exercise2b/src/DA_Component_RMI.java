import java.rmi.Remote;

public interface DA_Component_RMI extends Remote {
	public abstract void sendRequest(int source, int sequence);
	public abstract void sendToken(Token token);
}
