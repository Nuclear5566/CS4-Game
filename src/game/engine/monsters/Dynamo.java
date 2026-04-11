package game.engine.monsters;

import game.engine.Constants;
import game.engine.Role;
import game.engine.exceptions.*;

public class Dynamo extends Monster {
	
	public Dynamo(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	
	public void executePowerupEffect(Monster opponentMonster) throws OutOfEnergyException{
		boolean deductEnergy = true;
		for(int i = 0; i < Constants.MONSTER_CELL_INDICES.length; i++) {
			if(this.getPosition() == Constants.MONSTER_CELL_INDICES[i]) {
				deductEnergy = false;
			}
		}
		
		boolean canWork = (this.getEnergy() - 500) > 0;
		
		if(deductEnergy) {
			int newEnergy = this.getEnergy() - 500;
			if(canWork) {
				this.setEnergy(newEnergy);
				opponentMonster.setFrozen(true); //will change for one turn, come back after game class is done
			}
		}
		else {
			opponentMonster.setFrozen(true); //will change for one turn, come back after game class is done
		}
	}
	public void alterEnergy(int energy) {
		super.alterEnergy(energy * 2);
	}
}
