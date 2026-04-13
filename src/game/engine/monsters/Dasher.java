package game.engine.monsters;

import java.util.ArrayList;

import game.engine.Board;
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
	
	public void executePowerupEffect(Monster opponentMonster) throws OutOfEnergyException {
		//move 3x instead of 2x for 3 turns (needs game class)
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
		
		boolean canWork = (this.getEnergy() - Constants.POWERUP_COST) > 0;
		
		if(deductEnergy) {
			int newEnergy = this.getEnergy() - Constants.POWERUP_COST;
			if(canWork) {
				this.setEnergy(newEnergy);
				this.momentumTurns = 3;
			}
			else {
				throw new OutOfEnergyException("Insufficient Energy");
			}
		}
		else {
			//add actual implementation
			this.momentumTurns = 3;
		}*/
		
		this.momentumTurns = 3;
	}
	
	public void move(int distance) {
		super.move(distance * 2);
	}

}