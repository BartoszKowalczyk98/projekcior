package projektPaczkKlient;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;
import static projektPaczkKlient.CSVFileHandler.appendingToCSVFile;

public class Receiver implements Runnable{
    public Socket socket;
    public String from;
    public String filepath;
    final Semaphore semaphore;
    private String newowner="null";
    public Receiver(Socket socket, String from, String whereto, Semaphore semaphore) {
        this.socket = socket;
        this.from = from;
        this.filepath = whereto+"\\";
        this.semaphore=semaphore;
        //this.run();
    }
    public Receiver(Socket socket, String from, String whereto, Semaphore semaphore,String forwho) {
        this.socket = socket;
        this.from = from;
        this.filepath = whereto+"\\";
        this.semaphore=semaphore;
        this.newowner=forwho;
        //this.run();
    }

    @Override
    public void run() {

        try {
            ///wczytanie obiektu ze streama
            semaphore.acquire();
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            FileWithUsername fileWithUsername =(FileWithUsername) ois.readObject();

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
                    appendingToCSVFile(fileWithUsername.Username,filepath+fileWithUsername.filename,filepath+"info.csv");
                else
                    appendingToCSVFile(this.newowner,filepath+fileWithUsername.filename,filepath+"info.csv");
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
        catch (InterruptedException intex){
            System.out.println("interrupted exception");
            intex.printStackTrace();
        }
        finally {
            semaphore.release();
            return;
        }
    }
}
