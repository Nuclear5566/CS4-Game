package game.engine;
import java.util.ArrayList;
import game.engine.monsters.*;
import game.engine.cells.*;
import game.engine.cards.*;

public class Board{
	Cell[][] boardCells;
	static ArrayList <Monster> stationedMonsters;
	static ArrayList<Card> originalCards; 
	static ArrayList<Card> cards;
	
	public Board(ArrayList<Card> readCards){
		this.boardCells = new Cell[Constants.BOARD_ROWS][Constants.BOARD_COLS]; //unsure how to initialize this
		stationedMonsters = new ArrayList<>();
		cards = new ArrayList<>();
		//originalCards should read CSV, does nothing right now
	}
	
	public Cell[][] getboardCells(){ //gets boardCells
		return boardCells;
	}
	public ArrayList<Monster> getstationedMonsters(){ //gets stationedMonsters
		return stationedMonsters;
	}
	public void setStationedMonsters(ArrayList<Monster> newStationedMonsters){ //sets new stationedMonsters
		stationedMonsters = newStationedMonsters;
	}
	public ArrayList<Card> getoriginalCards(){
		return originalCards;
	}
	public ArrayList<Card> getcards(){
		return cards;
	}
	public void setcards(ArrayList<Card> newcards){
		cards = newcards;
	}
}
