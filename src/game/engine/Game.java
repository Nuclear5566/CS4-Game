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
        creditsBtn.setOnMouseClicked(e -> System.out.println("Credits clicked!"));

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

        // Position scarer group (X=0.18, Y=0.15)
        scarerGroup.layoutXProperty().bind(
            stage.widthProperty().multiply(0.18)
            .subtract(stage.widthProperty().multiply(0.30).divide(2))
        );
        scarerGroup.layoutYProperty().bind(
            stage.heightProperty().multiply(0.15)
        );

        // Position scarer description (X=0.18, Y=0.65)
        scarerDesc.layoutXProperty().bind(
            stage.widthProperty().multiply(0.18)
            .subtract(stage.widthProperty().multiply(0.20).divide(2))
        );
        scarerDesc.layoutYProperty().bind(
            stage.heightProperty().multiply(0.65)
        );

        // Position laugher group (X=0.72, Y=0.15)
        laugherGroup.layoutXProperty().bind(
            stage.widthProperty().multiply(0.72)
            .subtract(stage.widthProperty().multiply(0.30).divide(2))
        );
        laugherGroup.layoutYProperty().bind(
            stage.heightProperty().multiply(0.15)
        );

        // Position laugher description (X=0.72, Y=0.65)
        laugherDesc.layoutXProperty().bind(
            stage.widthProperty().multiply(0.72)
            .subtract(stage.widthProperty().multiply(0.20).divide(2))
        );
        laugherDesc.layoutYProperty().bind(
            stage.heightProperty().multiply(0.65)
        );

        // Click actions
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

    private Scene createGameBoardScene(Stage stage, Role playerRole) {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        Label placeholder = new Label("Game Board Coming Soon...\nRole: " + playerRole);
        placeholder.setStyle("-fx-text-fill: white; -fx-font-size: 32px;");
        root.getChildren().add(placeholder);

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE)
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
    public static void main(String[] args) {
        launch();
    }
}