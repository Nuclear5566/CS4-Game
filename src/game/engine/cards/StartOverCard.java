package game.engine.cards;

import game.engine.monsters.Monster;

public class StartOverCard extends Card {

	public StartOverCard(String name, String description, int rarity, boolean lucky) {
		super(name, description, rarity, lucky);
	}


	public void performAction(Monster Player, Monster opponent) {
		
		if (this.isLucky()) // if the card is lucky, set the opponent position 0
		{
			opponent.setPosition(0);
		}
		else
		{
			Player.setPosition(0);
		}
	}
	
}
