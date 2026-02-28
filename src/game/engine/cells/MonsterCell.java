package game.engine.cells;
import game.engine.monsters.*;
public class MonsterCell extends Cell {

	private Monster cellMonster; // Read only
	public MonsterCell(String name, Monster cellMonster )
	{
		super(name);
		this.cellMonster = cellMonster;
	}
	public Monster getCellMonster()
	{
		return this.cellMonster;
	}
	public void setCellMonster(Monster object)
	{
		this.cellMonster = object;
	}
	  
}
