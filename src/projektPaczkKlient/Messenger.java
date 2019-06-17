package projektPaczkKlient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public interface Messenger {
    static boolean sendMessage(Socket socket, String message) {
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(message);
        }
        catch (IOException ioex){
            return false;
        }
        finally {
            return true;
        }

    }
    static String receiveMessage(Socket socket){
        try{
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            String zwrot = dataInputStream.readUTF();
            return zwrot;
        }
        catch (IOException ioex)
        {
            return "Error";
        }

    }
}
