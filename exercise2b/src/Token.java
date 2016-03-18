import java.io.Serializable;

public class Token implements Serializable {
	public int[] TN = new int[DA_Component.NUM_PROCESSES];
	public State[] TS = new State[DA_Component.NUM_PROCESSES];

	public Token() {
		for (int j = 0; j < DA_Component.NUM_PROCESSES; j++) {
			TN[j] = 0;
			TS[j] = State.OTHER;
		}
	}
}
