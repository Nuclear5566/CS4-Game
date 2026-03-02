package game.engine.exceptions;
public abstract class GameActionException extends Exception {
	private static final long serialVersionUID = 1L;
	//This was for the error below
	//The serializable class GameActionException does not declare a static final serialVersionUID field of type long
	public GameActionException() {
		super();
	}
	public GameActionException(String message) {
		super(message);
	}
}