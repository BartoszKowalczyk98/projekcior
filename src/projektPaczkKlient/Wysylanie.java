package projektPaczkKlient;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.Socket;

public class Wysylanie implements Runnable{
    private Socket socket;
    private String from;
    private String filepath;
    public Wysylanie(Socket socket,String from,String filepath) {
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
            byte[] bytesarray = fileToBytearr(this.filepath);
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.write(bytesarray);
            dos.close();
        }
        catch(IOException ioex){
            System.out.println("IOException in thread for sending a message");
        }
    }
}
