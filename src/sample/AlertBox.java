package sample;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class AlertBox {

    public void display(String tile, String message) {

        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(tile);
        window.setMinWidth(400);
        window.setMinHeight(300);
        Label label = new Label();
        label.setText(message);
        Button button = new Button("Close");
        button.setOnAction(e -> System.exit(0));
        VBox layout = new VBox(10);

        layout.getChildren().addAll(label,button);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

    }
}
