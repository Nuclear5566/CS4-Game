package game.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import game.engine.cards.Card;
import game.engine.cells.Cell;
import game.engine.cells.DoorCell;
import game.engine.dataloader.DataLoader;
import game.engine.exceptions.InvalidMoveException;
import game.engine.exceptions.OutOfEnergyException;
import game.engine.monsters.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.ColorAdjust;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.beans.binding.Bindings;

public class Game extends Application {
    
    // ─── ENGINE VARIABLES ─────────────────────────────────────────────────────
    private Board board;
    private ArrayList<Monster> allMonsters;
    private Monster player;
    private Monster opponent;
    private Monster current;
    private int lastRoll = 0;

    // ─── GUI VARIABLES ────────────────────────────────────────────────────────
    private MediaPlayer mediaPlayer;
    private Game activeGame;
    
    // ─── DEBUG LOGGER ─────────────────────────────────────────────────────────
    private static final boolean DEBUG_MODE = true;
    
    private void debugLog(String message) {
        if (DEBUG_MODE) {
            System.out.println("[DEBUG-SYNC] " + message);
        }
    }

    public Game() {}

    public Game(Role playerRole) throws IOException {
        this.board = new Board(DataLoader.readCards());
        this.allMonsters = DataLoader.readMonsters();
        
        // 1. Assign Player Role Randomly
        this.player = selectRandomMonsterByRole(playerRole, null);
        allMonsters.remove(this.player);
        
        // 2. Assign Opponent to the opposite team randomly, ensuring they are NOT the same monster type!
        Role oppRole = (playerRole == Role.SCARER) ? Role.LAUGHER : Role.SCARER;
        this.opponent = selectRandomMonsterByRole(oppRole, this.player.getClass());
        this.current = this.player;
        allMonsters.remove(this.opponent);

        // 3. Create a new list for the remaining monsters to act as stationed cells
        ArrayList<Monster> remainingStationedMonsters = new ArrayList<>(allMonsters);
        Board.setStationedMonsters(remainingStationedMonsters);
        board.initializeBoard(DataLoader.readCells());
    }

    public Board getBoard() { return board; }
    public ArrayList<Monster> getAllMonsters() { return allMonsters; }
    public Monster getPlayer() { return player; }
    public Monster getOpponent() { return opponent; }
    public Monster getCurrent() { return current; }
    public void setCurrent(Monster current) { this.current = current; }
    public int getLastRoll() { return lastRoll; }

    private Monster selectRandomMonsterByRole(Role role, Class<?> excludeType) {
        Collections.shuffle(allMonsters);
        return allMonsters.stream()
                .filter(m -> m.getRole() == role && (excludeType == null || !m.getClass().equals(excludeType)))
                .findFirst()
                .orElseGet(() -> allMonsters.stream().filter(m -> m.getRole() == role).findFirst().orElse(null));
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
            lastRoll = 0; 
            switchTurn();
            return;
        }
        lastRoll = rollDice();
        board.moveMonster(current, lastRoll, getCurrentOpponent());
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
        if (checkWinCondition(player)) return player;
        if (checkWinCondition(opponent)) return opponent;
        return null;
    }

    private boolean isCardCell(int position) {
        int[] cardCells = {4, 12, 28, 36, 48, 56, 60, 76, 86, 90};
        for (int c : cardCells) {
            if (position == c) return true;
        }
        return false;
    }

    // ─── SCENES ───────────────────────────────────────────────────────────────

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Door Dash");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        
        primaryStage.setWidth(1024);
        primaryStage.setHeight(768);
        
        primaryStage.setScene(createTitleScene(primaryStage));
        
        primaryStage.setMaximized(true); 
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    private Scene createTitleScene(Stage stage) {
        StackPane root = new StackPane();
        double w = stage.getWidth() > 0 ? stage.getWidth() : 1024;
        double h = stage.getHeight() > 0 ? stage.getHeight() : 768;
        Scene scene = new Scene(root, w, h);
        
        stage.getIcons().add(new Image("gameNameTitle.png"));

        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);
        ImageView layer2 = new ImageView(new Image("titlegradientRectangle.png"));
        layer2.setPreserveRatio(false);

        ImageView titleLogo = new ImageView(new Image("gameNameTitle.png"));
        titleLogo.setPreserveRatio(true);
        titleLogo.fitWidthProperty().bind(root.widthProperty().multiply(0.4));

        Pane titleLayer = new Pane();
        titleLogo.layoutXProperty().bind(
            root.widthProperty().multiply(0.25)
            .subtract(root.widthProperty().multiply(0.4).divide(2))
        );
        titleLogo.layoutYProperty().bind(
            root.heightProperty().multiply(0.5)
            .subtract(titleLogo.fitWidthProperty().divide(2))
        );
        titleLayer.getChildren().add(titleLogo);
        titleLayer.setPickOnBounds(false);

        StackPane playBtn = createImageButton("titlescreenbuttonbackground.png", "PLAY.png", root, 0.22, 0.18, 0.8);
        StackPane creditsBtn = createImageButton("titlescreenbuttonbackground.png", "CREDITS.png", root, 0.22, 0.18, 0.6);
        playBtn.setOnMouseClicked(e -> stage.setScene(createInstructionsScene(stage)));
        creditsBtn.setOnMouseClicked(e -> stage.setScene(createCreditsScene(stage)));

        playBtn.setPickOnBounds(true);
        creditsBtn.setPickOnBounds(true);

        playBtn.layoutXProperty().bind(
            root.widthProperty().multiply(0.75)
            .subtract(root.widthProperty().multiply(0.01).divide(2))
        );
        playBtn.layoutYProperty().bind(
            root.heightProperty().multiply(0.55)
            .subtract(root.heightProperty().multiply(0.18).divide(2))
        );
        creditsBtn.layoutXProperty().bind(
            root.widthProperty().multiply(0.95)
            .subtract(root.widthProperty().multiply(0.40).divide(2))
        );
        creditsBtn.layoutYProperty().bind(
            root.heightProperty().multiply(0.73)
            .subtract(root.heightProperty().multiply(0.18).divide(2))
        );
        Pane buttonLayer = new Pane();
        buttonLayer.getChildren().addAll(playBtn, creditsBtn);
        buttonLayer.setPickOnBounds(false);

        root.getChildren().addAll(background, layer2, titleLayer, buttonLayer);

        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());
        layer2.fitWidthProperty().bind(root.widthProperty());
        layer2.fitHeightProperty().bind(root.heightProperty());
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                stage.setFullScreen(!stage.isFullScreen());
        });
        playAudio("MonstersTheme.mp3");
        return scene;
    }

    private Scene createInstructionsScene(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);

        GaussianBlur blur = new GaussianBlur(20);
        background.setEffect(blur);

        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.BLACK);
        overlay.setOpacity(0.65);
        overlay.widthProperty().bind(root.widthProperty());
        overlay.heightProperty().bind(root.heightProperty());

        Label title = new Label("HOW TO PLAY");
        title.setStyle(
            "-fx-font-size: 50px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #ff6b35;" +
            "-fx-font-family: 'Impact';"
        );
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        Label instructions = new Label(
            "OBJECTIVE\n" +
            "Be the first monster to reach Boo's Door (Cell 100) with at least 1000 energy.\n\n" +

            "TURN SEQUENCE\n" +
            "1. Powerup Phase (Optional) — Activate your powerup for 500 energy.\n" +
            "2. Dice Roll — Roll a 6-sided dice to determine how many cells to move.\n" +
            "3. Movement — Move forward the rolled number of cells.\n" +
            "   • If the destination is occupied by your opponent, roll again.\n\n" +

            "CELL TYPES\n" +
            "• Door Cells — Match your role to gain energy. Mismatch loses energy.\n" +
            "• Monster Cells — Same role: free powerup. Opposite role: energy swap.\n" +
            "• Conveyor Belts — Transports you forward instantly.\n" +
            "• Contamination Socks — Transports you backward and drains 100 energy.\n" +
            "• Card Cells — Draw a card and perform its action.\n" +
            "• Normal Cells — Nothing happens.\n\n" +

            "MONSTER TYPES\n" +
            "• Dasher — Moves at 2x speed. Powerup: 3x speed for 3 turns.\n" +
            "• Dynamo — All energy gains/losses doubled. Powerup: Freeze opponent 1 turn.\n" +
            "• Multitasker — Moves at half speed but +200 to all energy changes.\n" +
            "• Schemer — +10 bonus to all energy changes. Powerup: Steal from everyone.\n\n" +

            "CARDS\n" +
            "• Position Swap — Swap places with opponent if you're behind.\n" +
            "• Start Over — Sends player or opponent back to Cell 0.\n" +
            "• Energy Steal — Steal 50, 100, or 150 energy from opponent.\n" +
            "• Super Shield — Block the next negative energy effect.\n" +
            "• Confusion — Swap both players' roles for 2-3 turns.\n\n" +

            "WIN CONDITION\n" +
            "Reach Cell 99 (Boo's Door) with 1000+ energy to win!\n" +
            "Press F to play"
        );
        instructions.setStyle(
            "-fx-font-size: 20px;" +
            "-fx-text-fill: white;" +
            "-fx-font-family: 'Arial';" +
            "-fx-line-spacing: 3px;"
        );
        instructions.setWrapText(true);
        instructions.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        instructions.setAlignment(Pos.CENTER);
        instructions.setMaxWidth(Double.MAX_VALUE);
        VBox content = new VBox(15, title, instructions);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(40, 40, 40, 40));

        root.getChildren().addAll(background, overlay, content);

        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());
        content.maxWidthProperty().bind(root.widthProperty().multiply(0.8));
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F)
                stage.setScene(createRoleSelectionScene(stage));
        });
        playAudio("pizzaParlor.mp3"); 
        return scene;
    }

    private Scene createRoleSelectionScene(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);
        GaussianBlur blur = new GaussianBlur(20);
        background.setEffect(blur);

        ImageView scarerGroup = new ImageView(new Image("ScarerPickGroup.png"));
        scarerGroup.setPreserveRatio(true);
        scarerGroup.fitWidthProperty().bind(root.widthProperty().multiply(0.30));

        ImageView scarerDesc = new ImageView(new Image("ScarerTitleDesc.png"));
        scarerDesc.setPreserveRatio(true);
        scarerDesc.fitWidthProperty().bind(root.widthProperty().multiply(0.20));
        ImageView laugherGroup = new ImageView(new Image("LaugherPickGroup.png"));
        laugherGroup.setPreserveRatio(true);
        laugherGroup.fitWidthProperty().bind(root.widthProperty().multiply(0.30));

        ImageView laugherDesc = new ImageView(new Image("LaugherTitleDesc.png"));
        laugherDesc.setPreserveRatio(true);
        laugherDesc.fitWidthProperty().bind(root.widthProperty().multiply(0.20));

        scarerGroup.layoutXProperty().bind(root.widthProperty().multiply(0.25).subtract(root.widthProperty().multiply(0.30).divide(2)));
        scarerGroup.layoutYProperty().bind(root.heightProperty().multiply(0.20));
        scarerDesc.layoutXProperty().bind(root.widthProperty().multiply(0.25).subtract(root.widthProperty().multiply(0.20).divide(2)));
        scarerDesc.layoutYProperty().bind(root.heightProperty().multiply(0.55));
        laugherGroup.layoutXProperty().bind(root.widthProperty().multiply(0.75).subtract(root.widthProperty().multiply(0.30).divide(2)));
        laugherGroup.layoutYProperty().bind(root.heightProperty().multiply(0.20));
        laugherDesc.layoutXProperty().bind(root.widthProperty().multiply(0.75).subtract(root.widthProperty().multiply(0.20).divide(2)));
        laugherDesc.layoutYProperty().bind(root.heightProperty().multiply(0.55));

        scarerGroup.setOnMouseClicked(e -> startGame(stage, Role.SCARER));
        scarerDesc.setOnMouseClicked(e -> startGame(stage, Role.SCARER));
        laugherGroup.setOnMouseClicked(e -> startGame(stage, Role.LAUGHER));
        laugherDesc.setOnMouseClicked(e -> startGame(stage, Role.LAUGHER));
        scarerGroup.setOnMouseEntered(e -> scarerGroup.setOpacity(0.8));
        scarerGroup.setOnMouseExited(e -> scarerGroup.setOpacity(1.0));
        scarerDesc.setOnMouseEntered(e -> scarerDesc.setOpacity(0.8));
        scarerDesc.setOnMouseExited(e -> scarerDesc.setOpacity(1.0));
        laugherGroup.setOnMouseEntered(e -> laugherGroup.setOpacity(0.8));
        laugherGroup.setOnMouseExited(e -> laugherGroup.setOpacity(1.0));
        laugherDesc.setOnMouseEntered(e -> laugherDesc.setOpacity(0.8));
        laugherDesc.setOnMouseExited(e -> laugherDesc.setOpacity(1.0));

        scarerGroup.setStyle("-fx-cursor: hand;");
        scarerDesc.setStyle("-fx-cursor: hand;");
        laugherGroup.setStyle("-fx-cursor: hand;");
        laugherDesc.setStyle("-fx-cursor: hand;");

        Pane contentLayer = new Pane();
        contentLayer.getChildren().addAll(scarerGroup, scarerDesc, laugherGroup, laugherDesc);
        contentLayer.setPickOnBounds(false);

        root.getChildren().addAll(background, contentLayer);

        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                stage.setScene(createTitleScene(stage));
        });
        return scene;
    }

    private Scene createCreditsScene(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        
        Rectangle background = new Rectangle();
        background.setFill(Color.BLACK);
        background.widthProperty().bind(root.widthProperty());
        background.heightProperty().bind(root.heightProperty());
        Label title = new Label("GAME MADE BY TEAM 188");
        title.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #ff6b35; -fx-font-family: 'Impact';");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        String[] members = {
            "  Youssef Ashraf Saber", "  Amr Mohamed Mossad Kandeel",
            "  Omar Osama Ahmed Rady", "  Abdulrahman Emad Eldin Adel Soliman Yousry"
        };
        VBox memberList = new VBox(20);
        memberList.setAlignment(Pos.CENTER);
        memberList.setMaxWidth(Double.MAX_VALUE);

        for (String member : members) {
            Label memberLabel = new Label(member);
            memberLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-family: 'Georgia';");
            memberLabel.setMaxWidth(Double.MAX_VALUE);
            memberLabel.setAlignment(Pos.CENTER);
            memberList.getChildren().add(memberLabel);
        }

        Label hint = new Label("Press ESC to go back");
        hint.setStyle("-fx-font-size: 18px; -fx-text-fill: #aaaaaa; -fx-font-family: 'Georgia';");
        hint.setMaxWidth(Double.MAX_VALUE);
        hint.setAlignment(Pos.CENTER);

        VBox content = new VBox(50, title, memberList, hint);
        content.setAlignment(Pos.CENTER);
        content.setMaxWidth(Double.MAX_VALUE);
        content.setPadding(new Insets(60, 40, 40, 40));

        root.getChildren().addAll(background, content);

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                stage.setScene(createTitleScene(stage));
        });
        return scene;
    }

    // ─── START GAME LOGIC ─────────────────────────────────────────────────────
    private void startGame(Stage stage, Role chosenRole) {
        try {
            activeGame = new Game(chosenRole);
            debugLog("=== GAME STARTED ===");
            debugLog("Player 1: " + activeGame.getPlayer().getName() + " (" + activeGame.getPlayer().getClass().getSimpleName() + ")");
            debugLog("Player 2: " + activeGame.getOpponent().getName() + " (" + activeGame.getOpponent().getClass().getSimpleName() + ")");
            stage.setScene(createGameBoardScene(stage, chosenRole));
        } catch (Exception ex) {
            System.out.println("Failed to load game data! Check CSV files.");
            ex.printStackTrace();
        }
    }

    private void setupDiffLabel(Label label) {
        label.setStyle("-fx-font-family: 'Impact'; -fx-font-size: 24px; -fx-font-weight: bold;");
        DropShadow ds = new DropShadow();
        ds.setColor(Color.BLACK);
        ds.setRadius(3);
        ds.setSpread(0.8);
        label.setEffect(ds);
        label.setOpacity(0);
        // Hidden by default
    }

    private Scene createGameBoardScene(Stage stage, Role playerRole) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
        
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);
        GaussianBlur blur = new GaussianBlur(20);
        background.setEffect(blur);
        
        // State trackers for enforcing game logic rules
        final boolean[] mustDrawCard = {false};
        final boolean[] isRolling = {false};
        final Card[] pendingVisualCard = {null};
        // ─── DOOR VISUALS MAP ─────────────────────────────────────────────────
        final ImageView[] doorIcons = new ImageView[100];
        // ─── DELAYED EFFECT TRACKERS ──────────────────────────────────────────
        // These lock the UI to the "pre-card" visual state until the user clicks the deck
        final boolean[] isEffectDelayed = {false};
        final int[] delayedP1Pos = {0};
        final int[] delayedP2Pos = {0};
        final int[] delayedP1Energy = {1000};
        final int[] delayedP2Energy = {1000};
        final Monster[] delayedCurrentTurn = {null};
        
        // Trackers for animation interpolation
        final int[] prevP1Pos = {0};
        final int[] prevP2Pos = {0};
        
        // Energy tracking for the visual floating text
        final Map<Monster, Integer> previousEnergies = new HashMap<>();
        previousEnergies.put(activeGame.getPlayer(), activeGame.getPlayer().getEnergy());
        previousEnergies.put(activeGame.getOpponent(), activeGame.getOpponent().getEnergy());
        for (Monster m : Board.getStationedMonsters()) {
            previousEnergies.put(m, m.getEnergy());
        }
        
        // Animation Safety Trackers to prevent token overlapping bugs
        final SequentialTransition[] p1Anim = {null};
        final SequentialTransition[] p2Anim = {null};

        // ─── VISUAL TOKENS FOR THE GRID ───────────────────────────────────────
        DropShadow ds = new DropShadow();
        ds.setRadius(4.0); ds.setOffsetX(2.0); ds.setOffsetY(2.0);
        ds.setColor(Color.color(0, 0, 0, 0.6));

        Circle playerToken = new Circle();
        playerToken.radiusProperty().bind(root.heightProperty().multiply(0.010)); 
        playerToken.setFill(Color.web("#3498db"));
        playerToken.setStroke(Color.WHITE);
        playerToken.setStrokeWidth(2);
        playerToken.setEffect(ds);
        Circle opponentToken = new Circle();
        opponentToken.radiusProperty().bind(root.heightProperty().multiply(0.010));
        opponentToken.setFill(Color.web("#e74c3c"));
        opponentToken.setStroke(Color.WHITE);
        opponentToken.setStrokeWidth(2);
        opponentToken.setEffect(ds);

        StackPane[] cellPanes = new StackPane[100];
        // ─── DYNAMIC LABELS FOR ENGINE INTEGRATION ────────────────────────────
        Label p1EnergyLabel = new Label("1000");
        Label p2EnergyLabel = new Label("1000");
        Label p1EnergyDiff = new Label();
        Label p2EnergyDiff = new Label();
        
        setupDiffLabel(p1EnergyDiff);
        setupDiffLabel(p2EnergyDiff);
        Label turnIndicator = new Label();
        turnIndicator.setStyle("-fx-text-fill: #ff6b35; -fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: 'Impact';");
        // ─── POWERUP BUTTONS ──────────────────────────────────────────────────
        Button p1PowerupBtn = createPowerupButton();
        Button p2PowerupBtn = createPowerupButton();
        StackPane player1Panel = createPlayerPanel(root, "PLAYER 1 (You)", activeGame.getPlayer(), p1EnergyLabel, p1EnergyDiff, p1PowerupBtn);
        StackPane player2Panel = createPlayerPanel(root, "PLAYER 2 (Opponent)", activeGame.getOpponent(), p2EnergyLabel, p2EnergyDiff, p2PowerupBtn);
        // ─── DYNAMIC UPDATE METHOD ────────────────────────────────────────────
        Runnable updateUI = () -> {
            Monster p1 = activeGame.getPlayer();
            Monster p2 = activeGame.getOpponent();

            // Override values if we are delaying the card effect
            int p1Pos = isEffectDelayed[0] ?
            delayedP1Pos[0] : p1.getPosition();
            int p2Pos = isEffectDelayed[0] ? delayedP2Pos[0] : p2.getPosition();
            int p1Energy = isEffectDelayed[0] ? delayedP1Energy[0] : p1.getEnergy();
            int p2Energy = isEffectDelayed[0] ? delayedP2Energy[0] : p2.getEnergy();
            Monster curr = isEffectDelayed[0] ? delayedCurrentTurn[0] : activeGame.getCurrent();

            p1EnergyLabel.setText(String.valueOf(p1Energy));
            p2EnergyLabel.setText(String.valueOf(p2Energy));
            Monster winner = activeGame.getWinner();
            if (winner != null && !isEffectDelayed[0]) {
                turnIndicator.setText(winner.getName().toUpperCase() + " WINS!!!");
            } else if (mustDrawCard[0]) {
                turnIndicator.setText("DRAW YOUR CARD!");
            } else {
                turnIndicator.setText(curr == p1 ? "YOUR TURN!" : "OPPONENT'S TURN!");
            }

            double p1TargetX = (p1Pos == p2Pos) ?
            -8 : 0;
            double p2TargetX = (p1Pos == p2Pos) ? 8 : 0;
            // Safely remove tokens
            if (playerToken.getParent() != null) ((Pane) playerToken.getParent()).getChildren().remove(playerToken);
            if (opponentToken.getParent() != null) ((Pane) opponentToken.getParent()).getChildren().remove(opponentToken);

            // Add tokens to destination
            if (cellPanes[p1Pos] != null) {
                cellPanes[p1Pos].getChildren().add(playerToken);
                cellPanes[p1Pos].toFront();
            }
            if (cellPanes[p2Pos] != null) {
                cellPanes[p2Pos].getChildren().add(opponentToken);
                cellPanes[p2Pos].toFront();
            }

            if (p1Anim[0] != null) p1Anim[0].stop();
            if (p2Anim[0] != null) p2Anim[0].stop();

            p1Anim[0] = animateToken(playerToken, prevP1Pos[0], p1Pos, p1TargetX, 0, cellPanes);
            p2Anim[0] = animateToken(opponentToken, prevP2Pos[0], p2Pos, p2TargetX, 0, cellPanes);

            if (p1Anim[0] != null) p1Anim[0].play();
            if (p2Anim[0] != null) p2Anim[0].play();
            prevP1Pos[0] = p1Pos;
            prevP2Pos[0] = p2Pos;
            
            // Generate visual floating text for energy changes exclusively next to the player's energy bar
            if (!isEffectDelayed[0]) {
                int p1Diff = p1.getEnergy() - previousEnergies.get(p1);
                if (p1Diff != 0) {
                    animateDiffLabel(p1EnergyDiff, p1Diff);
                    previousEnergies.put(p1, p1.getEnergy());
                }
                
                int p2Diff = p2.getEnergy() - previousEnergies.get(p2);
                if (p2Diff != 0) {
                    animateDiffLabel(p2EnergyDiff, p2Diff);
                    previousEnergies.put(p2, p2.getEnergy());
                }
                
                // Track stationed monsters silently without displaying text on the grid board
                for (Monster m : Board.getStationedMonsters()) {
                    previousEnergies.put(m, m.getEnergy());
                }
            }
        };
        // Actions for PowerUp Buttons
        p1PowerupBtn.setOnAction(e -> {
            if (activeGame.getCurrent() != activeGame.getPlayer()) {
                turnIndicator.setText("NOT YOUR TURN!");
            } else if (activeGame.getPlayer().getEnergy() < Constants.POWERUP_COST) {
                turnIndicator.setText("NOT ENOUGH ENERGY (Need 500)");
            } else 
            {
                try { 
                    debugLog("=== P1 POWERUP ATTEMPT ===");
                    activeGame.usePowerup(); 
                    updateUI.run(); 
                
                    turnIndicator.setText("POWERUP ACTIVATED!");
                } catch (Exception ex) {
                    debugLog("Powerup Failed: " + ex.getMessage());
                }
            }
        });
        p2PowerupBtn.setOnAction(e -> {
            if (activeGame.getCurrent() != activeGame.getOpponent()) {
                turnIndicator.setText("NOT YOUR TURN!");
            } else if (activeGame.getOpponent().getEnergy() < Constants.POWERUP_COST) {
                turnIndicator.setText("NOT ENOUGH ENERGY (Need 500)");
            } else {
            
                try { 
                    debugLog("=== P2 POWERUP ATTEMPT ===");
                    activeGame.usePowerup(); 
                    updateUI.run(); 
                    turnIndicator.setText("POWERUP ACTIVATED!");
       
                } catch (Exception ex) {
                    debugLog("Powerup Failed: " + ex.getMessage());
                }
            }
        });
        // ─── DICE ─────────────────────────────────────────────────────────────
        Label diceLabel = new Label("?");
        diceLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-family: 'Impact';");

        StackPane diceBox = new StackPane(diceLabel);
        diceBox.setMinSize(80, 80);
        diceBox.setMaxSize(80, 80);
        diceBox.setStyle("-fx-background-color: white; -fx-background-radius: 12px; -fx-border-color: #cccccc; -fx-border-radius: 12px; -fx-border-width: 4px; -fx-cursor: hand;");
        diceBox.setOnMouseClicked(e -> {
            if (isRolling[0] || activeGame.getWinner() != null) return;
            if (mustDrawCard[0]) {
                turnIndicator.setText("DRAW YOUR CARD FIRST!");
                return;
            }
            
       
            isRolling[0] = true;
            Timeline timeline = new Timeline();
            for (int i = 0; i < 10; i++) {
                KeyFrame frame = new KeyFrame(Duration.millis(i * 80), ev -> {
                    diceLabel.setText(String.valueOf((int)(Math.random() * 6) + 1));
       
                });
                timeline.getKeyFrames().add(frame);
            }
            KeyFrame finalFrame = new KeyFrame(Duration.millis(10 * 80), ev -> {
                
                Monster playingMonster = activeGame.getCurrent();
       
                boolean isP1 = (playingMonster == activeGame.getPlayer());
                
                debugLog("\n====== TURN START: " + playingMonster.getName() + " ======");
                
                int oldP1Pos = activeGame.getPlayer().getPosition();
                int oldP2Pos = activeGame.getOpponent().getPosition();
                int oldP1Energy = activeGame.getPlayer().getEnergy();
                int oldP2Energy = activeGame.getOpponent().getEnergy();
                int oldPos = playingMonster.getPosition();
                int momentumBefore = (playingMonster instanceof Dasher) ? ((Dasher)playingMonster).getMomentumTurns() : 0;
                int focusBefore = (playingMonster instanceof MultiTasker) ? ((MultiTasker)playingMonster).getNormalSpeedTurns() : 0;
                ArrayList<Card> originalDeck = Board.getCards();
                if (originalDeck.isEmpty()) { 
                    Board.reloadCards();
                    originalDeck = Board.getCards(); 
                }
                
                Card expectedCard = originalDeck.get(0);
                ArrayList<Card> singleCardDeck = new ArrayList<>();
                singleCardDeck.add(expectedCard);
                Board.setCards(singleCardDeck);
                
                try {
                    activeGame.playTurn();
                    int roll = activeGame.getLastRoll();
                    debugLog(">>> Dice Rolled: " + roll + " <<<");
                    
                    int distance = roll;
                    if (playingMonster instanceof Dasher) {
                        distance *= (momentumBefore > 0) ?
                        3 : 2;
                    } else if (playingMonster instanceof MultiTasker) {
                        distance /= (focusBefore > 0) ?
                        1 : 2;
                    }
                    int expectedLandPos = (oldPos + distance) % 100;
                    // --- PATCH: Implement missing backend Door Cell logic ---
                    int cols = Constants.BOARD_COLS;
                    int r = expectedLandPos / cols;
                    int c = expectedLandPos % cols;
                    if (r % 2 == 1) c = cols - 1 - c;
                    Cell landedCell = activeGame.getBoard().getBoardCells()[r][c];
                    if (landedCell instanceof DoorCell && roll != 0) {
                        DoorCell door = (DoorCell) landedCell;
                        if (!door.isActivated()) {
                            door.setActivated(true);
                            // Make door look exhausted visually
                            ImageView doorImg = doorIcons[expectedLandPos];
                            if (doorImg != null) {
                                ColorAdjust grayscale = new ColorAdjust();
                                grayscale.setSaturation(-1);
                                grayscale.setBrightness(-0.4);
                                doorImg.setEffect(grayscale);
                            }
                            
                            boolean isMatch = (playingMonster.getRole() == door.getRole());
                            ArrayList<Monster> team = new ArrayList<>();
                            team.add(playingMonster);
                            for (Monster m : Board.getStationedMonsters()) {
                                if (m.getRole() == playingMonster.getRole()) team.add(m);
                            }
                            
                            if (isMatch) {
                                for (Monster m : team) {
     
                                    door.modifyCanisterEnergy(m, door.getEnergy());
                                }
                            } else {
                                boolean shieldActive = false;
                                for (Monster m : team) {
                                    if (m.isShielded()) {
                                        shieldActive = true;
                                        m.setShielded(false);
                                        break;
                                    }
                                }
                                if (!shieldActive) {
                                
                                    for (Monster m : team) {
                                        door.modifyCanisterEnergy(m, -door.getEnergy());
                                    }
                                }
                            }
                        }
                
                    }
                    // --------------------------------------------------------

                    boolean cardWasDrawn = isCardCell(expectedLandPos);
                    Board.setCards(originalDeck);
                    
                    if (cardWasDrawn && roll != 0) {
                        Board.getCards().remove(expectedCard);
                        mustDrawCard[0] = true;
                        pendingVisualCard[0] = expectedCard;
                        
                        isEffectDelayed[0] = true;
                        delayedP1Pos[0] = isP1 ? expectedLandPos : oldP1Pos;
                        delayedP2Pos[0] = !isP1 ?
                        expectedLandPos : oldP2Pos;
                        delayedP1Energy[0] = oldP1Energy;
                        delayedP2Energy[0] = oldP2Energy;
                        delayedCurrentTurn[0] = playingMonster;
                    } else {
                        isEffectDelayed[0] = false;
                    }

                    diceLabel.setText(roll == 0 ? "X" : String.valueOf(roll));
                    updateUI.run();
                } catch (InvalidMoveException ex) {
                    Board.setCards(originalDeck);
                    diceLabel.setText(String.valueOf(activeGame.getLastRoll()));
                    turnIndicator.setText("CELL OCCUPIED! ROLL AGAIN!");
                } catch (Exception ex) {
                    Board.setCards(originalDeck);
                }
                isRolling[0] = false;
            });
            timeline.getKeyFrames().add(finalFrame);
            timeline.play();
        });

        // ─── CARD DRAWING ───
        ImageView cardBack = new ImageView(new Image("Cards/cardstack.png"));
        cardBack.setPreserveRatio(false);
        cardBack.fitWidthProperty().bind(root.widthProperty().multiply(0.11));
        cardBack.fitHeightProperty().bind(root.heightProperty().multiply(0.32));
        cardBack.setStyle("-fx-cursor: hand;");

        ImageView cardPicked = new ImageView();
        cardPicked.setPreserveRatio(false);
        cardPicked.fitWidthProperty().bind(root.widthProperty().multiply(0.11));
        cardPicked.fitHeightProperty().bind(root.heightProperty().multiply(0.32));
        cardPicked.setVisible(false);

        ImageView overlayCard = new ImageView();
        overlayCard.setPreserveRatio(false);
        overlayCard.fitWidthProperty().bind(root.widthProperty().multiply(0.18));
        overlayCard.fitHeightProperty().bind(root.heightProperty().multiply(0.50));
        overlayCard.setVisible(false);

        Rectangle overlayBg = new Rectangle();
        overlayBg.setFill(Color.BLACK);
        overlayBg.setOpacity(0.7);
        overlayBg.setVisible(false);
        overlayBg.widthProperty().bind(root.widthProperty());
        overlayBg.heightProperty().bind(root.heightProperty());

        overlayBg.setOnMouseClicked(e -> { overlayBg.setVisible(false); overlayCard.setVisible(false); cardPicked.setVisible(true); });
        overlayCard.setOnMouseClicked(e -> { overlayBg.setVisible(false); overlayCard.setVisible(false); cardPicked.setVisible(true); });

        cardBack.setOnMouseClicked(e -> {
            if (!mustDrawCard[0]) return; 
            mustDrawCard[0] = false; 
            
            try {
                Card drawnCard = pendingVisualCard[0];
                if 
                (drawnCard == null) return;
                
                String imageFileName = drawnCard.getName().replace(" ", "") + ".png";
                Image realCardImage = new Image("Cards/" + imageFileName);
                
                overlayCard.setImage(realCardImage);
    
                cardPicked.setImage(realCardImage);
                cardPicked.setVisible(false);

                ScaleTransition shrink = new ScaleTransition(Duration.millis(150), overlayCard);
                shrink.setFromX(1); shrink.setToX(0);

                ScaleTransition grow = new ScaleTransition(Duration.millis(150), overlayCard);
             
                grow.setFromX(0); grow.setToX(1);
                
                shrink.setOnFinished(ev -> {
                    overlayCard.setVisible(true);
                    grow.play();
                    
                    isEffectDelayed[0] = false;
                    updateUI.run(); 
                });
                
                overlayBg.setVisible(true);
                overlayCard.setVisible(false);
                shrink.play();
                
            } catch (Exception ex) {
                System.out.println("Failed to perform card logic: " + ex.getMessage());
            }
        });

        HBox cardsRow = new HBox();
        cardsRow.setAlignment(Pos.BOTTOM_LEFT);
        cardsRow.spacingProperty().bind(root.widthProperty().multiply(0.01));
        
        cardsRow.translateYProperty().bind(root.heightProperty().multiply(-0.12)); 
        cardsRow.translateXProperty().bind(root.widthProperty().multiply(0.14));  
        cardsRow.getChildren().addAll(cardBack, cardPicked);
        // ─── LEFT PANEL ASSEMBLY ───────────────────────────────────────────────
        HBox diceRow = new HBox(15, new Label("Roll:"), diceBox);
        diceRow.setAlignment(Pos.CENTER_LEFT);
        diceRow.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-family: 'Impact';");

        VBox leftPanel = new VBox(5, player1Panel, player2Panel, turnIndicator, diceRow, cardsRow);
        leftPanel.setAlignment(Pos.TOP_LEFT);
        leftPanel.setPadding(new Insets(0, 10, 10, 10));
        leftPanel.prefWidthProperty().bind(root.widthProperty().multiply(0.35));
        leftPanel.prefHeightProperty().bind(root.heightProperty());

        // ─── GRID ─────────────────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.hgapProperty().bind(root.widthProperty().multiply(0.003));
        grid.vgapProperty().bind(root.heightProperty().multiply(0.005));
        grid.paddingProperty().bind(root.widthProperty().asObject().map(w -> new Insets(w.doubleValue() * 0.008)));

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int cellNumber = getCellNumber(row, col);
                int engineIndex = cellNumber - 1;

                StackPane cell = new StackPane();
                cell.prefWidthProperty().bind(root.heightProperty().multiply(0.85).divide(10).subtract(4));
                cell.prefHeightProperty().bind(root.heightProperty().multiply(0.85).divide(10).subtract(4));
                if (engineIndex == 99) {
                    cell.setStyle("-fx-background-color: #FFD700; -fx-border-color: #FFA500; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    Label winLabel = new Label("BOO'S\nDOOR");
                    winLabel.setStyle("-fx-font-size: 8px; -fx-text-fill: #8B0000; -fx-font-weight: bold;");
                    winLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                    cell.getChildren().add(winLabel);
                } else if (engineIndex == 0) {
                    cell.setStyle("-fx-background-color: #c8f0c8; -fx-border-color: #4caf50; -fx-border-width: 2px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    Label startLabel = new Label("START");
                    startLabel.setStyle("-fx-font-size: 8px; -fx-text-fill: #1a5c1a; -fx-font-weight: bold;");
                    cell.getChildren().add(startLabel);
                } else if (containsIndex(Constants.MONSTER_CELL_INDICES, engineIndex)) {
                    cell.setStyle("-fx-background-color: #4a90d9; -fx-border-color: #2c5f8a; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    String monsterImg = getMonsterImageForCell(engineIndex);
                    if (monsterImg != null) {
                        ImageView monsterView = new ImageView(new Image(monsterImg));
                        monsterView.setPreserveRatio(true);
                        monsterView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.80));
                        monsterView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.80));
                        cell.getChildren().add(monsterView);
                    }

                } else if (containsIndex(Constants.CONVEYOR_CELL_INDICES, engineIndex)) {
                    cell.setStyle("-fx-background-color: #5cb85c; -fx-border-color: #3d7a3d; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    ImageView conveyorView = new ImageView(new Image("conveyor_belt.png"));
                    conveyorView.setPreserveRatio(true);
                    conveyorView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.85));
                    conveyorView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.85));
                    cell.getChildren().add(conveyorView);
                } else if (containsIndex(Constants.SOCK_CELL_INDICES, engineIndex)) {
                    cell.setStyle("-fx-background-color: #e8a838; -fx-border-color: #b07820; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                    ImageView sockView = new ImageView(new Image("socks.png"));
                    sockView.setPreserveRatio(true);
                    sockView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.75));
                    sockView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.75));
                    cell.getChildren().add(sockView);
                } else if (containsIndex(Constants.CARD_CELL_INDICES, engineIndex)) {
                    cell.setStyle("-fx-background-color: #d9534f; -fx-border-color: #8a2c2c; -fx-border-width: 1px; -background-radius: 6px; -fx-border-radius: 6px;");
                    ImageView cardView = new ImageView(new Image("Cards/CardBack.png"));
                    cardView.setPreserveRatio(true);
                    cardView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.65));
                    cardView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.85));
                    cell.getChildren().add(cardView);
                } else if (engineIndex % 2 != 0) {
                    boolean isScarerDoor = ((engineIndex - 1) / 2) % 2 == 0;
                    if (isScarerDoor) {
                        cell.setStyle("-fx-background-color: #ffb6c1; -fx-border-color: #cc4466; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                        ImageView doorView = new ImageView(new Image("PinkDoor.png"));
                        doorView.setPreserveRatio(true);
                        doorView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.60));
                        doorView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.85));
                        doorIcons[engineIndex] = doorView;
                        cell.getChildren().add(doorView);
                    } else {
                        cell.setStyle("-fx-background-color: #aec6f0; -fx-border-color: #4a7ac2; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                        ImageView doorView = new ImageView(new Image("BlueDoor.png"));
                        doorView.setPreserveRatio(true);
                        doorView.fitWidthProperty().bind(cell.prefWidthProperty().multiply(0.60));
                        doorView.fitHeightProperty().bind(cell.prefHeightProperty().multiply(0.85));
                        doorIcons[engineIndex] = doorView;
                        cell.getChildren().add(doorView);
                    }

                } else {
                    cell.setStyle("-fx-background-color: #f5f0d0; -fx-border-color: #c8b870; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                }

                Label numberLabel = new Label(String.valueOf(cellNumber));
                numberLabel.styleProperty().bind(
                    root.heightProperty().multiply(0.013)
                    .asString("-fx-font-size: %.0fpx; -fx-text-fill: #333333; -fx-font-weight: bold;")
                );
                StackPane.setAlignment(numberLabel, Pos.TOP_LEFT);
                numberLabel.translateXProperty().bind(root.widthProperty().multiply(0.003));
                numberLabel.translateYProperty().bind(root.heightProperty().multiply(0.003));
                cell.getChildren().add(numberLabel);

                cellPanes[engineIndex] = cell;
                grid.add(cell, col, row);
            }
        }

        StackPane gridContainer = new StackPane(grid);
        gridContainer.setAlignment(Pos.CENTER);
        gridContainer.prefWidthProperty().bind(root.widthProperty().multiply(0.62));
        gridContainer.paddingProperty().bind(root.widthProperty().asObject().map(w -> 
            new Insets(w.doubleValue() * 0.015, w.doubleValue() * 0.02, w.doubleValue() * 0.015, 0)
        ));
        StackPane.setAlignment(gridContainer, Pos.CENTER_RIGHT);

        updateUI.run();

        // ─── MAIN LAYOUT ──────────────────────────────────────────────────────
        HBox mainLayout = new HBox(leftPanel, gridContainer);
        mainLayout.setAlignment(Pos.CENTER);

        StackPane.setAlignment(overlayCard, Pos.CENTER);
        StackPane.setMargin(overlayCard, new Insets(150, 0, 0, 0));

        root.getChildren().addAll(background, mainLayout, overlayBg, overlayCard);

        background.fitWidthProperty().bind(root.widthProperty());
        background.fitHeightProperty().bind(root.heightProperty());
        scene.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) stage.setScene(createRoleSelectionScene(stage));
        });
        return scene;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────
    
    // VISUAL ENERGY INDICATOR ANIMATION
    private void animateDiffLabel(Label label, int diff) {
        label.setText((diff > 0 ? "+" : "") + diff);
        label.setTextFill(diff > 0 ? Color.LIMEGREEN : Color.RED);
        label.setOpacity(1.0);
        label.setTranslateY(0);
        label.setTranslateX(0);
        label.setScaleX(1.0);
        label.setScaleY(1.0);
        
        TranslateTransition tt = new TranslateTransition(Duration.seconds(2), label);
        tt.setByY(-40);
        ScaleTransition st = new ScaleTransition(Duration.seconds(2), label);
        st.setToX(1.2); st.setToY(1.2);
        
        FadeTransition ft = new FadeTransition(Duration.seconds(2), label);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ParallelTransition pt = new ParallelTransition(tt, st, ft);
        pt.play();
    }

    private SequentialTransition animateToken(Node token, int oldPos, int newPos, double targetX, double targetY, StackPane[] cellPanes) {
        if (oldPos == newPos || cellPanes[oldPos] == null || cellPanes[newPos] == null) {
            token.setTranslateX(targetX);
            token.setTranslateY(targetY);
            token.setScaleX(1.0);
            token.setScaleY(1.0);
            token.setRotate(0);
            return null;
        }

        SequentialTransition seq = new SequentialTransition();
        int intermediatePos = newPos;

        if (containsIndex(Constants.CARD_CELL_INDICES, oldPos)) {
            intermediatePos = oldPos;
        } 
        else if (newPos < oldPos) {
            for (int i = oldPos + 1; i <= Math.min(99, oldPos + 18); i++) {
                if (containsIndex(Constants.SOCK_CELL_INDICES, i)) {
                    intermediatePos = i;
                    break;
                }
            }
        } 
        else {
            int foundConveyor = -1;
            for (int i = oldPos + 1; i <= Math.min(99, oldPos + 18); i++) {
                if (containsIndex(Constants.CONVEYOR_CELL_INDICES, i)) {
                    foundConveyor = i;
                    break;
                }
            }
            if (foundConveyor != -1 && newPos >= foundConveyor + 6) {
                intermediatePos = foundConveyor;
            }
        }

        if (intermediatePos > oldPos) {
            token.setTranslateX(cellPanes[oldPos].getLayoutX() - cellPanes[newPos].getLayoutX() + targetX);
            token.setTranslateY(cellPanes[oldPos].getLayoutY() - cellPanes[newPos].getLayoutY() + targetY);

            for (int i = oldPos; i < intermediatePos; i++) {
                TranslateTransition step = new TranslateTransition(Duration.millis(300), token);
                step.setFromX(cellPanes[i].getLayoutX() - cellPanes[newPos].getLayoutX() + targetX);
                step.setFromY(cellPanes[i].getLayoutY() - cellPanes[newPos].getLayoutY() + targetY);
                step.setToX(cellPanes[i+1].getLayoutX() - cellPanes[newPos].getLayoutX() + targetX);
                step.setToY(cellPanes[i+1].getLayoutY() - cellPanes[newPos].getLayoutY() + targetY);
                seq.getChildren().add(step);
            }
        } else {
            intermediatePos = oldPos;
            token.setTranslateX(cellPanes[oldPos].getLayoutX() - cellPanes[newPos].getLayoutX() + targetX);
            token.setTranslateY(cellPanes[oldPos].getLayoutY() - cellPanes[newPos].getLayoutY() + targetY);
        }

        if (intermediatePos != newPos) {
            PauseTransition pause = new PauseTransition(Duration.millis(600));
            seq.getChildren().add(pause);

            TranslateTransition jump = new TranslateTransition(Duration.millis(900), token);
            jump.setFromX(cellPanes[intermediatePos].getLayoutX() - cellPanes[newPos].getLayoutX() + targetX);
            jump.setFromY(cellPanes[intermediatePos].getLayoutY() - cellPanes[newPos].getLayoutY() + targetY);
            jump.setToX(targetX);
            jump.setToY(targetY);
            RotateTransition rt = new RotateTransition(Duration.millis(900), token);
            rt.setByAngle(720);

            ScaleTransition st = new ScaleTransition(Duration.millis(450), token);
            st.setFromX(1.0); st.setFromY(1.0);
            st.setToX(1.6); st.setToY(1.6);
            st.setAutoReverse(true);
            st.setCycleCount(2);
            ParallelTransition jumpPt = new ParallelTransition(jump, rt, st);
            seq.getChildren().add(jumpPt);
        }

        return seq;
    }

    private StackPane createPlayerPanel(Pane root, String titleName, Monster monster, Label energyLabel, Label diffLabel, Button powerupBtn) {
        // Monster image
    	ImageView monsterImg = new ImageView(new Image(getMonsterImageByName(monster.getName())));
    	monsterImg.setPreserveRatio(true);
    	monsterImg.fitWidthProperty().bind(root.widthProperty().multiply(0.08));
        monsterImg.fitHeightProperty().bind(root.heightProperty().multiply(0.08));

        // Title label
        Label titleLabel = new Label(titleName);
        titleLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-family: 'Impact'; -fx-text-fill: #ff6b35; -fx-font-size: ",
            root.heightProperty().multiply(0.022).asString("%.0f"),
            "px;"
        ));
        // Monster name — bigger font
        Label nameLabel = new Label(monster.getName());
        nameLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-family: 'Impact'; -fx-text-fill: white; -fx-font-size: ",
            root.heightProperty().multiply(0.024).asString("%.0f"),
            "px;"
        ));
        VBox nameTextBox = new VBox(1, titleLabel, nameLabel);
        nameTextBox.setAlignment(Pos.CENTER_LEFT);

        HBox nameRow = new HBox(8, monsterImg, nameTextBox);
        nameRow.setAlignment(Pos.CENTER_LEFT);
        // Type brushstroke
        ImageView brushBg = new ImageView(new Image("titlescreenbuttonbackground.png"));
        brushBg.setPreserveRatio(false);
        brushBg.fitWidthProperty().bind(root.widthProperty().multiply(0.15));
        brushBg.fitHeightProperty().bind(root.heightProperty().multiply(0.04));
        Label typeLabel = new Label(monster.getClass().getSimpleName());
        typeLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-family: 'Impact'; -fx-text-fill: #FFD700; -fx-font-size: ",
            root.heightProperty().multiply(0.022).asString("%.0f"),
            "px;"
        ));
        StackPane typeBox = new StackPane(brushBg, typeLabel);
        StackPane.setAlignment(typeLabel, Pos.CENTER);

        // Canister + energy
        ImageView canisterImg = new ImageView(new Image("Scream_Canister.png"));
        canisterImg.setPreserveRatio(true);
        canisterImg.fitWidthProperty().bind(root.widthProperty().multiply(0.018));

        energyLabel.styleProperty().bind(Bindings.concat(
            "-fx-font-family: 'Impact'; -fx-text-fill: white; -fx-font-size: ",
            root.heightProperty().multiply(0.022).asString("%.0f"),
            "px;"
        ));
        // This is where the diff label is injected right next to the energy label!
        HBox energyRow = new HBox(8, canisterImg, energyLabel, diffLabel);
        energyRow.setAlignment(Pos.CENTER_LEFT);

        // Compact black box
        VBox blackArea = new VBox(5, energyRow, powerupBtn);
        blackArea.setStyle(
            "-fx-background-color: rgba(0,0,0,0.5);" +
            "-fx-background-radius: 10px;" +
            "-fx-padding: 6;"
        );
        blackArea.prefWidthProperty().bind(root.widthProperty().multiply(0.18));

        // Panel background
        ImageView panelBg = new ImageView(new Image("titlescreenbuttonbackground.png"));
        panelBg.setPreserveRatio(false);
        panelBg.fitWidthProperty().bind(root.widthProperty().multiply(0.28));
        panelBg.fitHeightProperty().bind(root.heightProperty().multiply(0.19));
        panelBg.setOpacity(0.4);

        VBox content = new VBox(4, nameRow, typeBox, blackArea);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(20, 8, 8, 8));
        StackPane panelStack = new StackPane(panelBg, content);
        panelStack.setAlignment(Pos.TOP_LEFT);

        return panelStack;
    }

    private Button createPowerupButton() {
        Button btn = new Button("Use PowerUp (500 Energy)");
        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Impact'; -fx-cursor: hand; -fx-background-radius: 8px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Impact'; -fx-cursor: hand; -fx-background-radius: 8px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Impact'; -fx-cursor: hand; -fx-background-radius: 8px;"));
        btn.setMaxWidth(Double.MAX_VALUE); 
        return btn;
    }

    private StackPane createImageButton(String brushstrokePath, String labelImagePath, Pane root, double widthRatio, double heightRatio, double width_enhancer) {
        ImageView brush = new ImageView(new Image(brushstrokePath));
        brush.setPreserveRatio(false);
        brush.fitWidthProperty().bind(root.widthProperty().multiply(widthRatio));
        brush.fitHeightProperty().bind(root.heightProperty().multiply(heightRatio));

        ImageView label = new ImageView(new Image(labelImagePath));
        label.setPreserveRatio(true);
        label.fitWidthProperty().bind(root.widthProperty().multiply(widthRatio * width_enhancer));

        StackPane btn = new StackPane(brush, label);
        btn.setStyle("-fx-cursor: hand;");
        btn.setPickOnBounds(true);
        btn.setOnMouseEntered(e -> { btn.setOpacity(0.8); btn.setScaleX(1.05); btn.setScaleY(1.05); });
        btn.setOnMouseExited(e -> { btn.setOpacity(1.0); btn.setScaleX(1.0); btn.setScaleY(1.0); });
        return btn;
    }
    private String getMonsterImageByName(String name) {
        switch (name) {
            case "James P. Sullivan": return "Monsters/char_sulley.png";
            case "Mike Wazowski":     return "Monsters/char_mike.png";
            case "Randall Boggs":     return "Monsters/char_randall.png";
            case "Celia Mae":         return "Monsters/char_celia.png";
            case "Roz":               return "Monsters/char_roz.png";
            case "Fungus":            return "Monsters/char_fungus.png";
            case "Henry J. Waternoose": return "Monsters/char_waternoose.png";
            case "Yeti":              return "Monsters/char_yeti.png";
            default:                  return "Monsters/char_sulley.png";
        }
    }
    private int getCellNumber(int row, int col) {
        int boardRow = 9 - row;
        if (boardRow % 2 == 0) {
            return boardRow * 10 + col + 1;
        } else {
            return boardRow * 10 + (9 - col) + 1;
        }
    }
    private boolean containsIndex(int[] arr, int target) {
        for (int val : arr)
            if (val == target) return true;
        return false;
    }

    private String getMonsterImageForCell(int cellIndex) {
        switch (cellIndex) {
            case 2:  return "Monsters/char_randall.png";
            case 18: return "Monsters/char_roz.png";
            case 34: return "Monsters/char_waternoose.png";
            case 54: return "Monsters/char_celia.png";
            case 82: return "Monsters/char_fungus.png";
            case 88: return "Monsters/char_yeti.png";
            default: return null;
        }
    }
    
    private void playAudio(String filename) {
        if (mediaPlayer != null) { mediaPlayer.stop();
        mediaPlayer.dispose(); }
        try {
            Media media = new Media(new File(filename).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        } catch (Exception e) {
            System.out.println("Audio not found: " + filename);
        }
    }
    
    public static void main(String[] args) {
        launch();
    }
}