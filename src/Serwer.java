import projektPaczkKlient.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Serwer {
    public static void main(String[] args){
        //creating 5 directories
        String dirpath = "C:\\Users\\kowal\\IdeaProjects\\projekcior\\cos";
        for(int i =1;i<6;i++){
            File dir = new File(dirpath+i);
            dir.mkdir();
        }
        try (ServerSocket listener = new ServerSocket(59898)){
            System.out.println("server is running");
            ExecutorService pool = Executors.newFixedThreadPool(5);
            while (true){
                pool.execute(new ClientHandler(listener.accept(),dirpath));
            }
        }
        catch (IOException ioex){
            System.out.println("problem with setting up server");
        }
    }

    private static class ClientHandler implements Runnable{
        private Socket socket;
        private String filepath;
        ClientHandler(Socket socket,String directory) {
            this.socket=socket;
            this.filepath = directory+"1\\";
        }

        @Override
        public void run() {
            System.out.println("connected to "+ socket);
            try {
                while (true) {
                    /*Receiver odbieracz =*/ new Receiver(socket, "username", filepath).run();
//                    odbieracz.run();
                }
            }
            finally {
                System.out.println("client fucked up something");
            }
        }
    }

}
