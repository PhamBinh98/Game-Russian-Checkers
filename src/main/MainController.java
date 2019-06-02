package main;

import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.lang.Math.abs;

public class MainController implements Cloneable {
    static final int WIDTH = 8;
    static final int BOARD_EDGE_FOR_WHITE = 0;
    static final int BOARD_EDGE_FOR_BLACK = 7;
    private static final int END_WHITE_SIDE = 5;
    private static final int END_BLACK_SIDE = 2;
    private final double SQUARE_SIZE = 80.0;

    private GameBoard game;

    public ImageView turnImage;
    public Text txtScore;
    public Pane gamePanel;

    private int whiteScore;
    private int blackScore;

    private int count = 0;
    private boolean wait = false;

    private Player firstPlayer = Player.HUMAN;
    private Cells[][] firstBoard = new Cells[WIDTH][WIDTH]; //[X][Y]

    private Group groupHighlight = new Group();
    private List<Pair<ImageView, Pair<Integer, Integer>>> relations;
    private Image contour = new Image("main/fxml/Contour.png");
    private Image hatchCell = new Image("main/fxml/Hatching.png");
    private Image black = new Image("main/fxml/Black80.png");
    private Image white = new Image("main/fxml/White80.png");

    private void setFirstBoard(Cells[][] b, List<Pair<ImageView, Pair<Integer, Integer>>> list) {
        for (int x=0; x < WIDTH; x++) {
            for (int y=0; y <= END_BLACK_SIDE; y++) {
                if ((x+y)%2==1) {
                    b[x][y] = Cells.BLACK;
                    ImageView view = new ImageView(black);
                    view.relocate(x*SQUARE_SIZE, y*SQUARE_SIZE);
                    gamePanel.getChildren().add(view);
                    list.add(new Pair<>(view, new Pair<>(x, y)));
                }
                else b[x][y] = Cells.EMPTY;
            }
            for (int y = END_BLACK_SIDE + 1; y < END_WHITE_SIDE; y++) {
                b[x][y] = Cells.EMPTY;
            }
            for (int y = END_WHITE_SIDE; y < WIDTH; y++) {
                if ((x+y)%2==1) {
                    b[x][y] = Cells.WHITE;
                    ImageView view = new ImageView(white);
                    view.relocate(x*SQUARE_SIZE, y*SQUARE_SIZE);
                    gamePanel.getChildren().add(view);
                    list.add(new Pair<>(view, new Pair<>(x, y)));
                }
                else b[x][y] = Cells.EMPTY;
            }
        }
        relations = list;
        firstBoard = b;
    }

    private void winWhite() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Конец игры");
        alert.setHeaderText("Исход игры: победа БЕЛЫХ!\nИграть снова?");
        ButtonType play = new ButtonType("Играть");
        ButtonType exit = new ButtonType("Выход");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(play, exit);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == play) btnRestart();
        else System.exit(100000);
    }

    private void winBlack() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Конец игры");
        alert.setHeaderText("Исход игры: победа ЧЕРНЫХ!\nИграть снова?");
        ButtonType play = new ButtonType("Играть");
        ButtonType exit = new ButtonType("Выход");
        alert.getButtonTypes().clear();
        alert.getButtonTypes().addAll(play, exit);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == play) btnRestart();
        else System.exit(100000);
    }



    public void btnRestart() {
        gamePanel.getChildren().remove(groupHighlight);
        groupHighlight.getChildren().clear();
        for (Pair<ImageView, Pair<Integer,Integer>> pair: relations) {
            gamePanel.getChildren().remove(pair.getKey());
        }
        if (count == 0) setFirstBoard(new Cells[WIDTH][WIDTH], new ArrayList<>());
        turnImage.setImage(new Image("main/fxml/White20.png"));
        count = 0;
        whiteScore = 0;
        blackScore = 0;
        setTxtScore(whiteScore, blackScore);
    }

    public void onMouseClicked(MouseEvent mouseEvent) {
        count++;
        if (count == 1) {
            setFirstBoard(new Cells[WIDTH][WIDTH], new ArrayList<>());
            game = new GameBoard(firstBoard, firstPlayer);
        }

        if (game.getTurn() == Player.COMPUTER && !wait) {
            wait = true;
            Cells[][] before = game.getBoard();
            game.moveComputer();
            int from = game.getFromID();
            int to = game.getToID();
            boolean isAttack = game.isAttack();
            whiteScore = game.getWhiteScore();
            blackScore = game.getBlackScore();
            relocate(from/10, from%10, to/10, to%10, before, Player.COMPUTER, isAttack);
            wait = false;
        }
        else if (game.getTurn() == Player.HUMAN && !wait) {
            wait = true;
            gamePanel.getChildren().remove(groupHighlight);
            groupHighlight.getChildren().clear();
            int x = (int) (mouseEvent.getX()/SQUARE_SIZE);
            int y = (int) (mouseEvent.getY()/SQUARE_SIZE);

            Cells[][] before = game.getBoard();
            if (before[x][y] == Cells.WHITE) {            //Создать PLAYER-ов
                ImageView view1 = new ImageView(contour);
                view1.relocate(x * SQUARE_SIZE, y * SQUARE_SIZE);
                groupHighlight.getChildren().add(view1);
            }
            Cells[][] after = game.moveHuman(x, y);
            for (int i=0; i < WIDTH; i++) {
                for (int j=0; j < WIDTH; j++) {
                    if (after[i][j] == Cells.PLACE_MOVE) {
                        ImageView view2 = new ImageView(hatchCell);
                        view2.relocate(i * SQUARE_SIZE, j * SQUARE_SIZE);
                        groupHighlight.getChildren().add(view2);
                    }
                }
            }

            if (game.getReady()) {
                int from = game.getFromID();
                int to = game.getToID();
                boolean isAttack = game.isAttack();
                whiteScore = game.getWhiteScore();
                blackScore = game.getBlackScore();
                relocate(from/10, from%10, to/10, to%10, before, Player.HUMAN, isAttack);
                gamePanel.getChildren().remove(groupHighlight);
                groupHighlight.getChildren().clear();
            }

            gamePanel.getChildren().add(groupHighlight);
            wait = false;
        }

        if (blackScore == 12) {
            winWhite();
        }
        else if (whiteScore == 12) {
            winBlack();
        }

        setTxtScore(whiteScore, blackScore);
    }

    private void relocate(int fromPlaceX, int fromPlaceY, int toPlaceX, int toPlaceY, Cells[][] board, Player curPlayer,
                          boolean isAttack) {
        for (Pair<ImageView, Pair<Integer, Integer>> element: relations) {
            if (fromPlaceX == element.getValue().getKey() &&
                    fromPlaceY == element.getValue().getValue()) {
                gamePanel.getChildren().remove(element.getKey());
                if ((toPlaceY == BOARD_EDGE_FOR_WHITE && board[fromPlaceX][fromPlaceY] == Cells.WHITE) ||
                        (toPlaceY == BOARD_EDGE_FOR_BLACK && board[fromPlaceX][fromPlaceY] == Cells.BLACK)) {
                    relations.remove(element);
                    Image imageQueen;
                    if (toPlaceY == BOARD_EDGE_FOR_WHITE) imageQueen = new Image("main/fxml/WhiteQueen80.png");
                    else imageQueen = new Image("main/fxml/BlackQueen80.png");
                    ImageView viewQueen = new ImageView(imageQueen);
                    viewQueen.relocate(toPlaceX * SQUARE_SIZE, toPlaceY * SQUARE_SIZE);
                    gamePanel.getChildren().add(viewQueen);
                    relations.add(new Pair<>(viewQueen, new Pair<>(toPlaceX, toPlaceY)));
                } else {
                    element.getKey().relocate(toPlaceX * SQUARE_SIZE, toPlaceY * SQUARE_SIZE);
                    gamePanel.getChildren().add(element.getKey());
                    relations.remove(element);
                    relations.add(new Pair<>(element.getKey(), new Pair<>(toPlaceX, toPlaceY)));
                }
                break;
            }
        }
        if (isAttack) {
            int directionX = (toPlaceX - fromPlaceX) / abs(toPlaceX - fromPlaceX);
            int directionY = (toPlaceY - fromPlaceY) / abs(toPlaceY - fromPlaceY);
            for (; ; ) {
                if (GameBoard.getOpponent(board[fromPlaceX + directionX][fromPlaceY + directionY]) == curPlayer) {
                    for (Pair<ImageView, Pair<Integer, Integer>> element : relations) {
                        if (fromPlaceX + directionX == element.getValue().getKey() &&
                                fromPlaceY + directionY == element.getValue().getValue()) {
                            gamePanel.getChildren().remove(element.getKey());
                            relations.remove(element);
                            break;
                        }
                    }
                    break;
                }
                directionX += directionX / abs(directionX);
                directionY += directionY / abs(directionY);
            }
        }
    }

    private void setTxtScore(int scoreWhite, int scoreBlack) {
        txtScore.setText(scoreWhite + "-" + scoreBlack);
    }

}
