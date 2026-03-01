package game.engine.cells;
import game.engine.Role;

public class ContaminationSock extends TransportCell {
	public ContaminationSock(String name, int effect)
	{
		super(name,effect); // the effect must always be a negative, so does that mean i have to
							// do a validation check??
	}
}
