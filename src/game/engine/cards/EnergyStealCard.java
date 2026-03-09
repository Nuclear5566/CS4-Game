package game.engine.cards;
import game.engine.interfaces.*;
import game.engine.monsters.*;
public class EnergyStealCard extends Card implements CanisterModifier {
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
	public void Change_Energy(Monster shrek,int energy) {  
		/*it should take the energy of the card but idk why its private
		 so i'll leave it for now*/
	}
}
