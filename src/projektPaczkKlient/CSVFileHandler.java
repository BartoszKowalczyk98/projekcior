package projektPaczkKlient;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public interface CSVFileHandler {
    //public String filepath;

    /*public CSVFileHandler(String filepath) {
        this.filepath = filepath;
    }*/

    static boolean createCSVFile(String filepath) throws  IOException{

            File file = new File(filepath);
            if(file.exists())
                return false;
            else
                file.createNewFile();
                return true;
    }
    ///teoretycznie dziala bez zastrzezen
    static boolean appendingToCSVFile(String username,String filename ,String filepath) throws  IOException{

        File csvfile = new File(filepath);

        if(csvfile.isFile()) {
            FileWriter cvsWriter = new FileWriter(csvfile,true);
            BufferedWriter bufferedWriter =new BufferedWriter(cvsWriter);
            PrintWriter printWriter = new PrintWriter(bufferedWriter);
            printWriter.println(username+','+filename);
            printWriter.flush();
            printWriter.close();

            return true;
        }
        return false;
    }
/*

    public boolean searchingForOwner(String filename) throws IOException {
        BufferedReader cvsreader = new BufferedReader(new FileReader(filepath));
        String row;
        while ((row = cvsreader.readLine())!=null){
            String[] data = row.split(",");
            if(data[1].equals(filename))
                return true;
        }
        return false;
    }
*/
// TODO: 20.06.2019 opracowanc tak zeby zwracalo liste plikow nalezacych do danego usera
    static ArrayList<String> searchingForFiles(String filepath,String username) throws IOException {
        BufferedReader cvsreader = new BufferedReader(new FileReader(filepath));
        String row;
        ArrayList<String> outcome = new ArrayList<>();
        while ((row = cvsreader.readLine())!=null){
            String[] data = row.split(",");
            if(data[0].equals(username)){
                outcome.add(data[1]);
            }
        }
        return outcome;//zwraca liste sciezek do plikw ktore zgadzaja sie co do username'a
    }


}
