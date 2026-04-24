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
		ArrayList<Monster> stationedMonsters = Board.getStationedMonsters();
		int totalStolen = 0;

		// Steal from opponent
		int stolenFromOpponent = stealEnergyFrom(opponentMonster);
		opponentMonster.setEnergy(opponentMonster.getEnergy() - stolenFromOpponent);
		totalStolen += stolenFromOpponent;

		// Steal from all stationed monsters
		for (Monster cellMonster : stationedMonsters) {
			int stolen = stealEnergyFrom(cellMonster);
			cellMonster.setEnergy(cellMonster.getEnergy() - stolen);
			totalStolen += stolen;
		}

		// Give total to schemer at once (setEnergy applies +10 passive)
		this.setEnergy(this.getEnergy() + totalStolen);
	}
	
	public void setEnergy(int energy) {
		int change = energy - this.getEnergy();
		super.setEnergy(this.getEnergy() + (change + 10));
	}
	
}
