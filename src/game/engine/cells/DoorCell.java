package game.engine.cells;
import game.engine.Role;
import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.Monster;
public class DoorCell extends Cell implements CanisterModifier{
	private Role role; // Read only
	private int energy; // Read only
	private boolean activated; // Read and write
	
	public DoorCell(String name, Role role, int energy)
	{
		super(name);
		this.role = role;
		this.energy = energy;
		this.activated = false;
	}
	public Role getRole()
	{
		return this.role;
	}
	public int getEnergy()
	{
		return this.energy;
	}
	public boolean isActivated()
	{
		return this.activated;
	}
	public void setActivated(boolean flag) // does this have to be always set to automatically true or not??
	{
		this.activated = flag;
	}
	@Override
	public void Change_Energy(Monster shrek, int newEnergy) {
		// TODO Auto-generated method stub
		
	}
	  
}
 