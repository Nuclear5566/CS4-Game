package game.engine.monsters;

import java.util.ArrayList;

import game.engine.*;
import game.engine.exceptions.*;

public class Schemer extends Monster {
	
	public Schemer(String name, String description, Role role, int energy) {
		super(name, description, role, energy);
	}
	private int stealEnergyFrom(Monster target) {
		int temp = Constants.SCHEMER_STEAL;
		if(temp > target.getEnergy()) {
			return target.getEnergy();
		}
		else {
			return temp;
		}
	}
	public void executePowerupEffect(Monster opponentMonster) throws OutOfEnergyException {
		//schemer steals energy from the opponent and all stationed monsters, gaining a single total steal bonus at the end
		//gains +10 energy on every incoming energy changes, whether positive or negative
		
		boolean monsterAtSameCell = false;
		boolean deductEnergy = true;
		int totalEnergy = 0; //total combined energy of all stationedMonsters
		
		/*for(int i = 0; i < Constants.MONSTER_CELL_INDICES.length; i++) {
			if(this.getPosition() == i) {
				//if(Board.getBoardCells()[i%10][i/10].getMonster().getRole() == this.getRole()) {
					
				//}
			}
		}*/
		ArrayList<Monster> monsters = Board.getStationedMonsters();
		for(Monster monster : monsters) {
			if(this.getPosition() == monster.getPosition() && this.getRole() == monster.getRole()) {
				deductEnergy = false;
			}
		}
			
		ArrayList<Monster> stationedMonsters = Board.getStationedMonsters();
		
		
		for(Monster cellMonster : stationedMonsters) {
			//totalEnergy = totalEnergy + cellMonster.getEnergy();
			totalEnergy = totalEnergy + 10;
		}
		
		boolean canWork = (this.getEnergy() - 500) > 0;
		
		if(canWork || !deductEnergy) {
			for(Monster cellMonster : stationedMonsters) {
				cellMonster.setEnergy(cellMonster.getEnergy() - 10);
			}
		}
		
		if(deductEnergy) {
			int newEnergy = this.getEnergy() - 500;
			if(canWork) {
				this.setEnergy(newEnergy);
				this.alterEnergy(totalEnergy);
			}
			else {
				throw new OutOfEnergyException("Insufficient Energy");
			}
		}
		else {
			this.alterEnergy(totalEnergy);
		}
		
	}
	
	public void setEnergy(int energy) {
		int change = energy - this.getEnergy();
		super.setEnergy(this.getEnergy() + (change + 10));
	}
	
}
