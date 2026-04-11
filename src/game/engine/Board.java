package game.engine;

import java.util.*;

import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.monsters.Monster;
import game.engine.exceptions.*;
import game.engine.*;

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
		setCardsByRarity();
		reloadCards();
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
		int row = index / 10;
		int col;
		if(row % 2 == 0) {
			col = index % 10;
		}
		else {
			col = 9 - (index % 10);
		}
		
		int[] rowcol = {row, col};
		
		return rowcol;
	}
	private Cell getCell(int index) {
		int[] rowcol = indexToRowCol(index);
		Cell cell = this.boardCells[rowcol[0]][rowcol[1]];
		return cell;
	}
	private void setCell(int index, Cell cell) {
		int[] rowcol = indexToRowCol(index);
		this.boardCells[rowcol[0]][rowcol[1]] = cell;
	}
	void initializeBoard(ArrayList<Cell> specialCells) {
		int[] monsterIndices = Constants.MONSTER_CELL_INDICES; //{2, 18, 34, 54, 82, 88}
		int monsterCounter = 0;
		int[] conveyorIndices = Constants.CONVEYOR_CELL_INDICES; //{6, 22, 44, 52, 66}
		int conveyorCounter = 0;
		int[] sockIndices = Constants.SOCK_CELL_INDICES; //{32, 42, 74, 84, 98}
		int sockCounter = 0;
		int[] cardIndices = Constants.CARD_CELL_INDICES; //{4, 12, 28, 36, 48, 56, 60, 76, 86, 90}
		int cardCounter = 0;
		
		int i = 0;
		for(Cell cell : specialCells) {
			if(cell instanceof MonsterCell) {
				setCell(monsterIndices[monsterCounter], cell);
				i++;
				monsterCounter++;
			}
			else if(cell instanceof ConveyorBelt) {
				setCell(conveyorIndices[conveyorCounter], cell);
				i++;
				conveyorCounter++;
			}
			else if(cell instanceof ContaminationSock){
				setCell(sockIndices[sockCounter], cell);
				i++;
				sockCounter++;
			}
			else if(cell instanceof CardCell) {
				setCell(cardIndices[cardCounter], cell);
				i++;
				cardCounter++;
			}
			else if(i % 2 != 0) {
				setCell(i, cell);
				i++;
			}
		}
				
	}
	private void setCardsByRarity() {
		ArrayList<Card> newCards = new ArrayList<Card>();
		for(Card card : this.originalCards) {
			int rarity = card.getRarity();
			for(int i = 0; i < rarity; i++) {
				newCards.add(card);
			}
		}
		originalCards = newCards;
	}
	static void reloadCards() {
		cards = originalCards;
		Collections.shuffle(cards); //Collections.shuffle() randomly shuffles an ArrayList
	}
	public static Card drawCard() {
		if(cards.isEmpty()) {
			reloadCards();
		}
		return cards.remove(0);
	}
	void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException {
		
	}
	private void updateMonsterPositions(Monster player, Monster opponent) {
		
	}
}
