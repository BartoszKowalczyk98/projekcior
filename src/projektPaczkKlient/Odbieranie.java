package projektPaczkKlient;

import java.io.*;
import java.net.Socket;

public class Odbieranie implements Runnable{
    public Socket socket;
    public String from;
    public String filepath;
    public Odbieranie(Socket socket, String from, String whereto) {
        this.socket = socket;
        this.from = from;
        this.filepath = whereto;
    }

    @Override
    public void run() {
        File file = new File(filepath);
        if(file.exists()){
            System.out.println("Error file already exists!");
            return;
        }
        try {
            file.createNewFile();
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            byte[] bytearr =new byte [(byte)0];
            dis.read(bytearr);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytearr);
            dis.close();
            fos.close();
        }
        catch (IOException ioex)
        {
            System.out.println("IOException in  receiving the file!");
        }
    }
}
