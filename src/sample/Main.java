package sample;

import javafx.application.Application;

import javafx.scene.shape.Rectangle;

import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

  enum MoveType {
    NONE, NORMAL, KILL
}


public class Main extends Application {

    public static final int TILE_SIZE = 50;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;


    private Tile[][] board = new Tile[WIDTH][HEIGHT];

    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    private int RedScore = 12;
    private int WhiteScore = 12;

    private Parent createContent() {
        Pane root = new Pane();
        root.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        root.getChildren().addAll(tileGroup, pieceGroup);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Tile tile = new Tile((x + y) % 2 == 0, x, y);
                board[x][y] = tile;

                tileGroup.getChildren().add(tile);

                Piece piece = null;

                if (y <= 2 && (x + y) % 2 != 0) {
                    piece = makePiece(PieceType.RED, x, y);
                }

                if (y >= 5 && (x + y) % 2 != 0) {
                    piece = makePiece(PieceType.WHITE, x, y);
                }

                if (piece != null) {
                    tile.setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }

        return root;
    }

    private MoveResult tryMove(Piece piece, int newX, int newY) {
        if (board[newX][newY].hasPiece() || (newX + newY) % 2 == 0) {
            return new MoveResult(MoveType.NONE);
        }

        int x0 = toBoard(piece.getOldX());
        int y0 = toBoard(piece.getOldY());

        if (Math.abs(newX - x0) == 1 && piece.queen()) {
            return new MoveResult(MoveType.NORMAL);
        }

        if (Math.abs(newX - x0) == 1 && newY - y0 == piece.getType().moveDir) {
            return new MoveResult(MoveType.NORMAL);

        } else if (Math.abs(newX - x0) == 2) {

            int x1 = x0 + (newX - x0) / 2;
            int y1 = y0 + (newY - y0) / 2;

            if (board[x1][y1].hasPiece() && board[x1][y1].getPiece().getType() != piece.getType()) {
                return new MoveResult(MoveType.KILL, board[x1][y1].getPiece());
            }
        }

        return new MoveResult(MoveType.NONE);
    }

    private int toBoard(double pixel) {
        return (int)(pixel + TILE_SIZE / 2) / TILE_SIZE;
    }

    @Override
    public void start(Stage primaryStage) {
        Scene scene = new Scene(createContent());
        primaryStage.setTitle("Русские Шашки");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private Piece makePiece(PieceType type, int x, int y) {
        Piece piece = new Piece(type, x, y);

        piece.setOnMouseReleased(e -> {
            int newX = toBoard(piece.getLayoutX());
            int newY = toBoard(piece.getLayoutY());

            MoveResult result;

            if (newX < 0 || newY < 0 || newX >= WIDTH || newY >= HEIGHT) {
                result = new MoveResult(MoveType.NONE);
            } else {
                result = tryMove(piece, newX, newY);
            }

            int x0 = toBoard(piece.getOldX());
            int y0 = toBoard(piece.getOldY());

            switch (result.getType()) {
                case NONE:
                    piece.abortMove();
                    break;
                case NORMAL:
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);
                    break;
                case KILL:
                    piece.move(newX, newY);
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);

                    Piece otherPiece = result.getPiece();
                    board[toBoard(otherPiece.getOldX())][toBoard(otherPiece.getOldY())].setPiece(null);

                    pieceGroup.getChildren().remove(otherPiece);

                    if (otherPiece.getType() == PieceType.RED) {
                        RedScore--;
                    }
                    if (otherPiece.getType() == PieceType.WHITE) {
                        WhiteScore--;
                    }

                    AlertBox alert = new AlertBox();
                    if (WhiteScore == 0 )
                        alert.display("Game over", " Red win");

                    if (RedScore == 0)
                        alert.display("Game over", " White win");


                    break;
            }
        });

        return  piece;
}
    class MoveResult {
        private MoveType type;

        public MoveType getType() {
            return type;
        }

        private Piece piece;

        public Piece getPiece() {
            return piece;
        }

        public MoveResult(MoveType type) {
            this(type, null);
        }

        public MoveResult(MoveType type, Piece piece) {
            this.type = type;
            this.piece = piece;
        }
    }

    class Tile extends Rectangle {

        private Piece piece;
        public boolean hasPiece() {
            return piece != null;
        }
        public Piece  getPiece() {
            return piece;
        }
        public void setPiece(Piece piece) {
            this.piece = piece;
        }
        public Tile(boolean light, int x, int y) {
            setWidth(Main.TILE_SIZE);
            setHeight(Main.TILE_SIZE);

            relocate(x * Main.TILE_SIZE, y * Main.TILE_SIZE);

            setFill(light ? Color.AZURE : Color.BLACK);
        }

    }
    public static void main(String[] args) {
        launch(args);
    }

}
