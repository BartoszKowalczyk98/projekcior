package projektPaczkKlient;

import java.io.*;

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

    static boolean appendingToCSVFile(String username,String filename ,String filepath) throws  IOException{

        File csvfile = new File(filepath);

        if(csvfile.isFile()) {
            FileWriter cvsWriter = new FileWriter(csvfile);
            cvsWriter.append(username);
            cvsWriter.append(',');
            cvsWriter.append(filename);
            cvsWriter.append('\n');
            cvsWriter.flush();
            cvsWriter.close();

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

    public boolean searchingForFiles(String filename) throws IOException {
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



}
