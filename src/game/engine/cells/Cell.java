package game.engine.cells;
import game.engine.monsters.*;
public class Cell {
  
	private String name;
	private Monster monster;
	public Cell(String name)
	{
		this.name = name;
		monster = null;
	}
	
	public String getName()
	{
		return this.name;
	}
	public Monster getMonster()
	{
		return this.monster;
	}
	public void setMonster(Monster object)
	{
		this.monster = object;
	}
	
}
 