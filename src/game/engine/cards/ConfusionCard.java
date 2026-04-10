package game.engine.cards;

import game.engine.monsters.Monster;
import game.engine.Role;
public class ConfusionCard extends Card {
	private int duration;
	
	public ConfusionCard(String name, String description, int rarity, int duration) {
		super(name, description, rarity, false);
		this.duration = duration;
	}
	
	public int getDuration() {
		return duration;
	}

	
	public void performAction(Monster Player, Monster opponent) {
		// swap the 2 roles, and updates the confusion turns depending on the type of card
		
		// swapping
		Role tempRole = Player.getRole();
		Player.setRole(opponent.getRole());
		opponent.setRole(tempRole);
		
		// updates the confusion turns(will be the same as duration)
		Player.setConfusionTurns(this.getDuration());
		opponent.setConfusionTurns(this.getDuration());
		
		// the door cell should award/penalize them in reverse which idk how this will be implemented
		
	}

}
