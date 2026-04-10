package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import game.engine.dataloader.DataLoader;
import game.engine.monsters.*;
import game.engine.exceptions.*;

public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters; 
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	public Game(Role playerRole) throws IOException {
		this.board = new Board(DataLoader.readCards());
		
		this.allMonsters = DataLoader.readMonsters();
		
		this.player = selectRandomMonsterByRole(playerRole);
		this.opponent = selectRandomMonsterByRole(playerRole == Role.SCARER ? Role.LAUGHER : Role.SCARER);
		this.current = player;
	}
	
	public Board getBoard() {
		return board;
	}
	
	public ArrayList<Monster> getAllMonsters() {
		return allMonsters; 
	}
	
	public Monster getPlayer() {
		return player;
	}
	
	public Monster getOpponent() {
		return opponent;
	}
	
	public Monster getCurrent() {
		return current;
	}
	
	public void setCurrent(Monster current) {
		this.current = current;
	}
	
	private Monster selectRandomMonsterByRole(Role role) {
		Collections.shuffle(allMonsters);
	    return allMonsters.stream()
	    		.filter(m -> m.getRole() == role)
	    		.findFirst()
	    		.orElse(null);
	}
	
	
	private Monster getCurrentOpponent() {
		if(this.getCurrent().equals(this.player)) {
			return this.opponent;
		}
		else {
			return this.player;
		}
	}
	private int rollDice() {
		return (int) (Math.random() * 6 + 1);
	}
	void usePowerup() throws OutOfEnergyException {
		boolean deductEnergy = true;
		for(int i = 0; i < Constants.MONSTER_CELL_INDICES.length; i++) {
			if(this.current.getPosition() == i && this.current.getRole() == i.getRole()) {
				deductEnergy = false;
			}
		}
		if(!deductEnergy && this.current.getEnergy() > 500) {
			this.current.
		}
	}
	void playTurn() throws InvalidMoveException {
		
	}
	private void switchTurn() {
		
	}
	private boolean checkWinCondition(Monster monster) {
		
	}
	Monster getWinner() {
		
	}
	
}