package game.engine.cells;

import game.engine.monsters.*;
import game.engine.exceptions.*;
public class MonsterCell extends Cell {
	private Monster cellMonster;

	public MonsterCell(String name, Monster cellMonster) {
		super(name);
		this.cellMonster = cellMonster;
	}

	public Monster getCellMonster() {
		return cellMonster;
	}
	
	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);
		//Role Match
		if(landingMonster.getRole()==this.cellMonster.getRole()) {
			try {
				landingMonster.executePowerupEffect(opponentMonster);
			} catch(OutOfEnergyException e) {
				
			}
		}
		//Role Mismatch
		else {
			if(landingMonster.getEnergy()>this.cellMonster.getEnergy()) {
				//I will use different Logic instead of temp because of shield hassle
				int energyDifference=landingMonster.getEnergy() - this.cellMonster.getEnergy();
				this.cellMonster.setEnergy(landingMonster.getEnergy());
				landingMonster.alterEnergy(-energyDifference);
			}
		}
		
	}

}
