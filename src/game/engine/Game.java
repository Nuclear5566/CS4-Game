package game.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import javafx.*;
import javafx.application.*;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class Game extends Application {
	private Board board;
	private ArrayList<Monster> allMonsters; 
	private Monster player;
	private Monster opponent;
	private Monster current; 
	
	public Game() {
		
	}
	public Game(Role playerRole) throws IOException {
		this.board = new Board(DataLoader.readCards());
		
		this.allMonsters = DataLoader.readMonsters();
		
		this.player = selectRandomMonsterByRole(playerRole);
		this.opponent = selectRandomMonsterByRole(playerRole == Role.SCARER ? Role.LAUGHER : Role.SCARER);
		this.current = player;
		
		allMonsters.remove(player);
		allMonsters.remove(opponent);
		
		Board.setStationedMonsters(allMonsters);
		board.initializeBoard(DataLoader.readCells());
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
	
	public void setCurrent(Monster current) {
		this.current = current;
	}
	
	private Monster selectRandomMonsterByRole(Role role) {
		Collections.shuffle(allMonsters);
	    return allMonsters.stream()
	    		.filter(m -> m.getRole() == role)
	    		.findFirst()
	    		.orElse(null);
	}
	
	private Monster getCurrentOpponent() {
		return current == player ? opponent : player;
	}

	private int rollDice() {
		Random rand = new Random();
		return rand.nextInt(6) + 1;
	}
	
	public void usePowerup() throws OutOfEnergyException {
		if (current.getEnergy() < Constants.POWERUP_COST)
			throw new OutOfEnergyException("Not enough energy to use powerup");
		
		current.executePowerupEffect(getCurrentOpponent());
		current.setEnergy(current.getEnergy() - Constants.POWERUP_COST);
	}
	
	public void playTurn() throws InvalidMoveException {
		if (current.isFrozen()) {
			System.out.println(current.getName() + " is frozen! Turn skipped.");
			current.setFrozen(false);
			switchTurn();
			return;
		}
		
		int roll = rollDice();
		
		board.moveMonster(current, roll, getCurrentOpponent());
		
		switchTurn();
	}
	
	private void switchTurn() {
		this.setCurrent(getCurrentOpponent());
	}
	
	private boolean checkWinCondition(Monster monster) {
		return monster.getPosition() == Constants.WINNING_POSITION && 
		       monster.getEnergy() >= Constants.WINNING_ENERGY;
	}
	
	public Monster getWinner() {
		if (checkWinCondition(player)) 
			return player;
		
		if (checkWinCondition(opponent)) 
			return opponent;
		
		return null;
	}
	private StackPane createImageButton(String brushstrokePath, String labelImagePath, Stage stage, double widthRatio, double heightRatio, double width_enhancer) {
	    
	    // Brushstroke background
	    ImageView brush = new ImageView(new Image("titlescreenbuttonbackground.png"));
	    brush.setPreserveRatio(false);
	    brush.fitWidthProperty().bind(stage.widthProperty().multiply(widthRatio));
	    brush.fitHeightProperty().bind(stage.heightProperty().multiply(heightRatio));

	    // Label image (PLAY or CREDITS)
	    ImageView label = new ImageView(new Image(labelImagePath));
	    label.setPreserveRatio(true);
	    label.fitWidthProperty().bind(stage.widthProperty().multiply(widthRatio * width_enhancer));

	    StackPane btn = new StackPane(brush, label);
	    btn.setStyle("-fx-cursor: hand;");
	    btn.setOnMouseEntered(e -> btn.setOpacity(0.8));
	    btn.setOnMouseExited(e -> btn.setOpacity(1.0));

	    return btn;
	}
	@Override
	public void start(Stage primaryStage) throws Exception {
		// creating the head icon of the game
		Image icon = new Image("gameNameTitle.png");
		primaryStage.getIcons().add(icon);
		// Layer 1: background image
	    ImageView background = new ImageView(new Image("background(1st layer).png"));
	    background.fitWidthProperty().bind(primaryStage.widthProperty());
	    background.fitHeightProperty().bind(primaryStage.heightProperty());
	    background.setPreserveRatio(false);
	    
	    // Layer 2: black shady thing
	    ImageView layer2 = new ImageView(new Image("titlegradientRectangle.png"));
	    background.fitWidthProperty().bind(primaryStage.widthProperty());
	    background.fitHeightProperty().bind(primaryStage.heightProperty());
	    background.setPreserveRatio(false);

	    StackPane playBtn = createImageButton(
	    	    "titlescreenbuttonbackground.png",   // convert your SVG to PNG and save here
	    	    "PLAY.png",
	    	    primaryStage, 0.22, 0.18,0.8
	    	);

	    	StackPane creditsBtn = createImageButton(
	    	    "titlescreenbuttonbackground.png",
	    	    "CREDITS.png",
	    	    primaryStage, 0.22, 0.18,0.6
	    	);

	    // Click actions
	    playBtn.setOnMouseClicked(e -> {
	        System.out.println("Play clicked!");
	        // TODO: switch to game scene
	    });
	    creditsBtn.setOnMouseClicked(e -> {
	        System.out.println("Credits clicked!");
	        // TODO: switch to credits scene
	    });

	    // Stack buttons vertically
	    Pane buttonLayer = new Pane();
	    playBtn.layoutXProperty().bind(
	    	    primaryStage.widthProperty().multiply(0.75)
	    	    .subtract(primaryStage.widthProperty().multiply(0.01).divide(2))
	    	);
	    	playBtn.layoutYProperty().bind(
	    	    primaryStage.heightProperty().multiply(0.55)
	    	    .subtract(primaryStage.heightProperty().multiply(0.18).divide(2))
	    	);

	    	// Bind Credits button position (X=0.95, Y=0.73)
	    	creditsBtn.layoutXProperty().bind(
	    	    primaryStage.widthProperty().multiply(0.95)
	    	    .subtract(primaryStage.widthProperty().multiply(0.40).divide(2))
	    	);
	    	creditsBtn.layoutYProperty().bind(
	    	    primaryStage.heightProperty().multiply(0.73)
	    	    .subtract(primaryStage.heightProperty().multiply(0.18).divide(2))
	    	);

	    	buttonLayer.getChildren().addAll(playBtn, creditsBtn);
	    
	    	// Door Dash title logo
	    	ImageView titleLogo = new ImageView(new Image("gameNameTitle.png"));
	    	titleLogo.setPreserveRatio(true);
	    	titleLogo.fitWidthProperty().bind(primaryStage.widthProperty().multiply(0.4));

	    	Pane titleLayer = new Pane();

	    	titleLogo.layoutXProperty().bind(
	    	    primaryStage.widthProperty().multiply(0.25)
	    	    .subtract(primaryStage.widthProperty().multiply(0.4).divide(2))
	    	);
	    	titleLogo.layoutYProperty().bind(
	    	    primaryStage.heightProperty().multiply(0.5)
	    	    .subtract(titleLogo.fitWidthProperty().divide(2))
	    	);

	    	titleLayer.getChildren().add(titleLogo);

	    	// Add titleLayer to root between background/shade and buttons
	    // Root
	    StackPane root = new StackPane(background, layer2, buttonLayer,titleLayer);
	    Scene scene = new Scene(root);
	    primaryStage.setTitle("Door Dash");
	    primaryStage.setScene(scene);
	    primaryStage.setMaximized(true);
	    primaryStage.show();
	    scene.setOnKeyPressed(e -> {
	        if (e.getCode() == KeyCode.ESCAPE) {
	            primaryStage.setFullScreen(!primaryStage.isFullScreen());
	        }
	    });
	    primaryStage.setFullScreenExitHint("");
	    primaryStage.setFullScreen(true);
	}
	 
	public static void main(String [] args) {
		launch();
	}
	
}