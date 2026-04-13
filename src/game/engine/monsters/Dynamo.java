package game.engine.monsters;

import game.engine.*;
import game.engine.Role;
import game.engine.exceptions.*;
import java.util.*;

public class Dynamo extends Monster {
	
	public Dynamo(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	
	public void executePowerupEffect(Monster opponentMonster) throws OutOfEnergyException{
		boolean deductEnergy = true;
		/*for(int i = 0; i < Constants.MONSTER_CELL_INDICES.length; i++) {
			if(this.getPosition() == Constants.MONSTER_CELL_INDICES[i]) {
				deductEnergy = false;
			}
		}*/
		
		/*ArrayList<Monster> monsters = Board.getStationedMonsters();
		for(Monster monster : monsters) {
			if(this.getPosition() == monster.getPosition() && this.getRole() == monster.getRole()) {
				deductEnergy = false;
			}
		}
		
		boolean canWork = (this.getEnergy() - Constants.POWERUP_COST) > 0; // checks if the deduction works
		
		if(deductEnergy) {
			int newEnergy = this.getEnergy() - Constants.POWERUP_COST;
			if(canWork) {
				this.setEnergy(newEnergy);
				opponentMonster.setFrozen(true); //will change for one turn, come back after game class is done
			}
			else {
				throw new OutOfEnergyException("Insufficient Energy");
			}
		}
		else {
			opponentMonster.setFrozen(true); //will change for one turn, come back after game class is done
		}*/
		
		opponentMonster.setFrozen(true);
	}
	public void setEnergy(int energy) {
		int change = energy - this.getEnergy();
		super.setEnergy(this.getEnergy() + (change * 2));
	}
}
