package game.engine;

import java.io.File;
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
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.control.ScrollPane;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import javafx.beans.binding.Bindings;

public class Game extends Application {
    private Board board;
    private ArrayList<Monster> allMonsters;
    private Monster player;
    private Monster opponent;
    private Monster current;
    private MediaPlayer mediaPlayer; // for audios

    public Game() {}

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

    public Board getBoard() { return board; }
    public ArrayList<Monster> getAllMonsters() { return allMonsters; }
    public Monster getPlayer() { return player; }
    public Monster getOpponent() { return opponent; }
    public Monster getCurrent() { return current; }
    public void setCurrent(Monster current) { this.current = current; }

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
        if (checkWinCondition(player)) return player;
        if (checkWinCondition(opponent)) return opponent;
        return null;
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

        // Layer 1: background
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);

        // Layer 2: gradient overlay
        ImageView layer2 = new ImageView(new Image("titlegradientRectangle.png"));
        layer2.setPreserveRatio(false);

        // Title logo
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

        // Buttons
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

        // Bind images to scene size
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
        // Background
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);

        // Blur effect on background
        GaussianBlur blur = new GaussianBlur(20);
        background.setEffect(blur);

        // Dark overlay rectangle
        Rectangle overlay = new Rectangle();
        overlay.setFill(Color.BLACK);
        overlay.setOpacity(0.65);
        overlay.widthProperty().bind(stage.widthProperty());
        overlay.heightProperty().bind(stage.heightProperty());

        // Title
        Label title = new Label("HOW TO PLAY");
        title.setStyle(
            "-fx-font-size: 50px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #ff6b35;" +
            "-fx-font-family: 'Impact';"
        );
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        // Instructions text
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
            "Press F to continue..."
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

        // Press F to proceed to role selection
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F)
                stage.setScene(createRoleSelectionScene(stage));
        });
        playAudio("pizzaParlor.mp3"); 
        return scene;
    }
    private Scene createRoleSelectionScene(Stage stage) {
        // Background
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);
        GaussianBlur blur = new GaussianBlur(20);
        background.setEffect(blur);

        // Scarer group image
        ImageView scarerGroup = new ImageView(new Image("ScarerPickGroup.png"));
        scarerGroup.setPreserveRatio(true);
        scarerGroup.fitWidthProperty().bind(stage.widthProperty().multiply(0.30));

        // Scarer description
        ImageView scarerDesc = new ImageView(new Image("ScarerTitleDesc.png"));
        scarerDesc.setPreserveRatio(true);
        scarerDesc.fitWidthProperty().bind(stage.widthProperty().multiply(0.20));

        // Laugher group image
        ImageView laugherGroup = new ImageView(new Image("LaugherPickGroup.png"));
        laugherGroup.setPreserveRatio(true);
        laugherGroup.fitWidthProperty().bind(stage.widthProperty().multiply(0.30));

        // Laugher description
        ImageView laugherDesc = new ImageView(new Image("LaugherTitleDesc.png"));
        laugherDesc.setPreserveRatio(true);
        laugherDesc.fitWidthProperty().bind(stage.widthProperty().multiply(0.20));

        // Position scarer group (Centered at 25% of screen width)
        scarerGroup.layoutXProperty().bind(
            stage.widthProperty().multiply(0.25)
            .subtract(stage.widthProperty().multiply(0.30).divide(2))
        );
        scarerGroup.layoutYProperty().bind(
            stage.heightProperty().multiply(0.20) // Moved down slightly for alignment
        );

        // Position scarer description (Centered at 25% of screen width)
        scarerDesc.layoutXProperty().bind(
            stage.widthProperty().multiply(0.25)
            .subtract(stage.widthProperty().multiply(0.20).divide(2))
        );
        scarerDesc.layoutYProperty().bind(
            stage.heightProperty().multiply(0.55) // Brought up to reduce the gap
        );

        // Position laugher group (Centered at 75% of screen width)
        laugherGroup.layoutXProperty().bind(
            stage.widthProperty().multiply(0.75)
            .subtract(stage.widthProperty().multiply(0.30).divide(2))
        );
        laugherGroup.layoutYProperty().bind(
            stage.heightProperty().multiply(0.20) // Mathematically level with Scarer
        );

        // Position laugher description (Centered at 75% of screen width)
        laugherDesc.layoutXProperty().bind(
            stage.widthProperty().multiply(0.75)
            .subtract(stage.widthProperty().multiply(0.20).divide(2))
        );
        laugherDesc.layoutYProperty().bind(
            stage.heightProperty().multiply(0.55) // Brought up to reduce the gap
        );

        // Click actions - Points to the helper method to link the engine
        scarerGroup.setOnMouseClicked(e -> stage.setScene(createGameBoardScene(stage, Role.SCARER)));
        scarerDesc.setOnMouseClicked(e -> stage.setScene(createGameBoardScene(stage, Role.SCARER)));
        laugherGroup.setOnMouseClicked(e -> stage.setScene(createGameBoardScene(stage, Role.LAUGHER)));
        laugherDesc.setOnMouseClicked(e -> stage.setScene(createGameBoardScene(stage, Role.LAUGHER)));

        // Hover effects
        scarerGroup.setOnMouseEntered(e -> scarerGroup.setOpacity(0.8));
        scarerGroup.setOnMouseExited(e -> scarerGroup.setOpacity(1.0));
        scarerDesc.setOnMouseEntered(e -> scarerDesc.setOpacity(0.8));
        scarerDesc.setOnMouseExited(e -> scarerDesc.setOpacity(1.0));
        laugherGroup.setOnMouseEntered(e -> laugherGroup.setOpacity(0.8));
        laugherGroup.setOnMouseExited(e -> laugherGroup.setOpacity(1.0));
        laugherDesc.setOnMouseEntered(e -> laugherDesc.setOpacity(0.8));
        laugherDesc.setOnMouseExited(e -> laugherDesc.setOpacity(1.0));

        // Cursor
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
        // Black background
        Rectangle background = new Rectangle();
        background.setFill(Color.BLACK);
        background.widthProperty().bind(stage.widthProperty());
        background.heightProperty().bind(stage.heightProperty());

        // Title
        Label title = new Label("GAME MADE BY TEAM 188");
        title.setStyle(
            "-fx-font-size: 48px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #ff6b35;" +
            "-fx-font-family: 'Impact';"
        );
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        // Team members
        String[] members = {
            "1)  Youssef Ashraf Saber",
            "2)  Amr Mohamed Mossad Kandeel",
            "3)  Omar Ahmed Osama Rady",
            "4)  Abdulrahman Emad Eldin Adel Soliman Yousry"
        };

        VBox memberList = new VBox(20);
        memberList.setAlignment(Pos.CENTER);
        memberList.setMaxWidth(Double.MAX_VALUE);

        for (String member : members) {
            Label memberLabel = new Label(member);
            memberLabel.setStyle(
                "-fx-font-size: 32px;" +
                "-fx-text-fill: white;" +
                "-fx-font-family: 'Georgia';"
            );
            memberLabel.setMaxWidth(Double.MAX_VALUE);
            memberLabel.setAlignment(Pos.CENTER);
            memberList.getChildren().add(memberLabel);
        }

        // Back hint
        Label hint = new Label("Press ESC to go back");
        hint.setStyle(
            "-fx-font-size: 18px;" +
            "-fx-text-fill: #aaaaaa;" +
            "-fx-font-family: 'Georgia';"
        );
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
    private Scene createGameBoardScene(Stage stage, Role playerRole) {
        // Background
        ImageView background = new ImageView(new Image("background(1st layer).png"));
        background.setPreserveRatio(false);

        // ─── DICE ─────────────────────────────────────────────────────────────
        Label diceLabel = new Label("?");
        diceLabel.styleProperty().bind(
            stage.heightProperty().multiply(0.05).asString("-fx-font-size: %.0fpx; -fx-font-weight: bold; -fx-text-fill: #333333;")
        );

        StackPane diceBox = new StackPane(diceLabel);
        diceBox.prefWidthProperty().bind(stage.widthProperty().multiply(0.04));
        diceBox.prefHeightProperty().bind(stage.widthProperty().multiply(0.02));
        diceBox.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 12px;" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-radius: 12px;" +
            "-fx-border-width: 2px;" +
            "-fx-cursor: hand;"
        );

        final boolean[] isRolling = {false};
        diceBox.setOnMouseClicked(e -> {
            if (isRolling[0]) return;
            isRolling[0] = true;

            Timeline timeline = new Timeline();
            for (int i = 0; i < 10; i++) {
                KeyFrame frame = new KeyFrame(Duration.millis(i * 80), ev -> {
                    int randomNum = (int)(Math.random() * 6) + 1;
                    diceLabel.setText(String.valueOf(randomNum));
                });
                timeline.getKeyFrames().add(frame);
            }
            KeyFrame finalFrame = new KeyFrame(Duration.millis(10 * 80), ev -> {
                int result = rollDice();
                diceLabel.setText(String.valueOf(result));
                isRolling[0] = false;
            });
            timeline.getKeyFrames().add(finalFrame);
            timeline.play();
        });

        // ─── CARD ─────────────────────────────────────────────────────────────
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

        String[] cardImages = {
            "Cards/2319Alert.png",
            "Cards/ContaminationCode.png",
            "Cards/SmallSnatcher.png",
            "Cards/PositionSwap.png",
            "Cards/MindScramble.png",
            "Cards/TotalConfusion.png",
            "Cards/MegaDrain.png",
            "Cards/SneakyThief.png",
            "Cards/SuperShield.png"
        };

        // Overlay card scales with stage
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

        overlayBg.setOnMouseClicked(e -> {
            overlayBg.setVisible(false);
            overlayCard.setVisible(false);
            cardPicked.setVisible(true);
        });
        overlayCard.setOnMouseClicked(e -> {
            overlayBg.setVisible(false);
            overlayCard.setVisible(false);
            cardPicked.setVisible(true);
        });

        cardBack.setOnMouseClicked(e -> {
            String randomCard = cardImages[(int)(Math.random() * cardImages.length)];
            overlayCard.setImage(new Image(randomCard));
            cardPicked.setImage(new Image(randomCard));
            cardPicked.setVisible(false);

            ScaleTransition shrink = new ScaleTransition(Duration.millis(150), overlayCard);
            shrink.setFromX(1);
            shrink.setToX(0);

            ScaleTransition grow = new ScaleTransition(Duration.millis(150), overlayCard);
            grow.setFromX(0);
            grow.setToX(1);

            shrink.setOnFinished(ev -> {
                overlayCard.setVisible(true);
                grow.play();
            });

            overlayBg.setVisible(true);
            overlayCard.setVisible(false);
            shrink.play();
        });

        HBox cardsRow = new HBox();
        cardsRow.setAlignment(Pos.BOTTOM_LEFT);
        cardsRow.spacingProperty().bind(stage.widthProperty().multiply(0.01));
        cardsRow.paddingProperty().bind(
            stage.widthProperty().asObject().map(w ->
                new Insets(0, 0, 0, w.doubleValue() * 0.2)
            )
        );
        cardsRow.getChildren().addAll(cardBack, cardPicked);

        // ─── LEFT PANEL ───────────────────────────────────────────────────────
        Label playerLabel = new Label("Player Role: " + playerRole);
        playerLabel.styleProperty().bind(
            stage.heightProperty().multiply(0.03).asString("-fx-text-fill: white; -fx-font-size: %.0fpx; -fx-font-weight: bold;")
        );

        HBox diceRow = new HBox(diceBox);
        diceRow.setAlignment(Pos.CENTER_LEFT);
        diceRow.paddingProperty().bind(
            stage.widthProperty().asObject().map(w ->
                new Insets(0, 0, 0, 610)
            )
        );
        Button powerupBtn = new Button("Use PowerUp");
        powerupBtn.styleProperty().bind(
            stage.heightProperty().multiply(0.02).asString("-fx-font-size: %.0fpx; -fx-cursor: hand;")
        );
        powerupBtn.setMinWidth(Region.USE_PREF_SIZE);
        powerupBtn.prefWidthProperty().bind(stage.widthProperty().multiply(0.1));
        powerupBtn.setOnMouseClicked(e -> System.out.println("Powerup used!"));
        VBox.setMargin(powerupBtn, new Insets(0, 0, 0, 560));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox leftPanel = new VBox();
        leftPanel.setAlignment(Pos.TOP_LEFT);
        leftPanel.spacingProperty().bind(stage.heightProperty().multiply(0.02));
        leftPanel.paddingProperty().bind(
            stage.widthProperty().asObject().map(w ->
                new Insets(w.doubleValue() * 0.015, w.doubleValue() * 0.015,
                           w.doubleValue() * 0.04, w.doubleValue() * 0.015)
            )
        );
        leftPanel.prefWidthProperty().bind(stage.widthProperty().multiply(0.35));
        leftPanel.prefHeightProperty().bind(stage.heightProperty());
        leftPanel.getChildren().addAll(playerLabel, diceRow, powerupBtn, spacer, cardsRow);

        // ─── GRID ─────────────────────────────────────────────────────────────
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.hgapProperty().bind(stage.widthProperty().multiply(0.003));
        grid.vgapProperty().bind(stage.heightProperty().multiply(0.005));
        grid.paddingProperty().bind(
            stage.widthProperty().asObject().map(w ->
                new Insets(w.doubleValue() * 0.008)
            )
        );

        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                int cellNumber = getCellNumber(row, col);

                StackPane cell = new StackPane();
                cell.setStyle(
                    "-fx-background-color: #d0d0d0;" +
                    "-fx-border-color: #a0a0a0;" +
                    "-fx-border-width: 1px;" +
                    "-fx-background-radius: 6px;" +
                    "-fx-border-radius: 6px;"
                );
                cell.prefWidthProperty().bind(stage.heightProperty().multiply(0.85).divide(10).subtract(4));
                cell.prefHeightProperty().bind(stage.heightProperty().multiply(0.85).divide(10).subtract(4));

                Label numberLabel = new Label(String.valueOf(cellNumber));
                numberLabel.styleProperty().bind(
                    stage.heightProperty().multiply(0.013).asString("-fx-font-size: %.0fpx; -fx-text-fill: #555555;")
                );
                StackPane.setAlignment(numberLabel, Pos.TOP_LEFT);
                numberLabel.translateXProperty().bind(stage.widthProperty().multiply(0.003));
                numberLabel.translateYProperty().bind(stage.heightProperty().multiply(0.003));

                cell.getChildren().add(numberLabel);
                grid.add(cell, col, row);
            }
        }

        StackPane gridContainer = new StackPane(grid);
        gridContainer.setAlignment(Pos.CENTER);
        gridContainer.prefWidthProperty().bind(stage.widthProperty().multiply(0.62));
        gridContainer.paddingProperty().bind(
            stage.widthProperty().asObject().map(w ->
                new Insets(w.doubleValue() * 0.015, w.doubleValue() * 0.02,
                           w.doubleValue() * 0.015, 0)
            )
        );
        StackPane.setAlignment(gridContainer, Pos.CENTER_RIGHT);

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
            if (ev.getCode() == KeyCode.ESCAPE)
                stage.setScene(createRoleSelectionScene(stage));
        });

        return scene;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private StackPane createImageButton(String brushstrokePath, String labelImagePath,
                                        Stage stage, double widthRatio,
                                        double heightRatio, double width_enhancer) {
        ImageView brush = new ImageView(new Image("titlescreenbuttonbackground.png"));
        brush.setPreserveRatio(false);
        brush.fitWidthProperty().bind(stage.widthProperty().multiply(widthRatio));
        brush.fitHeightProperty().bind(stage.heightProperty().multiply(heightRatio));

        ImageView label = new ImageView(new Image(labelImagePath));
        label.setPreserveRatio(true);
        label.fitWidthProperty().bind(stage.widthProperty().multiply(widthRatio * width_enhancer));

        StackPane btn = new StackPane(brush, label);
        btn.setStyle("-fx-cursor: hand;");
        btn.setPickOnBounds(true);
        btn.setOnMouseEntered(e -> {
            btn.setOpacity(0.8);
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
        });
        btn.setOnMouseExited(e -> {
            btn.setOpacity(1.0);
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });
        return btn;
    }
    private int getCellNumber(int row, int col) {
        // Row 0 (top) = cells 100-91, Row 9 (bottom) = cells 1-10
        int boardRow = 9 - row; // flip so row 0 = top of board = highest numbers

        if (boardRow % 2 == 0) {
            // Even rows go left to right: 1,2,3...
            return boardRow * 10 + col + 1;
        } else {
            // Odd rows go right to left: 10,9,8...
            return boardRow * 10 + (9 - col) + 1;
        }
    }
    private void playAudio(String filename) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }
        try {
            Media media = new Media(new File(filename).toURI().toString());
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        } catch (Exception e) {
            System.out.println("Audio not found: " + filename);
        }
    }
	    private VBox createPlayerPanel(Stage stage, String playerName, String monsterImagePath,
	            String role, String monsterType, Label powerupLabel) {
	
	// Load Irish Grover font
	javafx.scene.text.Font irishGrover = javafx.scene.text.Font.loadFont(
	"file:IrishGrover-Regular.ttf",
	stage.getHeight() * 0.04
	);
	
	// Monster image
	ImageView monsterImg = new ImageView(new Image(monsterImagePath));
	monsterImg.setPreserveRatio(true);
	monsterImg.fitWidthProperty().bind(stage.widthProperty().multiply(0.07));
	
	// Player name label with Irish Grover font
	Label nameLabel = new Label(playerName);
	nameLabel.styleProperty().bind(
	Bindings.concat(
	"-fx-font-family: 'Irish Grover'; -fx-text-fill: #ff6b35; -fx-font-size: ",
	stage.heightProperty().multiply(0.04).asString("%.0f"),
	"px;"
	)
	);
	
	// Monster image + name side by side
	HBox nameRow = new HBox(8, monsterImg, nameLabel);
	nameRow.setAlignment(Pos.CENTER_LEFT);
	
	// Role label under name using brushstroke background
	ImageView brushBg = new ImageView(new Image("titlescreenbuttonbackground.png"));
	brushBg.setPreserveRatio(false);
	brushBg.fitWidthProperty().bind(stage.widthProperty().multiply(0.15));
	brushBg.fitHeightProperty().bind(stage.heightProperty().multiply(0.05));
	
	Label roleLabel = new Label(role);
	roleLabel.styleProperty().bind(
	Bindings.concat(
	"-fx-font-family: 'Irish Grover'; -fx-text-fill: #FFD700; -fx-font-size: ",
	stage.heightProperty().multiply(0.025).asString("%.0f"),
	"px;"
	)
	);
	
	StackPane roleBox = new StackPane(brushBg, roleLabel);
	roleBox.setAlignment(Pos.CENTER);
	
	// Type label
	Label typeLabel = new Label("Type\n" + monsterType);
	typeLabel.styleProperty().bind(
	Bindings.concat(
	"-fx-font-family: 'Irish Grover'; -fx-text-fill: #FFD700; -fx-font-size: ",
	stage.heightProperty().multiply(0.022).asString("%.0f"),
	"px;"
	)
	);
	typeLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
	
	VBox roleAndType = new VBox(2, roleBox, typeLabel);
	roleAndType.setAlignment(Pos.CENTER_LEFT);
	
	// Canister image + energy
	ImageView canisterImg = new ImageView(new Image("Scream_Canister.png"));
	canisterImg.setPreserveRatio(true);
	canisterImg.fitWidthProperty().bind(stage.widthProperty().multiply(0.035));
	
	Label energyLabel = new Label("1000");
	energyLabel.styleProperty().bind(
	Bindings.concat(
	"-fx-font-family: 'Irish Grover'; -fx-text-fill: white; -fx-font-size: ",
	stage.heightProperty().multiply(0.03).asString("%.0f"),
	"px;"
	)
	);
	
	// Canister background box
	HBox energyRow = new HBox(6, canisterImg, energyLabel);
	energyRow.setAlignment(Pos.CENTER_LEFT);
	energyRow.setStyle(
	"-fx-background-color: rgba(0,0,0,0.4);" +
	"-fx-background-radius: 20px;" +
	"-fx-padding: 5 12 5 8;"
	);
	
	// Powerup used label (hidden by default)
	powerupLabel.styleProperty().bind(
	Bindings.concat(
	"-fx-font-family: 'Irish Grover'; -fx-text-fill: #ff6b35; -fx-font-size: ",
	stage.heightProperty().multiply(0.025).asString("%.0f"),
	"px;"
	)
	);
	powerupLabel.setVisible(false);
	
	// Panel background
	ImageView panelBg = new ImageView(new Image("titlescreenbuttonbackground.png"));
	panelBg.setPreserveRatio(false);
	panelBg.fitWidthProperty().bind(stage.widthProperty().multiply(0.30));
	panelBg.fitHeightProperty().bind(stage.heightProperty().multiply(0.25));
	panelBg.setOpacity(0.4);
	
	VBox content = new VBox(6, nameRow, roleAndType, energyRow, powerupLabel);
	content.setAlignment(Pos.TOP_LEFT);
	content.paddingProperty().bind(
	stage.widthProperty().asObject().map(w ->
	new Insets(8, 8, 8, 8)
	)
	);
	
	StackPane panelStack = new StackPane(panelBg, content);
	panelStack.setAlignment(Pos.TOP_LEFT);
	
	VBox wrapper = new VBox(panelStack);
	wrapper.setAlignment(Pos.TOP_LEFT);
	return wrapper;
	}
    public static void main(String[] args) {
        launch();
    }
}