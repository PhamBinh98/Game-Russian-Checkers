package sample;

import javafx.scene.layout.StackPane;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Ellipse;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static sample.Main.TILE_SIZE;
public class Piece extends StackPane {

    private PieceType type;

    private double mouseX, mouseY;
    private double oldX, oldY;

    private boolean queen = false;

    public PieceType getType() {
        return type;
    }

    public double getOldX() {
        return oldX;
    }

    public double getOldY() {
        return oldY;
    }

    public Piece(PieceType type, int x, int y) {
        this.type = type;

        move(x, y);

        Ellipse bg = new Ellipse(TILE_SIZE * 0.3125, TILE_SIZE * 0.26);
        bg.setFill(Color.BLUE);

        bg.setStroke(Color.BLUE);
        bg.setStrokeWidth(TILE_SIZE * 0.03);

        bg.setTranslateX((TILE_SIZE - TILE_SIZE * 0.3125 * 2) / 2);
        bg.setTranslateY((TILE_SIZE - TILE_SIZE * 0.26 * 2) / 2 + TILE_SIZE * 0.05);

        Ellipse ellipse = new Ellipse(TILE_SIZE * 0.3125, TILE_SIZE * 0.26);
        ellipse.setFill(type == PieceType.RED
                ? Color.RED : Color.WHITE);

        ellipse.setStroke(Color.BLACK);
        ellipse.setStrokeWidth(TILE_SIZE * 0.03);

        ellipse.setTranslateX((TILE_SIZE - TILE_SIZE * 0.3125 * 2) / 2);
        ellipse.setTranslateY((TILE_SIZE - TILE_SIZE * 0.26 * 2) / 2);

        getChildren().addAll(bg, ellipse);

        setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            relocate(e.getSceneX() - mouseX + oldX, e.getSceneY() - mouseY + oldY);
        });
    }

    public void move(int x, int y) {

        if (type == PieceType.WHITE && y == 0 || type == PieceType.RED && y == 7) {

            Cylinder bg = new Cylinder(TILE_SIZE * 0.3125, TILE_SIZE * 0.26);
            bg.setTranslateX((TILE_SIZE - TILE_SIZE * 0.3125 * 2) / 2);
            bg.setTranslateY((TILE_SIZE - TILE_SIZE * 0.26 * 2) / 2 + TILE_SIZE * 0.05);

            Rectangle ellipse = new Rectangle(TILE_SIZE * 0.3125, TILE_SIZE * 0.26);
            ellipse.setFill(Color.GOLD);

            ellipse.setStroke(Color.BLACK);
            ellipse.setStrokeWidth(TILE_SIZE * 0.03);

            ellipse.setTranslateX((TILE_SIZE - TILE_SIZE * 0.3125 * 2) / 2);
            ellipse.setTranslateY((TILE_SIZE - TILE_SIZE * 0.26 * 2) / 2);

            getChildren().addAll(bg, ellipse);

            oldX = x * TILE_SIZE;
            oldY = y * TILE_SIZE;
            relocate(oldX, oldY);
            queen = true;

        } else {
            oldX = x * TILE_SIZE;
            oldY = y * TILE_SIZE;
            relocate(oldX, oldY);
        }
    }

    public boolean queen() {
        return queen;
    }

    public void abortMove() {
        relocate(oldX, oldY);
    }
}