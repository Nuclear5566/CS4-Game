package game.engine.cells;
import game.engine.Role;
public class DoorCell extends Cell{
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
	
}
