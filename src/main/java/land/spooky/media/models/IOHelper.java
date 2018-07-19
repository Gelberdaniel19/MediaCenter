package land.spooky.media.models;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Daniel Gelber
 * @version 1.0
 * Created 2018-07-18
 * Last Modified 2018-07-18
 *
 * This class contains methods to assist in all the IO requirements of the MainModel.
 * It handles the exceptions, and when applicable, returns true or false based on the
 * success of the process.
 */
public final class IOHelper {

    /**
     * Recursively delete a directory and its entire tree.
     * @param target the digital victim's address.
     * @return true if successful.
     */
    public static final boolean deleteFile(File target) {

        // Recursively delete all files under this
        if (target.isDirectory())
            for (File file : target.listFiles())
                deleteFile(file);
        return target.delete();

    }

    /**
     * Downloads an image from the internet to a target path on the local disk.
     * @param target the path to where the image gets saved on the local disk.
     * @param imageSource the web url to the image.
     * @return true if successful.
     */
    public static final boolean downloadImage(File target, String imageSource) {

        try {
            // Open input and output streams
            URL link = new URL(imageSource);
            InputStream is = link.openStream();
            OutputStream os = new FileOutputStream(target);

            // Set up buffer and use that to save the data from the input
            // stream to the output stream.
            byte[] b = new byte[2048];
            int length;
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // It worked because it didn't throw an error.
        return true;

    }

    /**
     * Makes a directory in the parent folder.
     * @param target where the directory goes.
     * @return true if successful.
     */
    public static final boolean makeDirectory(File target) {

        return target.mkdirs();

    }

    /**
     * Moves a non-directory file to a new location.
     * @param source the file to be moved.
     * @param target the file in its new location.
     * @return true if successful.
     */
    public static final boolean moveFile(File source, File target) {

        // Try to move the file
        Path temp;
        try {
            temp = Files.move(source.toPath(), target.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        // If the new file doesn't exist, it failed.
        if (temp == null)
            return false;
        else
            return true;

    }

    /**
     * Reads and returns the lines in a text file.
     * @param source the text file.
     * @return a List of strings containing the lines of the text file.
     */
    public static final List<String> readLines(File source) {

        List<String> lines = new ArrayList<>();

        // Read the lines or return null if theres an exception
        try {
            BufferedReader br = new BufferedReader(new FileReader(source));
            String line;
            while ((line = br.readLine()) != null)
                lines.add(line);
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return lines;

    }

    /**
     * Makes a text file with the specified text.
     * @param target the text file.
     * @param lines the list of lines to be written.
     * @return true if successful.
     */
    public static final boolean writeLines(File target, List<String> lines) {

        // Try to make the file and write the lines to it
        try {
            PrintWriter writer = new PrintWriter(target);
            for (String line : lines)
                writer.println(line);
            writer.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        // It worked because it didn't throw an error.
        return true;

    }


}
