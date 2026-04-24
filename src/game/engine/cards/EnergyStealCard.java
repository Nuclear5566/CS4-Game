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
		monster.alterEnergy(canisterValue);
	}  
	public void performAction(Monster player, Monster opponent)
	{
		if (!opponent.isShielded()) // not shielded => the energy steal effect is applied
		{
			int energyStolen = 0;
			if (this.energy > opponent.getEnergy())
			{
				energyStolen = opponent.getEnergy();
			}
			else
			{
				energyStolen = this.energy;
			}
			modifyCanisterEnergy(opponent, -1*energyStolen);
	        modifyCanisterEnergy(player, energyStolen);
		}
		// if shielded the effect would not be applied in the first place, but remove the effect
		else
		{
			opponent.setShielded(false);
		}
	}
	
}
