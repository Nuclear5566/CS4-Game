package game.engine;
import java.util.ArrayList;
import java.util.Random;
import game.engine.dataloader.DataLoader;
import game.engine.monsters.Monster;
import java.io.*; //for IOException
public class Game {
	private Board board;
	private ArrayList<Monster> allMonsters;
	private Monster player;
	private Monster opponent;
	private Monster current;
	
	public Game(Role playerRole) throws IOException{
		this.board=new Board(DataLoader.readCards());
		this.allMonsters=DataLoader.readMonsters();
		this.player=selectRandomMonsterByRole(playerRole);
		
		Role opponent_Role;
		if(playerRole==Role.SCARER) {
			opponent_Role=Role.LAUGHER;
		}
		else {
				opponent_Role=Role.SCARER;
			}
		this.opponent=selectRandomMonsterByRole(opponent_Role);
		this.current=this.player;
	}
	
	private Monster selectRandomMonsterByRole(Role role){
		ArrayList<Monster> filteredMonsters=new ArrayList<Monster>();
		for(int i=0;i<allMonsters.size();i++) {
			Monster m=allMonsters.get(i);
			if(m.getRole()==role) {
				filteredMonsters.add(m);
			}
		}
		if(filteredMonsters.size()>0) {
			Random rand=new Random();
			int RandomIndex=rand.nextInt(filteredMonsters.size());
			return filteredMonsters.get(RandomIndex);
		}
		return null;
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

	public void setCurrent(Monster current) {//READ AND WRITE
		this.current = current;
	}
}
