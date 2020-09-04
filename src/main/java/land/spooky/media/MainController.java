package land.spooky.media;

import javafx.animation.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.transform.Translate;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import javafx.util.Duration;
import land.spooky.media.controls.*;
import land.spooky.media.models.MainModel;
import land.spooky.media.models.MovieModel;

import java.io.*;
import java.util.Optional;

/**
 * @author Daniel Gelber
 * @version 1.0
 * Created 2018-07-14
 * Last Modified 2018-07-19
 *
 * The main controller. Interfaces the GUI and the logic.
 */
public class MainController {

    // INSTANCE VARIABLES

    @FXML private GridPane movieGrid;
    @FXML private ImageView activePoster;
    @FXML private Label activeTitle;
    @FXML private Label activeInfo;
    @FXML private Label activeDesc;
    @FXML private Label pageIndicator;
    @FXML private ProgressBar progressBar;
    @FXML private SmoothButton playBtn;
    @FXML private SmoothButton downloadBtn;
    @FXML private SmoothButton deleteBtn;
    @FXML private SmoothButton prevBtn;
    @FXML private SmoothButton nextBtn;
    @FXML private SmoothButton clearBtn;
    @FXML private TextField searchBar;
    @FXML private VBox infoBox;
    @FXML private BorderPane contentArea;
    private MainModel model;


    // METHODS

    /**
     * Plays the movie.
     */
    private void playMovie() {
        model.playMovie();
    }

    /**
     * Downloads the movie to a selected path.
     */
    private void downloadMovie() {

        // If it's already downloading something, notify and exit
        if (model.isDownloading()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Alert");
            alert.setHeaderText(null);
            alert.setContentText("Please wait until your current download is finished.");
            alert.showAndWait();
            return;
        }

        // Pick directory
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Where do you want to put it?");
        File dir = dirChooser.showDialog(movieGrid.getScene().getWindow());
        if (dir == null)
            return;

        // Download
        model.downloadMovie(dir.toString());

    }

    /**
     * Asks if you're sure, then deletes the movie.
     */
    private void deleteMovie() {

        // Are you sure?
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete this? If you want to save a copy elsewhere, you can" +
                " download it via the download button.");

        // If cancel, then return
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() != ButtonType.OK)
            return;

        // Delete and update display
        model.deleteMovie();
        updateMoviesDisplay("x");
        updateInfoPaneDisplay();

    }

    /**
     * When the user types something in the search bar, if it's enter,
     * then update the filter, clear the search bar, and refresh display.
     * @param event
     */
    private void searchBarUpdate(KeyEvent event) {

        if (event.getCode() == KeyCode.ENTER) {
            model.setFilter(searchBar.getText());
            searchBar.setText("");
            updateMoviesDisplay("l");
            nextBtn.requestFocus();
        }

    }

    /**
     * Empty the search filter and update the display.
     */
    private void clearSearch() {

        model.setFilter("");
        searchBar.setText("");
        updateMoviesDisplay("r");

    }

    /**
     * If the user has a download in progress, make sure they want to close.
     * Otherwise, close the app.
     */
    @FXML private void closeApp() {

        // If downloading
        if (model.isDownloading()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText(null);
            alert.setContentText("A download is in progress. Are you sure you want to close?");

            // If they hit ok, close. Otherwise cancel the close.
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() != ButtonType.OK)
                System.exit(0);
            else
                return;
        }

        // Otherwise just close
        System.exit(0);

    }

    /**
     * Move down a page and if the page has changed, update the display
     * with a left animation.
     */
    @FXML private void prevPage() {

        int currentPage = model.getPage();
        model.pageDown();
        if (model.getPage() != currentPage)
            updateMoviesDisplay("l");

    }

    /**
     * Move up a page and if the page has changed, update the display
     * with a right animation.
     */
    @FXML private void nextPage() {

        int currentPage = model.getPage();
        model.pageUp();
        if (model.getPage() != currentPage)
            updateMoviesDisplay("r");

    }

    /**
     * Add a movie to the model given a certain output path and IMDb link.
     * Update the display when done.
     * @param movie the file which is the movie
     */
    @FXML private void newMovie(File movie) {

        // Get IMDb link
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("IMDb Link");
        dialog.setContentText("Please enter the link to the movie on IMDb:");
        Optional<String> link = dialog.showAndWait();
        if (!link.isPresent())
            return;

        // Pull data and add to disk
        model.addMovie(movie.toString(), link.get());
        updateMoviesDisplay("x");

    }

    /**
     * Add a movie to the model given a certain file and an IMDb link.
     * Update the display when done.
     * @param event unused.
     */
    @FXML private void newMovie(ActionEvent event) {

        // Get File of the new movie being added
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose the Movie");
        File movie = fileChooser.showOpenDialog(movieGrid.getScene().getWindow());
        if (movie == null)
            return;

        newMovie(movie);

    }

    /**
     * Changes the target directory from where the app gets the list of
     * movies to display. Updates the display after the change.
     * @param event unused.
     */
    @FXML private void setTargetDirectory(ActionEvent event) {

        // Get new target directory
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose the Target Directory");
        File dir = dirChooser.showDialog(movieGrid.getScene().getWindow());
        if (dir == null)
            return;

        // Change the model's target directory
        model.setTargetDirectory(dir.getPath());
        updateInfoPaneDisplay();
        updateMoviesDisplay("l");

    }

    /**
     * Initialize the model, the button events, the display, the progress
     * bar's binding, and the navigation events.
     */
    @FXML public void initialize() {

        // Initialize the model
        model = new MainModel();

        // Initialize buttons events
        playBtn.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> playMovie());
        downloadBtn.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> downloadMovie());
        deleteBtn.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> deleteMovie());
        clearBtn.getButton().addEventHandler(MouseEvent.MOUSE_CLICKED, e -> clearSearch());
        searchBar.setOnKeyPressed(e -> searchBarUpdate(e));

        // Fill the randomized default selected movie
        updateInfoPaneDisplay();

        // Fill the grid with selectable movies
        updateMoviesDisplay("x");

        // Bind progress bar to the model
        progressBar.progressProperty().bind(model.downloadProgressProperty());
        model.downloadingProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Congratulations");
                alert.setHeaderText(null);
                alert.setContentText("Your download is complete.");
                alert.showAndWait();
            }
        });

        // Bind navigation buttons to this Controller
        prevBtn.getButton().setOnMousePressed(e -> prevPage());
        nextBtn.getButton().setOnMouseClicked(e -> nextPage());
        movieGrid.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.LEFT)
                prevPage();
            else if (e.getCode() == KeyCode.RIGHT)
                nextPage();
        });

        // DRAG AND DROP
        // Dragging over the movie grid
        contentArea.setOnDragOver(event -> {
            if (event.getGestureSource() != movieGrid
                    && event.getDragboard().hasFiles()
                    && event.getDragboard().getFiles().size() == 1) {
                event.acceptTransferModes(TransferMode.COPY);
            }
            event.consume();
        });

        contentArea.setOnDragEntered(event -> {
            if (event.getGestureSource() != movieGrid
                    && event.getDragboard().hasFiles()
                    && event.getDragboard().getFiles().size() == 1) {
                Color vColor = new Color(0.0274, .5607, 0.7176, 1);
                Border border = new Border(
                        new BorderStroke(vColor, BorderStrokeStyle.DASHED, new CornerRadii(50), new BorderWidths(10), new Insets(20)));
                contentArea.setBorder(border);
            }
            event.consume();
        });

        contentArea.setOnDragExited(event -> {
            contentArea.setBorder(Border.EMPTY);
            event.consume();
        });

        movieGrid.setOnDragDropped(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = db.hasFiles() && db.getFiles().size() == 1;
                event.setDropCompleted(success);
                event.consume();

                if (success) {
                    newMovie(db.getFiles().get(0));
                }
            }
        });

    }

    /**
     * Reloads the info bar with the updated information from model. Also
     * contains the animations for the switch.
     */
    private void updateInfoPaneDisplay() {

        // Fade out
        final Animation fadeOut = new Transition() {
            {
                setCycleDuration(Duration.millis(50));
                setInterpolator(Interpolator.EASE_OUT);
            }
            @Override
            protected void interpolate(double frac) {
                infoBox.setOpacity(1 - frac);
            }
        };

        // Fade in
        final Animation fadeIn = new Transition() {
            {
                setCycleDuration(Duration.millis(200));
                setInterpolator(Interpolator.EASE_IN);
            }
            @Override
            protected void interpolate(double frac) {
                infoBox.setOpacity(frac);
            }
        };

        // Change info
        fadeOut.setOnFinished(e -> {
            MovieModel activeMovie = model.getActiveMovie();
            if (activeMovie != null) {
                activePoster.setImage(new Image("file:" + activeMovie.getPoster()));
                activeTitle.setText(activeMovie.getTitle());
                activeInfo.setText(activeMovie.getInfo());
                activeDesc.setText(activeMovie.getDescription());
                fadeIn.play();
            }
        });

        // Begin process
        fadeOut.play();

    }

    /**
     * Reloads the movies display area with the updated information from model. Also
     * contains the animations for the switch.
     */
    private void updateMoviesDisplay(String direction) {

        // Fade out
        final Animation fadeOut = new Transition() {
            {
                setCycleDuration(Duration.millis(150));
                setInterpolator(Interpolator.LINEAR);
            }
            @Override
            protected void interpolate(double frac) {
                movieGrid.setOpacity(1 - frac);
                movieGrid.getTransforms().clear();
                if (direction.equals("r"))
                    movieGrid.getTransforms().add(new Translate(frac * -100, 0));
                else if (direction.equals("l"))
                    movieGrid.getTransforms().add(new Translate(frac * 100, 0));
            }
        };

        // Fade in
        final Animation fadeIn = new Transition() {
            {
                setCycleDuration(Duration.millis(150));
                setInterpolator(Interpolator.LINEAR);
            }
            @Override
            protected void interpolate(double frac) {
                movieGrid.setOpacity(frac);
                movieGrid.getTransforms().clear();
                if (direction.equals("r"))
                    movieGrid.getTransforms().add(new Translate((1 - frac) * 100, 0));
                else if (direction.equals("l"))
                    movieGrid.getTransforms().add(new Translate((1 - frac) * -100, 0));
            }
        };

        // Change info
        fadeOut.setOnFinished(e -> {

            // Reload movies list
            model.loadMovieList();
            movieGrid.getChildren().clear();

            // Movies list
            for (int i = 0; i < model.getMovieList().size(); i++) {
                Icon newIcon = new Icon(new ImageView(new Image("file:" + model.getMovieList().get(i).getPoster())),
                        model.getMovieList().get(i).getTitle(), model.getMovieList().get(i).getYear());
                newIcon.addEventHandler(MouseEvent.MOUSE_PRESSED, ev -> {
                    model.setActiveMovie(newIcon.getTitle(), newIcon.getYear());
                    updateInfoPaneDisplay();
                    nextBtn.requestFocus();
                });
                newIcon.setOnMouseReleased(ev -> newIcon.setOpacity(1));
                movieGrid.add(newIcon, i % 8, i / 8);
            }

            // Page indicator
            pageIndicator.setText(Integer.toString(model.getPage()) + "/" + model.getNumPages());

            fadeIn.play();

        });

        // Begin process
        fadeOut.play();

    }

}
