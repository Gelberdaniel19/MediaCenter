package land.spooky.media.controls;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * @author Daniel Gelber
 * @version 1.0
 * Created 2018-07-14
 * Last Modified 2018-07-15
 *
 * A graphical model of the Icon. A list of these are displayed as options
 * in the movie GridPane.
 */
public class Icon extends VBox {

    private static final String DEFAULT_STYLE_CLASS = "movie-icon";

    private ImageView poster;
    private Label displayedTitle;
    private String title;
    private String year;


    public Icon(ImageView poster, String title, String year) {
        this.poster = poster;
        this.title = title;
        this.year = year;

        if (this.title.length() > 40)
            this.displayedTitle = new Label(this.title.substring(0, 40) + "...");
        else
            this.displayedTitle = new Label(this.title);

        getStyleClass().setAll(DEFAULT_STYLE_CLASS);

        this.poster.setFitHeight(134);
        this.poster.setFitWidth(91);
        this.displayedTitle.setWrapText(true);

        final Animation fadeIn = new Transition() {
            {
                setCycleDuration(Duration.millis(50));
                setInterpolator(Interpolator.EASE_OUT);
            }
            @Override
            protected void interpolate(double frac) {
                Color vColor = new Color(0.0274, .5607, 0.7176, frac);
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
                Color vColor = new Color(0.0274, .5607, 0.7176, 1 - frac);
                setBackground(new Background(new BackgroundFill(vColor, CornerRadii.EMPTY, Insets.EMPTY)));
            }
        };

        Timeline clickTimeline = new Timeline();
        clickTimeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(opacityProperty(), 1)),
                new KeyFrame(new Duration(50),
                        new KeyValue(opacityProperty(), 0.7)),
                new KeyFrame(new Duration(500),
                        new KeyValue(opacityProperty(), 1)));

        this.setOnMouseEntered(e -> {
            fadeOut.stop();
            fadeIn.play();
        });
        this.setOnMouseExited(e -> {
            fadeIn.stop();
            fadeOut.play();
        });
        this.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> clickTimeline.play());


        this.getChildren().addAll(this.poster, this.displayedTitle);
    }

    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

}
