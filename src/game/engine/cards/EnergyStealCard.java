package game.engine.cards;

public class EnergyStealCard extends Card {
	private int energy; // Read only
	public EnergyStealCard(String name, String description, int rarity, int energy)
	{
		super(name,description,rarity);
		this.energy = energy;
	}
	public int getEnergy()
	{
		return this.energy;
	}
}
