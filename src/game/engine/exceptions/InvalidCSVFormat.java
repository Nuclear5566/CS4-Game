package game.engine.exceptions;
import java.io.*;

public class InvalidCSVFormat extends GameActionException {
	static final String MSG = "Invalid input detected while reading csv file, input = \n";
	String inputLine;
	
	public InvalidCSVFormat(String inputLine) {
		super(MSG);
	}
	public InvalidCSVFormat(String message, String inputLine){
		super(inputLine);
	}
	
	public void setInputLine(String inputLine) {
		this.inputLine = inputLine;
	}
}
