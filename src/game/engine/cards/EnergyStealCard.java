package game.engine.cards;

import game.engine.interfaces.CanisterModifier;
import game.engine.monsters.*;

public class EnergyStealCard extends Card implements CanisterModifier {
	private int energy;

	public EnergyStealCard(String name, String description, int rarity, int energy) {
		super(name, description, rarity, true);
		this.energy = energy;
	}
	
	public int getEnergy() {
		return energy;
	}
	@Override
	public void modifyCanisterEnergy(Monster monster, int canisterValue)
	{
		if (canisterValue >=0) // just add the energy to the existing energy of the monster
		{
			monster.setEnergy(monster.getEnergy() + canisterValue);
		}
		else // must first check if the total energy may be < 0, as the energy must be >=0
		{
			if( monster.getEnergy() - Math.abs(canisterValue) <= 0)
			{
				monster.setEnergy(0);
			}
			else
			{
				monster.setEnergy(monster.getEnergy() - Math.abs(canisterValue));
			}
			
		}
	}  
	public void performAction(Monster player, Monster opponent)
	{
		if (!opponent.isShielded()) // not shielded => the energy steal effect is applied
		{
			int lossEnergy = 0;
			if (player.getEnergy() > opponent.getEnergy()) //  it will steal all the energy of the opponent regardless of the type of the car
			{
				modifyCanisterEnergy(player, opponent.getEnergy()); // modifying the energy of the player
				lossEnergy = -1 * opponent.getEnergy();
				modifyCanisterEnergy(opponent,lossEnergy);
			}
			else // it will steal energy according to the type of the card
			{ 
				String cardName = this.getName();
				switch(cardName)
				{
				case "Small Snatcher": // it will steal 50 energy
					modifyCanisterEnergy(player,50);
					modifyCanisterEnergy(opponent,-50);
					break;
				case "Sneaky Thief": // it will steal 100 energy
					modifyCanisterEnergy(player,100);
					modifyCanisterEnergy(opponent,-100);
					break;
				case "Mega Drain": // it will steal 150 energy
					modifyCanisterEnergy(player,150);
					modifyCanisterEnergy(opponent,-150);
					break;
				}
				// note: if this doesn't work use rarity as the switch case
			}
		}
		// if shielded the effect would not be applied in the first place, but remove the effect
		else
		{
			opponent.setShielded(false);
		}
	}
	
}
