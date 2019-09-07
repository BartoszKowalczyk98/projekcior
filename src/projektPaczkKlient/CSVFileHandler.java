package projektPaczkKlient;

import java.io.*;
import java.util.ArrayList;

/**
 * interface that is used to deal with .csv files
 */
public interface CSVFileHandler {

    /**
     * method that creats .csv file
     * @param filepath path where that file is supposed to be created
     * @return boolean true means file created succesfully false if not
     * @throws IOException TBA
     */
    static boolean createCSVFile(String filepath) throws IOException {

        File file = new File(filepath);
        if (file.exists())
            return false;
        else
            file.createNewFile();
        return true;
    }

    /**
     * method that appends given username and filename to specified in filepath .csv file
     * @param username name of user that owns file
     * @param filename filename that belongs to username
     * @param filepath path to .csv file that we want other info appended
     * @return boolean returns true if given text was appended succesfully false otherwise
     * @throws IOException TBA
     */
    static boolean appendingToCSVFile(String username, String filename, String filepath) throws IOException {

        File csvfile = new File(filepath);

        if (csvfile.isFile()) {
            FileWriter cvsWriter = new FileWriter(csvfile, true);
            BufferedWriter bufferedWriter = new BufferedWriter(cvsWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            printWriter.println(username + ',' + filename);
            printWriter.flush();
            printWriter.close();

            return true;
        }
        return false;
    }

    /**
     * method that reads .csv file and returns list of files belonging to username
     * @param filepath path to .csv file
     * @param username name of user
     * @return arraylist string names of files beloging to username
     * @throws IOException TBA
     */
    static ArrayList<String> searchingForFiles(String filepath, String username) throws IOException {
        BufferedReader cvsreader = new BufferedReader(new FileReader(filepath));
        String row;
        ArrayList<String> outcome = new ArrayList<>();
        while ((row = cvsreader.readLine()) != null) {
            String[] data = row.split(",");
            if (data[0].equals(username)) {
                outcome.add(data[1]);
            }
        }
        return outcome;//zwraca liste sciezek do plikw ktore zgadzaja sie co do username'a
    }


}
