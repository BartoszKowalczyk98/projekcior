package projektPaczkKlient;

import java.io.*;
import java.net.Socket;

import static java.lang.Thread.sleep;
import static projektPaczkKlient.CSVFileHandler.appendingToCSVFile;

public class Receiver implements Runnable{
    public Socket socket;
    public String from;
    public String filepath;
    public Receiver(Socket socket, String from, String whereto) {
        this.socket = socket;
        this.from = from;
        this.filepath = whereto+"\\";
    }

    @Override
    public void run() {

        try {
            ///wczytanie obiektu ze streama
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            FileWithUsername fileWithUsername =(FileWithUsername) ois.readObject();

            //stworzenie pliku na dysku
            File file = new File(filepath+fileWithUsername.filename);
            file.createNewFile();
            if(file.exists()){
                file.delete();
            }

            //wpisanie do pliku
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(fileWithUsername.bytesarray);

            //zamkniecie tego co nie potrzebne
            fos.close();
            if(from.equals("server")){
                //adding entry into csv file
                appendingToCSVFile(fileWithUsername.Username,filepath+fileWithUsername.filename,filepath+"info.csv");
            }
            sleep(100);//tymczasowy sleep nie do konca potrzebny ale jest
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
        catch (InterruptedException intex){
            System.out.println("interrupted exception");
            intex.printStackTrace();
        }
        finally {
            return;
        }
    }
}
