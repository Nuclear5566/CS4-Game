package game.engine.cells;
import game.engine.Role;

public abstract class TransportCell extends Cell {

	private int effect; // Read only
	public TransportCell(String name, int effect)
	{
		super(name);
		this.effect = effect;
	}
	public int getEffect()
	{
		return this.effect;
	}
}
