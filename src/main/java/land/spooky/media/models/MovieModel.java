package land.spooky.media.models;

/**
 * @author Daniel Gelber
 * @version 1.0
 * Created 2018-07-14
 * Last Modified 2018-07-16
 *
 * This is a model of a movie. It contains all the logic needed to display
 * the necessary information onto a GUI. Information cannot be changed.
 */
public class MovieModel {

    private String poster;
    private String title;
    private String year;
    private String info;
    private String description;


    /**
     * Constructor for the movie model.
     * @param poster the string uri to the poster.
     * @param title the title.
     * @param year the year it was released.
     * @param info the technical info (Rating, runtime, etc).
     * @param description the summary.
     */
    public MovieModel(String poster, String title, String year, String info, String description) {
        this.poster = poster;
        this.title = title;
        this.year = year;
        this.info = info;
        this.description = description;
    }

    /**
     * @return poster.
     */
    public String getPoster() {
        return poster;
    }

    /**
     * @return title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return year.
     */
    public String getYear() {
        return year;
    }

    /**
     * @return info.
     */
    public String getInfo() {
        return info;
    }

    /**
     * @return description.
     */
    public String getDescription() {
        return description;
    }

}
