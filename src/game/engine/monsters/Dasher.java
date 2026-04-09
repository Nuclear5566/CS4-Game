package game.engine.monsters;

import game.engine.Constants;
import game.engine.Role;
import game.engine.exceptions.*;

public class Dasher extends Monster {
	private int momentumTurns;

	public Dasher(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
		this.momentumTurns = 0;
	}
	
	public int getMomentumTurns() {
		return momentumTurns;
	}
	
	public void setMomentumTurns(int momentumTurns) {
		this.momentumTurns = momentumTurns;
	}
	
	void executePowerupEffect(Monster opponentMonster) throws OutOfEnergyException {
		//move 3x instead of 2x for 3 turns (needs game class)
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
		super.move(distance * 2);
	}

}