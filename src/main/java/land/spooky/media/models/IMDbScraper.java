package land.spooky.media.models;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

/**
 * @author Daniel Gelber
 * @version 1.0
 * Created 2018-07-18
 * Last Modified 2018-07-18
 *
 * This class has a JSoup document. A document is the HTML data for a webpage, given
 * by the String URL passed in the constructor. Each IDMbScraper will be able to gather
 * the following information about the movie in the link: Title, year, information,
 * description, and poster link.
 */
public class IMDbScraper {

    private Document doc;

    /**
     * After instantiating the IMDbScraper, you cannot change the webpage. In order
     * to gather information about a different movie, make a new instance.
     * @param link the link to the IMDb page with the movie you want information for.
     * @throws IOException if the web page cannot be connected to.
     */
    public IMDbScraper(String link) throws IOException {

        doc = Jsoup.connect(link).get();

    }

    /**
     * Gets the title of the movie.
     * @return the title.
     */
    public String getTitle() {

        Elements titleWrapper = doc.getElementsByClass("title_wrapper");
        // There should always only be one
        if (titleWrapper.size() != 1)
            return null;
        Elements titleH1 = titleWrapper.get(0).getElementsByTag("h1");
        // There should be at least one
        if (titleH1.size() == 0)
            return null;
        String titleYear = titleH1.get(0).text();

        // Separate the year from the title. The way the Document gets the data causes
        // the space between the title and year to be char=160, which is a non-breaking
        // space. The rest of the spaces are normal. Therefore the string can be split
        // by char 160.
        String[] titleYearWords = titleYear.split(Character.toString((char)160));
        return titleYearWords[0];

    }

    /**
     * Gets the year of the movie.
     * @return the year.
     */
    public String getYear() {

        Element yearWrapper = doc.getElementById("titleYear");
        return yearWrapper.child(0).text();

    }

    /**
     * Gets the info of the movie.
     * @return the info.
     */
    public String getInfo() {

        Elements subtext = doc.getElementsByClass("subtext");
        // There should be at least one
        if (subtext.size() == 0)
            return null;
        return subtext.get(0).text();

    }

    /**
     * Gets the description of the movie.
     * @return the description.
     */
    public String getDescription() {

        Elements summaryText = doc.getElementsByClass("summary_text");
        // There should be at least one
        if (summaryText.size() == 0)
            return null;
        return summaryText.get(0).text();

    }

    /**
     * Gets the poster URL of the movie as a string.
     * @return the string URL of the poster.
     */
    public String getPoster() {

        Elements posterDiv = doc.getElementsByClass("poster");
        // There should be at least one
        if (posterDiv.size() == 0)
            return null;
        Elements posterA = posterDiv.get(0).getElementsByTag("a");
        // There should be at least one
        if (posterA.size() == 0)
            return null;
        Elements posterImg = posterA.get(0).getElementsByTag("img");
        // There should be at least one
        if (posterImg.size() == 0)
            return null;
        return posterImg.get(0).attr("src");

    }

}
