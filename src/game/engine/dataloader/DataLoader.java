package game.engine.dataloader;
import java.io.*;
import java.util.ArrayList;
import game.engine.cards.*;
import game.engine.cells.*;
import game.engine.monsters.*;
import game.engine.*;
public class DataLoader {
	 static String CARDS_FILE_NAME = "cards.csv"; //A String containing the name of the card’s CSV file.
	 static String CELLS_FILE_NAME = "cells.csv"; //A String containing the name of the cell’s CSV file.
	 static String MONSTERS_FILE_NAME = "monsters.csv"; //A String containing the name of the monster’s CSV file.
	 
	 public static ArrayList<Card> readCards() throws IOException{
		 ArrayList<Card> cards = new ArrayList<Card>();
		 try(BufferedReader reader = new BufferedReader(new FileReader(CARDS_FILE_NAME))){
			 String line;
			 while((line = reader.readLine()) != null) {
				 String [] items = line.split(","); //creates an array with a line in the csv
				 
				 String cardType = items[0];
				 String name = items[1];
				 String description = items[2];
				 int rarity = Integer.parseInt(items[3]);
				 switch(cardType) {
				 	case "STARTOVER":
				 		boolean lucky = Boolean.parseBoolean(items[4]);
				 		Card c1 = new StartOverCard(name, description, rarity, lucky);
				 		cards.add(c1);
				 		break;
				 	case "ENERGYSTEAL":
				 		int energy = Integer.parseInt(items[4]);
				 		Card c2 = new EnergyStealCard(name, description, rarity, energy);
				 		cards.add(c2);
				 		break;
				 	case "CONFUSION":
				 		int duration = Integer.parseInt(items[4]);
				 		Card c3 = new ConfusionCard(name, description, rarity, duration);
				 		cards.add(c3);
				 		break;
				 	case "SHIELD":
				 		Card c4 = new ShieldCard(name, description, rarity);
				 		cards.add(c4);
				 		break;
				 	case "SWAPPER":
				 		Card c5 = new SwapperCard(name, description, rarity);
				 		cards.add(c5);
				 		break;
				 }						 
			 }
		 }
		 return cards;
	 }
	 public static ArrayList<Cell> readCells() throws IOException{
		 ArrayList<Cell> cells = new ArrayList<Cell>();
		 try(BufferedReader reader = new BufferedReader(new FileReader(CELLS_FILE_NAME))){
			 String line;
			 while((line = reader.readLine()) != null) {
				 String [] items = line.split(","); //creates an array with a line in the csv
				 String name = items[0];
				 if(items[1].equals("SCARER")){
					 int energy = Integer.parseInt(items[2]);
					 Role role = Role.SCARER;
					 Cell c = new DoorCell(name, role, energy);
					 cells.add(c);
				 }
				 else if(items[1].equals("LAUGHER")){
					 int energy = Integer.parseInt(items[2]);
					 Role role = Role.LAUGHER;
					 Cell c = new DoorCell(name, role, energy);
					 cells.add(c);
				 }
				 else if(Integer.parseInt(items[1]) >= 0){
					 int effect = Integer.parseInt(items[1]);
					 TransportCell c = new ConveyorBelt(name, effect);
					 cells.add(c);
				 }
				 else if(Integer.parseInt(items[1]) <= 0){
					 int effect = Integer.parseInt(items[1]);
					 TransportCell c = new ContaminationSock(name, effect);
					 cells.add(c);
				 }
				 
			 }
		 }
		 return cells;
	 }
	 public static ArrayList<Monster> readMonsters() throws IOException{
		 ArrayList<Monster> monster = new ArrayList<Monster>();
		 try(BufferedReader reader = new BufferedReader(new FileReader(MONSTERS_FILE_NAME))){
			 String line;
			 while((line = reader.readLine()) != null) {
				 String [] items = line.split(","); //creates an array with a line in the csv
				 String monsterType = items[0];
				 String name = items[1];
				 String description = items[2];
				 Role role;
				 if(items[3].equals("SCARER")) {
					 role = Role.SCARER;
				 }
				 else{
					 role = Role.LAUGHER;
				 }
				 int energy = Integer.parseInt(items[4]);
				 switch(monsterType){
				 	case "DYNAMO":
				 		Monster m1 = new Dynamo(name, description, role, energy);
				 		monster.add(m1);
				 		break;
				 	case "DASHER":
				 		Monster m2 = new Dasher(name, description, role, energy);
				 		monster.add(m2);
				 		break;
				 	case "SCHEMER":
				 		Monster m3 = new Schemer(name, description, role, energy);
				 		monster.add(m3);
				 		break;
				 	case "MULTITASKER":
				 		Monster m4 = new MultiTasker(name, description, role, energy);
				 		monster.add(m4);
				 		break;
				 }
			 }
		 }
		 return monster;
	 }
}
