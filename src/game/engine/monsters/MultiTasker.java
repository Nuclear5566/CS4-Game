package game.engine.monsters;

import game.engine.Role;
import game.engine.*;
import game.engine.exceptions.*;

public class MultiTasker extends Monster {
	private int normalSpeedTurns;
	
	public MultiTasker(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
		this.normalSpeedTurns = 0;
	}

	public int getNormalSpeedTurns() {
		return normalSpeedTurns;
	}

	public void setNormalSpeedTurns(int normalSpeedTurns) {
		this.normalSpeedTurns = normalSpeedTurns;
	}
	
	void executePowerupEffect(Monster opponentMonster) throws OutOfEnergyException {
		boolean deductEnergy = true;
		for(int i = 0; i < Constants.MONSTER_CELL_INDICES.length; i++) {
			if(this.getPosition() == i) {
				deductEnergy = false;
			}
		}
		
		boolean canWork = (this.getEnergy() - 500) > 0;
		
		if(deductEnergy) {
			int newEnergy = this.getEnergy() - 500;
			if(canWork) {
				this.setEnergy(newEnergy);
				//add actual implementation
			}
		}
		else {
			//add actual implementation
		}
	}
	
	void move(int distance) {
		super.move(distance / 2);
	}
	
	void alterEnergy(int energy) {
		super.alterEnergy(energy + 200);
	}

}