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
		ArrayList<Monster> stationedMonsters = new ArrayList<Monster>();
		for(Monster monster : allMonsters) {
			if(monster.equals(player) || monster.equals(opponent)) {
				continue;
			}
			stationedMonsters.add(monster);
		}
		Board.setStationedMonsters(stationedMonsters);
		board.initializeBoard(DataLoader.readCells());
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
	public void usePowerup() throws OutOfEnergyException {
		if (current.getEnergy() < Constants.POWERUP_COST) {
	        throw new OutOfEnergyException();
	    }
		else
		{ 
			 current.setEnergy(current.getEnergy() - Constants.POWERUP_COST);
			 current.executePowerupEffect(getCurrentOpponent());
		}
	}
	public void playTurn() throws InvalidMoveException {
		if (current.isFrozen())
		{
			// skip the turn
			current.setFrozen(false);
			this.switchTurn();
			return;
		}
		else
		{
			int roll = rollDice();
		    board.moveMonster(current, roll, getCurrentOpponent());
		    this.switchTurn();
		}
		
	}
	private void switchTurn() {
		if(current.equals(player)) {
			this.current = this.opponent;
		}
		else if(current.equals(opponent)) {
			this.current = this.player;
		}
	}
	private boolean checkWinCondition(Monster monster) {
		if(monster.getEnergy() >= Constants.WINNING_ENERGY && monster.getPosition() == Constants.WINNING_POSITION) {
			return true;
		}
		return false;
	}
	public Monster getWinner() {
		if(checkWinCondition(player)) {
			return player;
		}
		else if(checkWinCondition(opponent)) {
			return opponent;
		}
		return null;
	}
	
}