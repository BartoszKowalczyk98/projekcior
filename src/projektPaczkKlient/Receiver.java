package projektPaczkKlient;

import java.io.*;
import java.net.Socket;

import static projektPaczkKlient.CSVFileHandler.appendingToCSVFile;


public class Receiver implements Runnable{
    public ObjectInputStream ois;
    public String filepath;
    public String from;
    FileWithUsername fileWithUsername;
    private String newowner="null";

    public Receiver(ObjectInputStream objectInputStream,String from,String filepath) {
        this.ois = objectInputStream;
        this.from=from;
        this.filepath=filepath;
    }
    /*
    public Receiver(Socket socket,String from,String filepath, String forwho) {
        this.socket = socket;
        this.filepath=filepath+"\\";
        this.from=from;
        this.newowner=forwho;

    }*/

    @Override
    public void run() {

        try {
            ///wczytanie obiektu ze streama
            fileWithUsername =(FileWithUsername) ois.readObject();


            //stworzenie pliku w folderze
            File file = new File(filepath+fileWithUsername.filename);
            if(file.exists()){
                file.delete();
            }
            file.createNewFile();

            //wpisanie do pliku
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileWithUsername.bytesarray);

            //zamkniecie tego co nie potrzebne
            fos.close();
            if(from.equals("server")){
                //adding entry into csv file
                if(this.newowner.equals("null"))
                    appendingToCSVFile(fileWithUsername.Username, fileWithUsername.filename,filepath+"info.csv");
                else
                    appendingToCSVFile(this.newowner, fileWithUsername.filename,filepath+"info.csv");
            }
        }
        catch (FileNotFoundException fifex){
            System.out.println("file not found exception in receiving!");
        }
        catch (IOException ioex)
        {
            ioex.printStackTrace();
            System.out.println("IOException in  receiving the file!");
        }
        catch (ClassNotFoundException cnfex){
            System.out.println("Error class not found!");
            cnfex.printStackTrace();
        }
        finally {
            return;
        }
    }
}
