package game.engine;

import java.util.ArrayList;

import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.monsters.Monster;
import game.engine.exceptions.*;

public class Board {
	private Cell[][] boardCells;
	private static ArrayList<Monster> stationedMonsters; 
	private static ArrayList<Card> originalCards;
	public static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards) {
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS];
		stationedMonsters = new ArrayList<Monster>();
		originalCards = readCards;
		cards = new ArrayList<Card>();
	}
	
	public Cell[][] getBoardCells() {
		return boardCells;
	}
	
	public static ArrayList<Monster> getStationedMonsters() {
		return stationedMonsters;
	}
	
	public static void setStationedMonsters(ArrayList<Monster> stationedMonsters) {
		Board.stationedMonsters = stationedMonsters;
	}

	public static ArrayList<Card> getOriginalCards() {
		return originalCards;
	}
	
	public static ArrayList<Card> getCards() {
		return cards;
	}
	
	public static void setCards(ArrayList<Card> cards) {
		Board.cards = cards;
	}
	
	private int[] indexToRowCol(int index) {
		
	}
	private Cell getCell(int index) {
		
	}
	private void setCell(int index, Cell cell) {
		
	}
	void initializeBoard(ArrayList<Cell> specialCells) {
		
	}
	private void setCardsByRarity() {
		
	}
	static void reloadCards() {
		
	}
	static Card drawCard() {
		
	}
	void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException {
		
	}
	private void updateMonsterPositions(Monster player, Monster opponent) {
		
	}
}
