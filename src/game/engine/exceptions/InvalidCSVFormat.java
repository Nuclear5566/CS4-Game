package game.engine.exceptions;

public class InvalidCSVFormat extends GameActionException {
	private static final long serialVersionUID = 1L;
	static final String MSG = "Invalid input detected while reading csv file, input = \n";
	String inputLine;
	
	public InvalidCSVFormat(String inputLine) {
		super(MSG + inputLine);
		this.inputLine = inputLine;
	}
	public InvalidCSVFormat(String message, String inputLine){
		super(inputLine);
		this.inputLine = inputLine;
	}
	
	public void setInputLine(String inputLine) {
		this.inputLine = inputLine;
	}
	public String getInputLine() {
		return this.inputLine;
	}
}
