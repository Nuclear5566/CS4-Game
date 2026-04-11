package game.engine.cells;

import game.engine.monsters.Monster;
import game.engine.Board;
import game.engine.cards.*;
public class CardCell extends Cell {
	
	public CardCell(String name) {
        super(name);
    }
 
	@Override
	public void onLand(Monster landingMonster, Monster opponentMonster) {
		super.onLand(landingMonster, opponentMonster);
		Card drawnCard=Board.drawCard();
		drawnCard.performAction(landingMonster, opponentMonster);
	}
		 
	}