package game.engine.monsters;
import game.engine.Role;
public abstract class Monster implements Comparable<Monster> {
	private String name; // Read Only
	private String description; // Read only
	private Role role; // Read and Write
	private Role originalRole; // Read
 	private int energy; // Read and Write
 	private int position; // Read and Write
 	private boolean frozen; // Read and Write
 	private boolean shielded; // Read and Write
 	private int confusionTurns; // Read and Write
 	
 	Monster(String name, String description, Role originalRole, int energy)
 	{
 		this.name = name;
 		this.description = description;
 		this.originalRole = originalRole;
 		this.role = originalRole;
 		this.energy = energy;
 		this.position = 0;
 		this.confusionTurns = 0;
 		this.frozen = false;
 		this.shielded = false;
 	}
 	public int compareTo(Monster o)
 	{
 		if (this.position < o.position)
 			return -1;
 		else if (this.position > o.position)
 			return 1;
 		else
 			return 0;
 	}
 	public String getName()
 	{
 		return this.name;
 	}
 	public String getDescription()
 	{
 		return this.description;
 	}
 	public Role getRole()
 	{
 		return this.role;
 	}
 	public void setRole(Role tempRole)
 	{
 		 this.role = tempRole;
 	}
 	public Role getOriginalRole()
 	{
 		return this.originalRole;
 	}
 	public int getEnergy()
 	{
 		return this.energy;
 	}
 	public void setEnergy(int E)
 	{
 		this.energy = E;
 	}
 	public int getPosition()
 	{
 		return this.position;
 	}
 	public void setPosition(int pos)
 	{
 		this.position = pos;
 	}
 	public boolean isFrozen()
 	{
 		return this.frozen;
 	}
 	public void setFrozen(boolean flag)
 	{
 		this.frozen = flag;
 	}
 	public boolean isShielded()
 	{
 		return this.shielded;
 	}
 	public void setShielded(boolean flag)
 	{
 		this.shielded = flag;
 	}
 	// getter and setter for confusionTurns not made, due to not having enough
 	// info about subclasses
}
