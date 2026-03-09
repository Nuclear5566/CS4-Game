package game.engine;
import java.io.IOException;
import java.util.ArrayList;
import game.engine.monsters.*;
import game.engine.cells.*;
import game.engine.dataloader.DataLoader;
import game.engine.cards.*;

public class Board{
	private Cell[][] boardCells;
	private static ArrayList <Monster> stationedMonsters;
	private static ArrayList<Card> originalCards; 
	private static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards) throws IOException{
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS]; //unsure how to initialize this
		stationedMonsters = new ArrayList<>();
		cards = new ArrayList<>();
		//originalCards should read CSV, does nothing right now
		originalCards = DataLoader.readCards();
	}
	 
	public Cell[][] getBoardCells(){ //gets boardCells
		return boardCells;
	}
	public ArrayList<Monster> getStationedMonsters(){ //gets stationedMonsters
		return stationedMonsters;
	}
	public void setStationedMonsters(Monster newStationedMonsters){ //sets new stationedMonsters
		stationedMonsters.add(newStationedMonsters);
	}
	public ArrayList<Card> getOriginalCards(){
		return originalCards;
	}
	public ArrayList<Card> getCards(){
		return cards;
	}
	public void setCards(ArrayList<Card> newcards){
	    Board.cards = newcards;

	}
}
