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

            if(dir.mkdir()){
                System.out.println(dirpath+i+" Directory created succefully!");
            }
            else{
                System.out.println("something went wrong with creating directories");
            }
        }
        try (ServerSocket listener = new ServerSocket(59898)){
            System.out.println("server is running");
            ExecutorService pool = Executors.newFixedThreadPool(5);
            while (true){
                pool.execute(new ClientHandler(listener.accept()));
            }
        }
        catch (IOException ioex){
            System.out.println("problem with setting up server");
        }
    }
    private static class ClientHandler implements Runnable{
        private Socket socket;
        ClientHandler(Socket socket) { this.socket=socket; }

        @Override
        public void run() {
            System.out.println("connected to "+ socket);
        }
    }

}
