package game.engine.cards;

import game.engine.monsters.Monster;

public class ShieldCard extends Card {
	
	public ShieldCard(String name, String description, int rarity) {
		super(name, description, rarity, true); 
	}
	


	
	public void performAction(Monster Player, Monster opponent) {
		Player.setShielded(true);
		opponent.setShielded(false);
	}
}