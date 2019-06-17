import projektPaczkKlient.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static projektPaczkKlient.CSVFileHandler.createCSVFile;
import static projektPaczkKlient.Messenger.receiveMessage;

public class Serwer {
    public static void main(String[] args){
        //creating 5 directories
        String dirpath = "C:\\Users\\kowal\\IdeaProjects\\projekcior\\Dysk";
        try {
            for(int i =1;i<6;i++){
                File dir = new File(dirpath+i);
                dir.mkdir();
                createCSVFile(dirpath+i+"\\info.csv");
            }
        }
        catch (IOException ioex)
        {
            System.out.println("problem with cvsfilecreating");
            return;
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
            return;
        }
    }

    private static class ClientHandler implements Runnable{
        private Socket socket;
        private String filepath;
        private List<DirectroyWithSize> disc = new ArrayList<>();

        ClientHandler(Socket socket,String directory) {
            this.socket=socket;
            this.filepath = directory;
            for(int i =1;i<6;i++){
                disc.add(new DirectroyWithSize(directory+i));
            }

        }

        @Override
        public void run() {
            System.out.println("connected to "+ socket);

            System.out.println(receiveMessage(socket));

            try {
                // TODO: 16.06.2019 tutaj wysylanie na poczatku polaczenia
                while (true) {
                    disc.get(0).updateSize();
                    Collections.sort(disc);
                    new Receiver(socket, "server", disc.get(0).dirpath).run();

                }
            }
            finally {
                System.out.println("client fucked up something");
            }
        }


    }

}


// TODO: 16.06.2019 drugi klient
// TODO: 16.06.2019 rownolegle przesylanie
