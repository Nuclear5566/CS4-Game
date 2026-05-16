package game.engine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import game.engine.cards.Card;
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
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
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

    public Game() {}

    public Game(Role playerRole) throws IOException {
        this.board = new Board(DataLoader.readCards());
        this.allMonsters = DataLoader.readMonsters();
        
        // 1. Assign Player Role Randomly
        this.player = selectRandomMonsterByRole(playerRole, null);
        
        // Remove active player from the pool
        allMonsters.remove(this.player);
        
        // 2. Assign Opponent to the opposite team randomly, ensuring they are NOT the same monster type!
        Role oppRole = (playerRole == Role.SCARER) ? Role.LAUGHER : Role.SCARER;
        this.opponent = selectRandomMonsterByRole(oppRole, this.player.getClass());
        this.current = this.player;
        
        // Remove active opponent from the pool
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

    // Excludes specific monster types so both players aren't Dynamos, Dashers, etc.
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
    }

    public void playTurn() throws InvalidMoveException {
        if (current.isFrozen()) {
            System.out.println(current.getName() + " is frozen! Turn skipped.");
            current.setFrozen(false);
            lastRoll = 0; // Visual indicator that turn was skipped
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
        primaryStage.setScene(createTitleScene(primaryStage));
        primaryStage.setMaximized(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.setFullScreen(true);
        primaryStage.show();
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(500);
    }

    private Scene createTitleScene(Stage stage) {
        stage.getIcons().add(new Image("gameNameTitle.png"));

        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);

        ImageView layer2 = new ImageView(new Image("titlegradientRectangle.png"));
        layer2.setPreserveRatio(false);

        ImageView titleLogo = new ImageView(new Image("gameNameTitle.png"));
        titleLogo.setPreserveRatio(true);
        titleLogo.fitWidthProperty().bind(stage.widthProperty().multiply(0.4));

        Pane titleLayer = new Pane();
        titleLogo.layoutXProperty().bind(
            stage.widthProperty().multiply(0.25)
            .subtract(stage.widthProperty().multiply(0.4).divide(2))
        );
        titleLogo.layoutYProperty().bind(
            stage.heightProperty().multiply(0.5)
            .subtract(titleLogo.fitWidthProperty().divide(2))
        );
        titleLayer.getChildren().add(titleLogo);
        titleLayer.setPickOnBounds(false);

        StackPane playBtn = createImageButton("titlescreenbuttonbackground.png", "PLAY.png", stage, 0.22, 0.18, 0.8);
        StackPane creditsBtn = createImageButton("titlescreenbuttonbackground.png", "CREDITS.png", stage, 0.22, 0.18, 0.6);

        playBtn.setOnMouseClicked(e -> stage.setScene(createInstructionsScene(stage)));
        creditsBtn.setOnMouseClicked(e -> stage.setScene(createCreditsScene(stage)));

        playBtn.setPickOnBounds(true);
        creditsBtn.setPickOnBounds(true);

        playBtn.layoutXProperty().bind(
            stage.widthProperty().multiply(0.75)
            .subtract(stage.widthProperty().multiply(0.01).divide(2))
        );
        playBtn.layoutYProperty().bind(
            stage.heightProperty().multiply(0.55)
            .subtract(stage.heightProperty().multiply(0.18).divide(2))
        );
        creditsBtn.layoutXProperty().bind(
            stage.widthProperty().multiply(0.95)
            .subtract(stage.widthProperty().multiply(0.40).divide(2))
        );
        creditsBtn.layoutYProperty().bind(
            stage.heightProperty().multiply(0.73)
            .subtract(stage.heightProperty().multiply(0.18).divide(2))
        );

        Pane buttonLayer = new Pane();
        buttonLayer.getChildren().addAll(playBtn, creditsBtn);
        buttonLayer.setPickOnBounds(false);

        StackPane root = new StackPane(background, layer2, titleLayer, buttonLayer);
        Scene scene = new Scene(root);

        background.fitWidthProperty().bind(scene.widthProperty());
        background.fitHeightProperty().bind(scene.heightProperty());
        layer2.fitWidthProperty().bind(scene.widthProperty());
        layer2.fitHeightProperty().bind(scene.heightProperty());

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                stage.setFullScreen(!stage.isFullScreen());
        });
        playAudio("MonstersTheme.mp3");
        return scene;
    }

    private Scene createInstructionsScene(Stage stage) {
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);

        GaussianBlur blur = new GaussianBlur(20);
        background.setEffect(blur);

        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.BLACK);
        overlay.setOpacity(0.65);
        overlay.widthProperty().bind(stage.widthProperty());
        overlay.heightProperty().bind(stage.heightProperty());

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

        StackPane root = new StackPane(background, overlay, content);
        Scene scene = new Scene(root);

        background.fitWidthProperty().bind(scene.widthProperty());
        background.fitHeightProperty().bind(scene.heightProperty());
        content.maxWidthProperty().bind(scene.widthProperty().multiply(0.8));

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F)
                stage.setScene(createRoleSelectionScene(stage));
        });
        playAudio("pizzaParlor.mp3"); 
        return scene;
    }

    private Scene createRoleSelectionScene(Stage stage) {
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);
        GaussianBlur blur = new GaussianBlur(20);
        background.setEffect(blur);

        ImageView scarerGroup = new ImageView(new Image("ScarerPickGroup.png"));
        scarerGroup.setPreserveRatio(true);
        scarerGroup.fitWidthProperty().bind(stage.widthProperty().multiply(0.30));

        ImageView scarerDesc = new ImageView(new Image("ScarerTitleDesc.png"));
        scarerDesc.setPreserveRatio(true);
        scarerDesc.fitWidthProperty().bind(stage.widthProperty().multiply(0.20));

        ImageView laugherGroup = new ImageView(new Image("LaugherPickGroup.png"));
        laugherGroup.setPreserveRatio(true);
        laugherGroup.fitWidthProperty().bind(stage.widthProperty().multiply(0.30));

        ImageView laugherDesc = new ImageView(new Image("LaugherTitleDesc.png"));
        laugherDesc.setPreserveRatio(true);
        laugherDesc.fitWidthProperty().bind(stage.widthProperty().multiply(0.20));

        scarerGroup.layoutXProperty().bind(stage.widthProperty().multiply(0.25).subtract(stage.widthProperty().multiply(0.30).divide(2)));
        scarerGroup.layoutYProperty().bind(stage.heightProperty().multiply(0.20));
        scarerDesc.layoutXProperty().bind(stage.widthProperty().multiply(0.25).subtract(stage.widthProperty().multiply(0.20).divide(2)));
        scarerDesc.layoutYProperty().bind(stage.heightProperty().multiply(0.55));

        laugherGroup.layoutXProperty().bind(stage.widthProperty().multiply(0.75).subtract(stage.widthProperty().multiply(0.30).divide(2)));
        laugherGroup.layoutYProperty().bind(stage.heightProperty().multiply(0.20));
        laugherDesc.layoutXProperty().bind(stage.widthProperty().multiply(0.75).subtract(stage.widthProperty().multiply(0.20).divide(2)));
        laugherDesc.layoutYProperty().bind(stage.heightProperty().multiply(0.55));

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

        StackPane root = new StackPane(background, contentLayer);
        Scene scene = new Scene(root);

        background.fitWidthProperty().bind(scene.widthProperty());
        background.fitHeightProperty().bind(scene.heightProperty());

        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
                stage.setScene(createTitleScene(stage));
        });

        return scene;
    }

    private Scene createCreditsScene(Stage stage) {
        Rectangle background = new Rectangle();
        background.setFill(Color.BLACK);
        background.widthProperty().bind(stage.widthProperty());
        background.heightProperty().bind(stage.heightProperty());

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

        StackPane root = new StackPane(background, content);
        Scene scene = new Scene(root);

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
            stage.setScene(createGameBoardScene(stage, chosenRole));
        } catch (Exception ex) {
            System.out.println("Failed to load game data! Check CSV files.");
            ex.printStackTrace();
        }
    }

    private Scene createGameBoardScene(Stage stage, Role playerRole) {
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);

        // State trackers for enforcing game logic rules
        final boolean[] mustDrawCard = {false};
        final boolean[] isRolling = {false};

        // ─── VISUAL TOKENS FOR THE GRID ───────────────────────────────────────
        DropShadow ds = new DropShadow();
        ds.setRadius(4.0); ds.setOffsetX(2.0); ds.setOffsetY(2.0);
        ds.setColor(Color.color(0, 0, 0, 0.6));

        Circle playerToken = new Circle();
        playerToken.radiusProperty().bind(stage.heightProperty().multiply(0.010)); 
        playerToken.setFill(Color.web("#3498db")); // Blue for player
        playerToken.setStroke(Color.WHITE);
        playerToken.setStrokeWidth(2);
        playerToken.setEffect(ds);

        Circle opponentToken = new Circle();
        opponentToken.radiusProperty().bind(stage.heightProperty().multiply(0.010));
        opponentToken.setFill(Color.web("#e74c3c")); // Red for opponent
        opponentToken.setStroke(Color.WHITE);
        opponentToken.setStrokeWidth(2);
        opponentToken.setEffect(ds);

        StackPane[] cellPanes = new StackPane[100];

        // ─── DYNAMIC LABELS FOR ENGINE INTEGRATION ────────────────────────────
        Label p1EnergyLabel = new Label("1000");
        Label p2EnergyLabel = new Label("1000");

        Label turnIndicator = new Label();
        turnIndicator.setStyle("-fx-text-fill: #ff6b35; -fx-font-size: 36px; -fx-font-weight: bold; -fx-font-family: 'Impact';");

        // ─── POWERUP BUTTONS ──────────────────────────────────────────────────
        Button p1PowerupBtn = createPowerupButton();
        Button p2PowerupBtn = createPowerupButton();

        // ─── PLAYER PANELS ────────────────────────────────────────────────────
        VBox player1Panel = createPlayerPanel(stage, "PLAYER 1 (You)", activeGame.getPlayer(), p1EnergyLabel, p1PowerupBtn);
        VBox player2Panel = createPlayerPanel(stage, "PLAYER 2 (Opponent)", activeGame.getOpponent(), p2EnergyLabel, p2PowerupBtn);

        // ─── DYNAMIC UPDATE METHOD ────────────────────────────────────────────
        Runnable updateUI = () -> {
            Monster p1 = activeGame.getPlayer();
            Monster p2 = activeGame.getOpponent();
            Monster curr = activeGame.getCurrent();

            p1EnergyLabel.setText(String.valueOf(p1.getEnergy()));
            p2EnergyLabel.setText(String.valueOf(p2.getEnergy()));

            Monster winner = activeGame.getWinner();
            if (winner != null) {
                turnIndicator.setText(winner.getName().toUpperCase() + " WINS!!!");
            } else if (mustDrawCard[0]) {
                turnIndicator.setText("DRAW YOUR CARD!");
            } else {
                turnIndicator.setText(curr == p1 ? "YOUR TURN!" : "OPPONENT'S TURN!");
            }

            // Move tokens on the board
            if (playerToken.getParent() != null) ((Pane) playerToken.getParent()).getChildren().remove(playerToken);
            if (opponentToken.getParent() != null) ((Pane) opponentToken.getParent()).getChildren().remove(opponentToken);

            int p1Pos = p1.getPosition();
            int p2Pos = p2.getPosition();

            if (p1Pos == p2Pos) {
                playerToken.setTranslateX(-8);
                opponentToken.setTranslateX(8);
            } else {
                playerToken.setTranslateX(0);
                opponentToken.setTranslateX(0);
            }

            if (cellPanes[p1Pos] != null) cellPanes[p1Pos].getChildren().add(playerToken);
            if (cellPanes[p2Pos] != null) cellPanes[p2Pos].getChildren().add(opponentToken);
        };

        // Actions for PowerUp Buttons (Validated with feedback messages)
        p1PowerupBtn.setOnMouseClicked(e -> {
            if (activeGame.getCurrent() != activeGame.getPlayer()) {
                turnIndicator.setText("NOT YOUR TURN!");
            } else if (activeGame.getPlayer().getEnergy() < Constants.POWERUP_COST) {
                turnIndicator.setText("NOT ENOUGH ENERGY (Need 500)");
            } else {
                try { activeGame.usePowerup(); updateUI.run(); } catch (Exception ex) {}
            }
        });

        p2PowerupBtn.setOnMouseClicked(e -> {
            if (activeGame.getCurrent() != activeGame.getOpponent()) {
                turnIndicator.setText("NOT YOUR TURN!");
            } else if (activeGame.getOpponent().getEnergy() < Constants.POWERUP_COST) {
                turnIndicator.setText("NOT ENOUGH ENERGY (Need 500)");
            } else {
                try { activeGame.usePowerup(); updateUI.run(); } catch (Exception ex) {}
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
                try {
                    activeGame.playTurn();
                    diceLabel.setText(activeGame.getLastRoll() == 0 ? "X" : String.valueOf(activeGame.getLastRoll())); 
                    
                    Monster justPlayed = activeGame.getCurrent() == activeGame.getPlayer() ? activeGame.getOpponent() : activeGame.getPlayer();
                    if (isCardCell(justPlayed.getPosition())) {
                        mustDrawCard[0] = true;
                    }
                    updateUI.run();
                } catch (Exception ex) {
                    System.out.println("Invalid Move Exception Caught!");
                }
                isRolling[0] = false;
            });
            timeline.getKeyFrames().add(finalFrame);
            timeline.play();
        });

        // ─── CARD DRAWING ─────────────────────────────────────────────────────
        ImageView cardBack = new ImageView(new Image("Cards/cardstack.png"));
        cardBack.setPreserveRatio(false);
        cardBack.fitWidthProperty().bind(stage.widthProperty().multiply(0.11));
        cardBack.fitHeightProperty().bind(stage.heightProperty().multiply(0.32));
        cardBack.setStyle("-fx-cursor: hand;");

        ImageView cardPicked = new ImageView();
        cardPicked.setPreserveRatio(false);
        cardPicked.fitWidthProperty().bind(stage.widthProperty().multiply(0.11));
        cardPicked.fitHeightProperty().bind(stage.heightProperty().multiply(0.32));
        cardPicked.setVisible(false);

        ImageView overlayCard = new ImageView();
        overlayCard.setPreserveRatio(false);
        overlayCard.fitWidthProperty().bind(stage.widthProperty().multiply(0.18));
        overlayCard.fitHeightProperty().bind(stage.heightProperty().multiply(0.50));
        overlayCard.setVisible(false);

        Rectangle overlayBg = new Rectangle();
        overlayBg.setFill(Color.BLACK);
        overlayBg.setOpacity(0.7);
        overlayBg.setVisible(false);
        overlayBg.widthProperty().bind(stage.widthProperty());
        overlayBg.heightProperty().bind(stage.heightProperty());

        overlayBg.setOnMouseClicked(e -> { overlayBg.setVisible(false); overlayCard.setVisible(false); cardPicked.setVisible(true); });
        overlayCard.setOnMouseClicked(e -> { overlayBg.setVisible(false); overlayCard.setVisible(false); cardPicked.setVisible(true); });

        cardBack.setOnMouseClicked(e -> {
            if (!mustDrawCard[0]) return; 
            mustDrawCard[0] = false; 
            
            try {
                // 1. Draw the card from the Engine
                Card drawnCard = Board.drawCard();
                
                // 2. Format the Name to match the file path (e.g., "Position Swap" -> "PositionSwap.png")
                String imageFileName = drawnCard.getName().replace(" ", "") + ".png";
                Image realCardImage = new Image("Cards/" + imageFileName);
                
                overlayCard.setImage(realCardImage);
                cardPicked.setImage(realCardImage);
                cardPicked.setVisible(false);

                // 3. Actually perform the Action on the Monsters
                Monster drawer = activeGame.getCurrent() == activeGame.getPlayer() ? activeGame.getOpponent() : activeGame.getPlayer();
                Monster waiting = activeGame.getCurrent();
                drawnCard.performAction(drawer, waiting);

                // 4. Animate it
                ScaleTransition shrink = new ScaleTransition(Duration.millis(150), overlayCard);
                shrink.setFromX(1); shrink.setToX(0);

                ScaleTransition grow = new ScaleTransition(Duration.millis(150), overlayCard);
                grow.setFromX(0); grow.setToX(1);

                shrink.setOnFinished(ev -> {
                    overlayCard.setVisible(true);
                    grow.play();
                    updateUI.run(); // Updates energy/positions after card effect
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
        cardsRow.spacingProperty().bind(stage.widthProperty().multiply(0.01));
        
        cardsRow.translateYProperty().bind(stage.heightProperty().multiply(-0.12)); 
        cardsRow.translateXProperty().bind(stage.widthProperty().multiply(0.14));  
        cardsRow.getChildren().addAll(cardBack, cardPicked);

        // ─── LEFT PANEL ASSEMBLY ───────────────────────────────────────────────
        HBox diceRow = new HBox(15, new Label("Roll:"), diceBox);
        diceRow.setAlignment(Pos.CENTER_LEFT);
        diceRow.setStyle("-fx-text-fill: white; -fx-font-size: 28px; -fx-font-family: 'Impact';");

        VBox leftPanel = new VBox(15, player1Panel, player2Panel, turnIndicator, diceRow, cardsRow);
        leftPanel.setAlignment(Pos.TOP_LEFT);
        leftPanel.paddingProperty().bind(stage.widthProperty().asObject().map(w -> 
            new Insets(w.doubleValue() * 0.015, w.doubleValue() * 0.015, w.doubleValue() * 0.04, w.doubleValue() * 0.015)
        ));
        leftPanel.prefWidthProperty().bind(stage.widthProperty().multiply(0.35));
        leftPanel.prefHeightProperty().bind(stage.heightProperty());

        // ─── GRID ─────────────────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.hgapProperty().bind(stage.widthProperty().multiply(0.003));
        grid.vgapProperty().bind(stage.heightProperty().multiply(0.005));
        grid.paddingProperty().bind(stage.widthProperty().asObject().map(w -> new Insets(w.doubleValue() * 0.008)));

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int cellNumber = getCellNumber(row, col);
                int engineIndex = cellNumber - 1; 

                StackPane cell = new StackPane();
                cell.setStyle("-fx-background-color: rgba(255, 255, 255, 0.7); -fx-border-color: #a0a0a0; -fx-border-width: 1px; -fx-background-radius: 6px; -fx-border-radius: 6px;");
                cell.prefWidthProperty().bind(stage.heightProperty().multiply(0.85).divide(10).subtract(4));
                cell.prefHeightProperty().bind(stage.heightProperty().multiply(0.85).divide(10).subtract(4));

                Label numberLabel = new Label(String.valueOf(cellNumber));
                numberLabel.styleProperty().bind(stage.heightProperty().multiply(0.013).asString("-fx-font-size: %.0fpx; -fx-text-fill: #333333; -fx-font-weight: bold;"));
                StackPane.setAlignment(numberLabel, Pos.TOP_LEFT);
                numberLabel.translateXProperty().bind(stage.widthProperty().multiply(0.003));
                numberLabel.translateYProperty().bind(stage.heightProperty().multiply(0.003));

                cell.getChildren().add(numberLabel);
                grid.add(cell, col, row);
                cellPanes[engineIndex] = cell; 
            }
        }

        StackPane gridContainer = new StackPane(grid);
        gridContainer.setAlignment(Pos.CENTER);
        gridContainer.prefWidthProperty().bind(stage.widthProperty().multiply(0.62));
        gridContainer.paddingProperty().bind(stage.widthProperty().asObject().map(w -> 
            new Insets(w.doubleValue() * 0.015, w.doubleValue() * 0.02, w.doubleValue() * 0.015, 0)
        ));
        StackPane.setAlignment(gridContainer, Pos.CENTER_RIGHT);

        // Run UI update once to position tokens initially
        updateUI.run();

        // ─── MAIN LAYOUT ──────────────────────────────────────────────────────
        HBox mainLayout = new HBox(leftPanel, gridContainer);
        mainLayout.setAlignment(Pos.CENTER);

        StackPane.setAlignment(overlayCard, Pos.CENTER);
        StackPane.setMargin(overlayCard, new Insets(150, 0, 0, 0));

        StackPane root = new StackPane(background, mainLayout, overlayBg, overlayCard);
        Scene scene = new Scene(root);

        background.fitWidthProperty().bind(scene.widthProperty());
        background.fitHeightProperty().bind(scene.heightProperty());

        scene.setOnKeyPressed(ev -> {
            if (ev.getCode() == KeyCode.ESCAPE) stage.setScene(createRoleSelectionScene(stage));
        });

        return scene;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private VBox createPlayerPanel(Stage stage, String titleName, Monster monster, Label energyLabel, Button powerupBtn) {
        // Monster Image
        String monsterImagePath = (monster.getRole() == Role.SCARER) ? "ScarerPickGroup.png" : "LaugherPickGroup.png";
        ImageView monsterImg = new ImageView(new Image(monsterImagePath));
        monsterImg.setPreserveRatio(true);
        monsterImg.fitWidthProperty().bind(stage.widthProperty().multiply(0.06));

        // Name Label (Title + Name)
        Label nameLabel = new Label(titleName + "\nName: " + monster.getName());
        nameLabel.styleProperty().bind(Bindings.concat("-fx-font-family: 'Impact'; -fx-text-fill: #ff6b35; -fx-font-size: ", stage.heightProperty().multiply(0.022).asString("%.0f"), "px;"));
        nameLabel.setWrapText(true);

        HBox nameRow = new HBox(10, monsterImg, nameLabel);
        nameRow.setAlignment(Pos.CENTER_LEFT);

        // Type Label explicitly stated and placed nicely in the brush box
        ImageView brushBg = new ImageView(new Image("titlescreenbuttonbackground.png"));
        brushBg.setPreserveRatio(false);
        brushBg.fitWidthProperty().bind(stage.widthProperty().multiply(0.15)); // Slightly wider to fit the text perfectly
        brushBg.fitHeightProperty().bind(stage.heightProperty().multiply(0.04));

        Label typeLabel = new Label("Type: " + monster.getClass().getSimpleName());
        typeLabel.styleProperty().bind(Bindings.concat("-fx-font-family: 'Impact'; -fx-text-fill: #FFD700; -fx-font-size: ", stage.heightProperty().multiply(0.018).asString("%.0f"), "px;"));
        StackPane typeBox = new StackPane(brushBg, typeLabel);
        
        VBox typeContainer = new VBox(5, typeBox);
        typeContainer.setAlignment(Pos.CENTER_LEFT);

        // Black Area with Canister, Energy, and PowerUp Button
        ImageView canisterImg = new ImageView(new Image("Scream_Canister.png"));
        canisterImg.setPreserveRatio(true);
        canisterImg.fitWidthProperty().bind(stage.widthProperty().multiply(0.020));

        energyLabel.styleProperty().bind(Bindings.concat("-fx-font-family: 'Impact'; -fx-text-fill: white; -fx-font-size: ", stage.heightProperty().multiply(0.025).asString("%.0f"), "px;"));

        HBox energyStatsRow = new HBox(15, canisterImg, energyLabel);
        energyStatsRow.setAlignment(Pos.CENTER_LEFT);
        
        VBox blackArea = new VBox(10, energyStatsRow, powerupBtn);
        blackArea.setStyle("-fx-background-color: rgba(0,0,0,0.4); -fx-background-radius: 15px; -fx-padding: 10;");

        // Main Panel Wrapper
        ImageView panelBg = new ImageView(new Image("titlescreenbuttonbackground.png"));
        panelBg.setPreserveRatio(false);
        panelBg.fitWidthProperty().bind(stage.widthProperty().multiply(0.32));
        panelBg.fitHeightProperty().bind(stage.heightProperty().multiply(0.25)); 
        panelBg.setOpacity(0.4);

        VBox content = new VBox(12, nameRow, typeContainer, blackArea);
        content.setAlignment(Pos.TOP_LEFT);
        content.paddingProperty().bind(stage.widthProperty().asObject().map(w -> new Insets(10, 10, 10, 10)));

        StackPane panelStack = new StackPane(panelBg, content);
        panelStack.setAlignment(Pos.TOP_LEFT);

        return new VBox(panelStack);
    }

    private Button createPowerupButton() {
        Button btn = new Button("Use PowerUp (500 Energy)");
        btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Impact'; -fx-cursor: hand; -fx-background-radius: 8px;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Impact'; -fx-cursor: hand; -fx-background-radius: 8px;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Impact'; -fx-cursor: hand; -fx-background-radius: 8px;"));
        btn.setMaxWidth(Double.MAX_VALUE); // Let it fill the black box width
        return btn;
    }

    private StackPane createImageButton(String brushstrokePath, String labelImagePath, Stage stage, double widthRatio, double heightRatio, double width_enhancer) {
        ImageView brush = new ImageView(new Image(brushstrokePath)); 
        brush.setPreserveRatio(false);
        brush.fitWidthProperty().bind(stage.widthProperty().multiply(widthRatio));
        brush.fitHeightProperty().bind(stage.heightProperty().multiply(heightRatio));

        ImageView label = new ImageView(new Image(labelImagePath));
        label.setPreserveRatio(true);
        label.fitWidthProperty().bind(stage.widthProperty().multiply(widthRatio * width_enhancer));

        StackPane btn = new StackPane(brush, label);
        btn.setStyle("-fx-cursor: hand;");
        btn.setPickOnBounds(true);
        btn.setOnMouseEntered(e -> { btn.setOpacity(0.8); btn.setScaleX(1.05); btn.setScaleY(1.05); });
        btn.setOnMouseExited(e -> { btn.setOpacity(1.0); btn.setScaleX(1.0); btn.setScaleY(1.0); });
        return btn;
    }
    
    private int getCellNumber(int row, int col) {
        int boardRow = 9 - row; 
        if (boardRow % 2 == 0) {
            return boardRow * 10 + col + 1;
        } else {
            return boardRow * 10 + (9 - col) + 1;
        }
    }
    
    private void playAudio(String filename) {
        if (mediaPlayer != null) { mediaPlayer.stop(); mediaPlayer.dispose(); }
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