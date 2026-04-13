package game.engine.monsters;

import java.util.ArrayList;

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
	
	public void executePowerupEffect(Monster opponentMonster) throws OutOfEnergyException {
		boolean deductEnergy = true;
		/*for(int i = 0; i < Constants.MONSTER_CELL_INDICES.length; i++) {
			if(this.getPosition() == Constants.MONSTER_CELL_INDICES[i]) {
				
				//deductEnergy = false;
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
				//add actual implementation
				this.normalSpeedTurns = 2;
			}
			else {
				throw new OutOfEnergyException("Insufficient Energy");
			}
		}
		else {
			this.normalSpeedTurns = 2;
		}*/
		
		this.normalSpeedTurns = 2;
	}
	
	public void move(int distance) {
		super.move(distance / 2);
	}
	
	public void setEnergy(int energy) {
		int change = energy - this.getEnergy();
		super.setEnergy(this.getEnergy() + (change + 200));
	}

}