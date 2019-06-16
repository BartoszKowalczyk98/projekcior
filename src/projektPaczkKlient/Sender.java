package projektPaczkKlient;

import java.io.*;
import java.net.Socket;

public class Sender implements Runnable{
    private Socket socket;
    private String from;
    private String filepath;
    public Sender(Socket socket,String from,String filepath) {
        this.socket=socket;
        this.from=from;
        this.filepath=filepath;
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

            ObjectOutputStream obs = new ObjectOutputStream(socket.getOutputStream());
            obs.writeObject(new FileWithUsername(this.from,bytesarray,filename));

        }
        catch(IOException ioex){
            ioex.printStackTrace();
            System.out.println("IOException in thread for sending a message");
        }
    }
}
