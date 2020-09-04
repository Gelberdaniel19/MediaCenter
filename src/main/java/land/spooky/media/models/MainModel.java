package land.spooky.media.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.concurrent.Task;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Daniel Gelber
 * @version 1.0
 * Created 2018-07-14
 * Last Modified 2018-07-18
 *
 * This model has the properties and logic needed for the main display.
 */
public final class MainModel {

    // INSTANCE VARIABLES

    /**
     * The current page that the user is viewing.
     */
    private int page = 1;

    /**
     * The amount of movies shown per page.
     */
    private int moviesPerPage = 24;

    /**
     * The search filter. When a user searches for a movie, the loadMovieList() method
     * uses this to find relevant search results. If left empty, all movies are shown.
     */
    private String filter = "";

    /**
     * A list of every movie that is on the current page.
     */
    private ArrayList<MovieModel> movieList = new ArrayList<>();

    /**
     * The movie the user is currently being shown on the info bar.
     */
    private MovieModel activeMovie;

    /**
     * The location on the user's disk where the information is stored.
     */
    private String targetDirectory;

    /**
     * A property that shows if the Model is currently downloading a movie to their
     * disk. Only one download is allowed at a time.
     */
    private BooleanProperty isDownloading = new SimpleBooleanProperty(false);

    /**
     * A property that shows the progress of the download, useful for a progress bar.
     * Defaults to zero on initialization and when nothing is being downloaded.
     */
    private DoubleProperty downloadProgress = new SimpleDoubleProperty(0);


    // CONSTRUCTOR AND METHODS

    /**
     * All of the logic for the app is controlled from this class. There may be a future
     * version where multiple instances are useful, such as having different users. For
     * now, this class can be treated static.
     */
    public MainModel() {

        loadTargetDirectory();
        loadMovieList();
        pickActiveMovie();

    }

    /**
     * Called on construction and when the target directory is changed. It reads the
     * persisting file and makes sure the files get put in the right place when the
     * user adds movies.
     */
    private void loadTargetDirectory() {

        File persistingData = new File(getClass().getClassLoader().getResource("persisting/targetDirectory.txt").getFile());

        List<String> lines = IOHelper.readLines(persistingData);
        if (lines.size() >= 1)
            targetDirectory = lines.get(0);
        else
            // This will make it so that if no target directory is set, no errors will occur
            // when someone adds a movie, but nothing happens.
            targetDirectory = "";

    }

    /**
     * Called at startup, picks a random movie from all available movies on the disk
     * to display on the info bar. If there are no movies in the target directory,
     * then info on how to get a movie is displayed.
     */
    private void pickActiveMovie() {

        // If the user doesn't have any movies, let them know how to get some.
        // Otherwise, pick a random one to showcase in the info bar.
        if (movieList.size() == 0)
            activeMovie = new MovieModel("", "No Movies.", "", "Be sure to set up a folder on" +
                    " your drive to store your movies with FILE > SET TARGET DIRECTORY. Then, you can add movies with" +
                    " FILE > ADD. Enjoy watching!", "");
        else {
            Random rand = new Random();
            int index = rand.nextInt(movieList.size());
            activeMovie = movieList.get(index);
        }
    }

    /**
     * Pulls information from IMDb's HTML and uses that to create a folder for storing
     * the movie. The folder is located directly under targetDirectory and it contains
     * an info txt file with text information, an image file with the movie's poster,
     * and the movie itself.
     * @param pathString where the movie is moved into the program from.
     * @param IMDb a link to the IMDb page of the movie.
     */
    public void addMovie(String pathString, String IMDb) {

        // Instantiate scraper and if it fails, exit method
        IMDbScraper scraper = null;
        try {
            scraper = new IMDbScraper(IMDb);
        } catch (IOException e) {
            System.out.println("Couldn't load IMDb link");
            return;
        }

        // Get info from scraper
        String title = scraper.getTitle();
        String year = scraper.getYear();
        String info = scraper.getInfo();
        String desc = scraper.getDescription();
        String poster = scraper.getPoster();
        MovieModel newMovie = new MovieModel(poster, title, year, info, desc);

        // Make list of lines to be saved to disk
        List<String> lines = new ArrayList<>();
        lines.add(title);
        lines.add(year);
        lines.add(info);
        lines.add(desc);

        // Save data on disk
        String folderName = title + " " + year;
        String folderPath = targetDirectory + "/" + folderName;
        IOHelper.makeDirectory(new File(folderPath));
        IOHelper.downloadImage(new File(folderPath + "/poster"), poster);
        IOHelper.writeLines(new File(folderPath + "/info"), lines);
        IOHelper.moveFile(new File(pathString), new File(folderPath + "/movie"));

        loadMovieList();

    }

    /**
     * Deletes the movie from the file system and reloads the movie list to get rid of
     * it.
     */
    public void deleteMovie() {

        // Delete
        String folderPath = targetDirectory + "/" + activeMovie.getTitle() + " " + activeMovie.getYear();
        System.out.println(IOHelper.deleteFile(new File(folderPath)));

        // Reload
        loadMovieList();
        pickActiveMovie();

    }

    /**
     * Starts a new thread to download a movie from the app's file system onto the
     * user's hard drive. It can be downloading in the background.
     * @param destDir the location to copy the movie to.
     */
    public void downloadMovie(String destDir) {

        // Get source and dest files
        String pathToMovieDir = targetDirectory + "/" + activeMovie.getTitle() + " " + activeMovie.getYear();
        File source = new File(pathToMovieDir + "/movie");
        File dest = new File(destDir + "/" + activeMovie.getTitle() + " " + activeMovie.getYear());

        Task<Double> task = new Task<Double>() {

            @Override
            protected Double call() throws Exception {
                long totalBytes = source.length();
                long bytesCopied = 0;

                try {

                    InputStream in = new FileInputStream(source);
                    OutputStream out = new FileOutputStream(dest);

                    byte[] buffer = new byte[1024];
                    int len = 0;

                    while ((len = in.read(buffer)) > 0) {
                        out.write(buffer, 0, len);
                        bytesCopied += len;
                        double progress = (double)bytesCopied / (double)totalBytes;
                        setDownloadProgress(progress);
                    }

                    // JAVA BUG - Automatic garbage collection sometimes fails and causes, in this case, the folders
                    // containing the movie info to not be deleted when the user wants. The solution is to close,
                    // flush, nullify, then call garbage collection manually.
                    in.close();
                    out.flush();
                    out.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.gc();
                }

                return (double)bytesCopied / (double)totalBytes;
            }
        };

        task.setOnSucceeded((e) -> {
            setDownloading(false);
            setDownloadProgress(0);
        });

        Thread t = new Thread(task);
        setDownloading(true);
        t.start();

    }

    /**
     * Gets the active movie. This is the movie that is currently displayed on the
     * info bar.
     * @return active movie.
     */
    public MovieModel getActiveMovie() {

        return activeMovie;

    }

    /**
     * Gets the search filter.
     * @return search filter.
     */
    public String getFilter() {

        return this.filter;

    }

    /**
     * Gets the list of movies loaded.
     * @return list of movies loaded.
     */
    public ArrayList<MovieModel> getMovieList() {

        return movieList;

    }

    /**
     * Gets the maximum number of pages given the movies saved in the file system.
     * @return max pages.
     */
    public int getNumPages() {

        if (targetDirectory.isEmpty()) return 1;

        File dir = new File(targetDirectory);
        File[] files = dir.listFiles();

        // Applies the filter from the user's search.
        // Nothing changes if there is no search active.
        ArrayList<File> filteredFiles = new ArrayList<>();
        for (File file : files)
            if (filter.equals("") || file.getName().toLowerCase().contains(filter.toLowerCase()))
                filteredFiles.add(file);

        return filteredFiles.size() / moviesPerPage + 1;

    }

    /**
     * Gets the current page.
     * @return current page.
     */
    public int getPage() {

        return page;

    }

    /**
     * Gets the current target directory, where the movies and info are saved.
     * @return target directory.
     */
    public String getTargetDirectory() {

        return targetDirectory;

    }

    /**
     * Based on the current page, number of pages, and search filter, this clears the
     * movieList and refills it with the necessary MovieModels.
     */
    public void loadMovieList() {
        // Stop if there is no target dir
        if (targetDirectory.isEmpty()) return;

        File dir = new File(targetDirectory);
        File[] files = dir.listFiles();

        // Applies the filter from the user's search.
        // Nothing changes if there is no search active.
        ArrayList<File> filteredFiles = new ArrayList<>();
        for (File file : files)
            if (filter.equals("") || file.getName().toLowerCase().contains(filter.toLowerCase()))
                filteredFiles.add(file);

        // Fills the movie list by looping through the list of files. It skips the first however many
        // files based on what the active page is. Then, it iterates until it reaches the limit for
        // movies on a page or until there are no movies left in the filtered search.
        movieList.clear();
        int startIndex = (page - 1) * moviesPerPage;
        for (int i = startIndex; i < startIndex + moviesPerPage && filteredFiles.size() > i; i++) {
            // Reads the info file so it can make a MovieModel
            List<String> lines = IOHelper.readLines(new File(filteredFiles.get(i) + "/info"));
            // Grabs the String path to the poster
            String poster = filteredFiles.get(i).getPath() + "/poster";
            // Makes the MovieModel object and adds it to the list
            MovieModel movie = new MovieModel(poster, lines.get(0), lines.get(1), lines.get(2), lines.get(3));
            movieList.add(movie);
        }
    }

    /**
     * Makes the page decrease by one, if possible.
     */
    public void pageDown() {

        if (page > 1)
            page--;
        loadMovieList();

    }

    /**
     * Makes the page increase by one, if possible
     */
    public void pageUp() {

        File dir = new File(targetDirectory);
        File[] files = dir.listFiles();

        // Applies the filter from the user's search.
        // Nothing changes if there is no search active.
        ArrayList<File> filteredFiles = new ArrayList<>();
        for (File file : files)
            if (filter.equals("") || file.getName().toLowerCase().contains(filter.toLowerCase()))
                filteredFiles.add(file);

        int totalMovies = filteredFiles.size();
        if (totalMovies > page * moviesPerPage)
            page++;
        loadMovieList();

    }

    /**
     * Launches the movie on the user's default video playback software.
     */
    public void playMovie() {

        String pathToMovieDir = targetDirectory + "/" + activeMovie.getTitle() + " " + activeMovie.getYear();
        File movieFile = new File(pathToMovieDir + "/movie");

        // isDesktopSupported and the Thread are necessary to open more than one movie.
        // Otherwise, the program crashes. It's probably a java problem, but this is a workaround.
        if (Desktop.isDesktopSupported()) {
            new Thread(() -> {
                try {
                    // Open the movie with the default movie player of the OS
                    Desktop.getDesktop().open(movieFile);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

    }

    /**
     * Sets the active movie, which is shown on the info bar. It's found by searching
     * for a matching title and year in the movie list.
     * @param title title of the movie being set to active.
     * @param year year of the movie being set to active.
     */
    public void setActiveMovie(String title, String year) {

        // Searches for the movie in the list by title and year
        for (MovieModel movie : movieList) {
            if (movie.getTitle().equals(title) && movie.getYear().equals(year)) {
                activeMovie = movie;
                return;
            }
        }

    }

    /**
     * Sets the search filter.
     * @param filter new search filter.
     */
    public void setFilter(String filter) {

        this.filter = filter;
        page = 1;

    }

    /**
     * Sets the page being shown. If the page is too low, it defaults to the minimum.
     * If the page is too high, it defaults to maximum. Currently not in use.
     * Could be implemented later if user is allowed to type a page.
     * @param page new page.
     */
    public void setPage(int page) {

        if (page > movieList.size() / moviesPerPage + 1)
            this.page = movieList.size() / moviesPerPage + 1;
        else if (page < 1)
            this.page = 1;
        else
            this.page = page;

    }

    /**
     * Sets the target directory, where the app stores information and movies.
     * @param targetDirectory new target directory.
     */
    public void setTargetDirectory(String targetDirectory) {

        // Get the persisting file
        File persisting = new File(getClass().getClassLoader().getResource("persisting/targetDirectory.txt").getFile());

        // Writes the targetDirectory onto the persisting file
        List<String> target = new ArrayList<>();
        target.add(targetDirectory);
        IOHelper.writeLines(persisting, target);

        // Refresh
        this.targetDirectory = targetDirectory;
        this.loadMovieList();
        this.pickActiveMovie();

    }


    // PROPERTY METHODS

    /**
     * @return if a download is in progress.
     */
    public final boolean isDownloading() {

        return isDownloading.get();

    }

    /**
     * @param downloading if it's downloading.
     */
    public final void setDownloading(boolean downloading) {

        isDownloading.set(downloading);

    }

    /**
     * @return downloading property.
     */
    public BooleanProperty downloadingProperty() {

        return isDownloading;

    }

    /**
     * @return the download progress.
     */
    public final double getDownloadProgress() {

        return downloadProgress.get();

    }

    /**
     * @param value download progress.
     */
    public final void setDownloadProgress(double value) {

        downloadProgress.set(value);

    }

    /**
     * @return download progress property.
     */
    public DoubleProperty downloadProgressProperty() {

        return downloadProgress;

    }

}
