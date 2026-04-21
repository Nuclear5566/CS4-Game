package game.engine;

import java.util.*;
import game.engine.cards.Card;
import game.engine.cells.*;
import game.engine.dataloader.DataLoader;
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
	public void initializeBoard(ArrayList<Cell> specialCells) {
		int[] monsterIndices = Constants.MONSTER_CELL_INDICES; //{2, 18, 34, 54, 82, 88}
		int monsterCounter = 0;
		int[] conveyorIndices = Constants.CONVEYOR_CELL_INDICES; //{6, 22, 44, 52, 66}
		int conveyorCounter = 0;
		int[] sockIndices = Constants.SOCK_CELL_INDICES; //{32, 42, 74, 84, 98}
		int sockCounter = 0;
		int[] cardIndices = Constants.CARD_CELL_INDICES; //{4, 12, 28, 36, 48, 56, 60, 76, 86, 90}
		int cardCounter = 0;
		int doorCellCounter = 0;
		ArrayList<Cell> doorCells = new ArrayList<Cell>();
		ArrayList<Cell> otherSpecialCells = new ArrayList<Cell>();
		// differentiate which is a doorcell and which is not
		for (Cell cell : specialCells)
		{
			if (cell instanceof DoorCell)
			{
				doorCells.add(cell);
			}
			else
			{
				otherSpecialCells.add(cell);
			}
		}
		// fill the 100 spaces first with either Restcell or Odd as Doorcell
		for (int i = 0 ; i < Constants.BOARD_SIZE ; i++)
		{
			if (i%2 == 0)
			{
				 setCell(i, new Cell("Rest Cell"));
			}
			else
			{
				setCell(i, doorCells.get(doorCellCounter));
				doorCellCounter++;
			}
			
		}
		
		//assign the other cells, conveyor, contamination,etc
		
		for(Cell cell : otherSpecialCells) {
			if(cell instanceof MonsterCell) {
				setCell(monsterIndices[monsterCounter], cell);
				monsterCounter++;
			}
			else if(cell instanceof ConveyorBelt) {
				setCell(conveyorIndices[conveyorCounter], cell);
				conveyorCounter++;
			}
			else if(cell instanceof ContaminationSock){
				setCell(sockIndices[sockCounter], cell);
				sockCounter++;
			}
			else if(cell instanceof CardCell) {
				setCell(cardIndices[cardCounter], cell);
				cardCounter++;
			}
		}
		// a the StationedMonsters
		for (int i = 0 ; i < stationedMonsters.size(); i++) {
	        Monster m = stationedMonsters.get(i);
	        m.setPosition(monsterIndices[i]);
	        getCell(monsterIndices[i]).setMonster(m);
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
	public static void reloadCards() {
		cards = new ArrayList<Card>(originalCards);
		Collections.shuffle(cards); //Collections.shuffle() randomly shuffles an ArrayList
	}
	public static Card drawCard() {
		if(cards.isEmpty()) {
			reloadCards();
		}
		return cards.remove(0);
	} 
	public void moveMonster(Monster currentMonster, int roll, Monster opponentMonster) throws InvalidMoveException {
		 int oldPosition = currentMonster.getPosition();
		 currentMonster.move(roll); // move the monster first depending on the roll
         getCell(currentMonster.getPosition()).onLand(currentMonster, opponentMonster); // check collision (swaps/transports may change positions)
		  
         if (currentMonster.getPosition() == opponentMonster.getPosition()) {// Collision check after onLand

		        currentMonster.setPosition(oldPosition);
		        throw new InvalidMoveException("Cannot land on the opponent's cell");
		    }

		    // Decrement confusion after landing
		    if (currentMonster.getConfusionTurns() > 0)
		        currentMonster.decrementConfusion();
		    if (opponentMonster.getConfusionTurns() > 0)
		        opponentMonster.decrementConfusion();

		    // Sync board cell references
		    updateMonsterPositions(currentMonster, opponentMonster);
	}
	private void updateMonsterPositions(Monster player, Monster opponent) {
		for (int i = 0 ; i <  Constants.BOARD_ROWS; i++) { // removing all the cells inside the board
	        for (int j = 0; j < Constants.BOARD_COLS; j ++) {
	            boardCells[i][j].setMonster(null);
	        }
	    }
	    getCell(player.getPosition()).setMonster(player);
	    getCell(opponent.getPosition()).setMonster(opponent); // reassigning them
	}
			
	}

