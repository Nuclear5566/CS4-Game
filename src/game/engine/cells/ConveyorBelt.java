package game.engine.cells;
import game.engine.Role;

public class ConveyorBelt extends TransportCell{
	public ConveyorBelt(String name, int effect)
	{
		super(name,effect); // the effect must always be positive so does that mean we have to do
							// a validation check???
	}
}
