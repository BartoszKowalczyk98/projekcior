package projektPaczkKlient;

import java.io.*;
import java.net.Socket;


public class Sender implements Runnable{
    private ObjectOutputStream oos;
    private String from;
    private String filepath;

    //from means who started this class/thread
    public Sender(ObjectOutputStream objectOutputStream, String from, String filepath) {
        this.oos = objectOutputStream;
        this.from=from;
        this.filepath=filepath;

        //this.run();
    }
    private byte[] fileToBytearr(String filepath) throws  IOException {
        File file = new File(filepath);
        byte[] bytesarray = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesarray);
        fis.close();
        return bytesarray;
    }
    @Override
    public void run()  {
        try {

            File f = new File(this.filepath);

            if ((!f.exists()) || f.isDirectory()) return;// if file does not exist or is a directory
            byte[] bytesarray = fileToBytearr(this.filepath);
            String filename = f.getName();


            oos.writeObject(new FileWithUsername(this.from,bytesarray,filename));

        }
        catch(IOException ioex){
            ioex.printStackTrace();
            System.out.println("IOException in thread for sending a message");
        }
        finally {
            return;
        }
    }
}
