package game.engine.cells;
import game.engine.Role;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;

public class ContaminationSock extends TransportCell implements CanisterModifier {
	public ContaminationSock(String name, int effect)
	{
		super(name,effect); // the effect must always be a negative, so does that mean i have to
							// do a validation check??
	}
	public void Change_Energy(Monster shrek) {  
		return;
	} 
}
