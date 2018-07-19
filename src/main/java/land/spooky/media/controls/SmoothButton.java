package land.spooky.media.controls;

import javafx.animation.*;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * @author Daniel Gelber
 * @version 1.0
 * Created 2018-07-15
 * Last Modified 2018-07-16
 *
 * This button comes with the animations needed for this application.
 */
public class SmoothButton extends VBox implements Initializable {

    private Button button;

    public String buttonText = "default";

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
        button.setText(buttonText);
    }

    public Button getButton() {
        return button;
    }


    public SmoothButton() {

        button = new Button();
        this.getChildren().add(button);

        final Animation fadeIn = new Transition() {
            {
                setCycleDuration(Duration.millis(50));
                setInterpolator(Interpolator.EASE_OUT);
            }
            @Override
            protected void interpolate(double frac) {
                Color vColor = new Color(0.0274, .5607, 0.7176, (0.01 + frac)/1.01);
                setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        };

        final Animation fadeOut = new Transition() {
            {
                setCycleDuration(Duration.millis(300));
                setInterpolator(Interpolator.EASE_OUT);
            }
            @Override
            protected void interpolate(double frac) {
                Color vColor = new Color(0.0274, .5607, 0.7176, (1.01 - frac)/1.01);
                setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        };

        Timeline clickTimeline = new Timeline();
        clickTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(opacityProperty(), 1)),
                new KeyFrame(new Duration(50),
                        new KeyValue(opacityProperty(), 0.7)),
                new KeyFrame(new Duration(300),
                        new KeyValue(opacityProperty(), 1))
        );

        this.setOnMouseEntered(e -> {
            fadeOut.stop();
            fadeIn.play();
        });
        this.setOnMouseExited(e -> {
            fadeIn.stop();
            fadeOut.play();
        });
        this.button.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            clickTimeline.stop();
            clickTimeline.play();
        });

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
