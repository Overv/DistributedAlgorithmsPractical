import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class DA_Component extends UnicastRemoteObject implements DA_Component_RMI {
	public static final int NUM_PROCESSES = 2;

	private int[] N = new int[NUM_PROCESSES];
	private State[] S = new State[NUM_PROCESSES];

	private Token token = null;

	int i;

	public DA_Component(int i) throws MalformedURLException, RemoteException, AlreadyBoundException {
		this.i = i;

		Naming.bind("rmi://localhost:1099/" + i, this);

		if (i == 0) {
			S[0] = State.HOLDING;

			for (int j = 1; j < NUM_PROCESSES; j++) {
				S[j] = State.OTHER;
			}

			token = new Token();
		} else {
			for (int j = 0; j < NUM_PROCESSES; j++) {
				if (j <= i - 1) {
					S[j] = State.REQUESTING;
				} else {
					S[j] = State.OTHER;
				}
			}
		}
	}

	public DA_Component_RMI getP(int j) {
		try {
			return (DA_Component_RMI) Naming.lookup("rmi://localhost:1099/" + j);
		} catch (MalformedURLException | RemoteException | NotBoundException e) {
			System.err.println("something went wrong");
			return null;
		}
	}

	// Requesting access to the CS
	public void requestToken() {
		if (S[i] == State.HOLDING) {
			getP(i).sendRequest(i, N[i]);
		}

		S[i] = State.REQUESTING;
		N[i]++;

		for (int j = 0; j < NUM_PROCESSES; j++) {
			if (j != i && S[j] == State.REQUESTING) {
				getP(j).sendRequest(i, N[i]);
			}
		}
	}

	// Receiving a REQUEST message
	@Override
	public void sendRequest(int j, int r) {
		switch (S[i]) {
		case EXECUTING:
		case OTHER:
			S[j] = State.REQUESTING;
			break;

		case REQUESTING:
			if (S[j] != State.REQUESTING) {
				S[j] = State.REQUESTING;
				getP(j).sendRequest(i, N[i]);
			}
			break;

		case HOLDING:
			S[j] = State.REQUESTING;
			S[i] = State.OTHER;

			token.TS[j] = State.REQUESTING;
			token.TN[j] = r;

			getP(j).sendToken(token);

			this.token = null;
		}
	}

	// Receiving the token
	@Override
	public void sendToken(Token token) {
		this.token = token;

		S[i] = State.EXECUTING;
		runCritical();

		S[i] = State.OTHER;
		token.TS[i] = State.OTHER;

		for (int j = 0; j < NUM_PROCESSES; j++) {
			if (N[j] > token.TN[j]) {
				token.TN[j] = N[j];
				token.TS[j] = S[j];
			} else {
				N[j] = token.TN[j];
				S[j] = token.TS[j];
			}
		}

		boolean allOther = true;

		for (int j = 0; j < NUM_PROCESSES; j++) {
			allOther &= S[j] == State.OTHER;
		}

		if (allOther) {
			S[i] = State.HOLDING;
		} else {
			for (int j = 0; j < NUM_PROCESSES; j++) {
				if (S[j] == State.REQUESTING) {
					getP(j).sendToken(token);
					this.token = null;
					break;
				}
			}
		}
	}

	public void runCritical() {
		System.out.println("Process " + i + " is running the critical section!");
	}
}
