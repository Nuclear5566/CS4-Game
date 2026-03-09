package game.engine.exceptions;

public class InvalidMoveException extends GameActionException {
	private static final long serialVersionUID = 1L;
	private static final String MSG = "Invalid move attempted";
	//Uses the default exception from MSG
	public InvalidMoveException() {
        super(MSG);
    }
	//Uses custom message
	public InvalidMoveException(String message) {
		super(message);
	}
}