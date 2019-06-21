import projektPaczkKlient.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;
import static projektPaczkKlient.CSVFileHandler.createCSVFile;
import static projektPaczkKlient.Messenger.receiveMessage;
import static projektPaczkKlient.Messenger.sendMessage;

public class Serwer {
    public static Map<String,Socket> mapOfClients;
    private static
    public static void main(String[] args){
        //creating 5 directories
        String dirpath = "C:\\Users\\kowal\\Desktop\\projek\\Dysk";
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
    private static class Controller implements Runnable {
        ArrayList<Receiver> taskList;
        public ExecutorService pool;

        public Controller(ExecutorService pool) {
            this.pool = pool;
            taskList = new ArrayList<>();
        }
        private int numberofclientsconnected(){
            return mapOfClients.size();
        }

        @Override
        public void run() {

        }
    }
    private static class ClientHandler implements Runnable{
        private Socket socket;
        //private String filepath;
        private List<DirectroyWithSize> disc = new ArrayList<>();
        private String  username;
        final Semaphore semaphore;
        ClientHandler(Socket socket,String directory) {
            this.socket=socket;
            this.semaphore = new Semaphore(1);
            this.username="unknown";
            for(int i =1;i<6;i++){
                disc.add(new DirectroyWithSize(directory+i));
            }

        }

        @Override
        public void run() {
            System.out.println("connected to "+ socket);
            try {

                username =receiveMessage(socket);
                if(mapOfClients.containsKey(username)) {
                    sendMessage(socket,"nametaken");
                    socket.close();
                    return;
                }
                sendMessage(socket,"welcome");
                mapOfClients.put(username,socket);
                sendEverything();
                //wielki while operujący wszystkim
                String whatClientSaid;
                do {
                    whatClientSaid = receiveMessage(socket);
                    if(whatClientSaid.equals("clientlist")){
                        sendclientlist();
                    }
                    else if(whatClientSaid.equals("sendto")){
                        receivefor();
                    }
                    else if(whatClientSaid.equals("anythingnew")){
                        sendEverything();
                    }
                    else{
                        int howmany = Integer.parseInt(whatClientSaid);
                        givemefiles(howmany);
                    }
                }while(!whatClientSaid.equals("kaniet"));//dopóki nie dostanie kaniet to ma czekac na wiadomosc
                socket.close();
            }
            catch (IOException ioex) {
                System.out.println("IOException in server running!");
            }
            catch (InterruptedException intex) {
                System.out.println("sth was interrupted");
            }
            finally {

                System.out.println("client fucked up something");
            }

        }
        private void sendclientlist(){
            String clientlist = new String();
            for(String key :mapOfClients.keySet()){
                clientlist= clientlist+','+key;
            }
            sendMessage(socket,clientlist);
        }
        private void receivefor(){
            String forwho =receiveMessage(socket);
            if(mapOfClients.containsKey(forwho)){
                sendMessage(socket,"howmany");
                int howmany=Integer.parseInt(receiveMessage(socket));
                ExecutorService pool;
                pool = Executors.newFixedThreadPool(howmany);

                for(int i =0;i<howmany;i++)
                {
                    disc.get(0).updateSize();
                    Collections.sort(disc);
                    pool.execute(new Receiver(socket,"server",disc.get(0).dirpath,semaphore,forwho));
                    try {
                        sleep(1000);//tymczasowy sleep nie do konca potrzebny ale jest
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    semaphore.release();
                }
                sendMessage(socket,"received");
            }
            else
                sendMessage(socket,"nosuchclient");
        }
        private void sendEverything() throws  IOException, InterruptedException{
            String listoffilenames = receiveMessage(socket);
            String [] arroffilenames = listoffilenames.split(";");
            ArrayList<String> currentlyOwnedByUser = new ArrayList<>();
            for(int i =0;i<arroffilenames.length;i++){
                currentlyOwnedByUser.add(arroffilenames[i]);
            }

            List<String> whatDoYouOwnDisc;
            ArrayList<String> toBeSent = new ArrayList<>();
            for(int disciterator =0;disciterator<5;disciterator++){
                semaphore.acquire();
                whatDoYouOwnDisc= CSVFileHandler.searchingForFiles(disc.get(disciterator).dirpath+"\\info.csv",username);
                semaphore.release();
                for(String s : whatDoYouOwnDisc){
                    if(!currentlyOwnedByUser.contains(s)){
                        toBeSent.add(disc.get(disciterator).dirpath+"\\"+s);
                    }
                }

            }
            int howmany =toBeSent.size();
            sendMessage(socket,String.valueOf(howmany));
            ExecutorService pool = Executors.newFixedThreadPool(howmany);
            for(int i =0;i<toBeSent.size();i++){
                pool.execute(new Sender(socket,"server",toBeSent.get(i),semaphore));
            }
            sendMessage(socket,"nomore");

        }
        private void givemefiles(int howmany){
            ExecutorService pool;
            pool = Executors.newFixedThreadPool(howmany);

            for(int i =0;i<howmany;i++)
            {
                disc.get(0).updateSize();
                Collections.sort(disc);
                pool.execute(new Receiver(socket,"server",disc.get(0).dirpath,semaphore));
                /*try {
                    sleep(1000);//tymczasowy sleep nie do konca potrzebny ale jest
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/

                semaphore.release();
            }
            sendMessage(socket,"received");
        }

    }

}






// TODO: 16.06.2019 drugi klient

// TODO: 21.06.2019 semafor na csv
/*
C. Serwer:
+ 5 folderow, ktore symulujÄ 5 serwerow lub 5 dyskow
+ Klient wysyla np. 10 plikow, wiec serwer uruchamia iles watkow na ktorych rownolegle kopiuje pliki do tych dyskow (folderow)
+ Wymagany jest kontroler, ktory tak rozlozy ruch, ze do kazdego z dyskow (folderow) jednoczesnie jest kopiowana taka sama liczba plikow
+- Jezeli podlaczy sie drugi klient, ktory zacznie wysylac pliki, nie moze on czekac az skoncza sie zadania pierwszego klienta. Lista zadan na serwerze musi ulec reorganizacji, tak aby obydwaj klienci mieli wrazenie natychmiastowej obslugi (zaproponuj stosowny algorytm)
+ Na kazdym dysku serwera znajduje sie plik tekstowy (np. csv), w ktorym jest opisana zawartosc danego dysku i kto jest jego wlascicielem. Zauwaz, ze plik bedzie uaktualniany przez wiele watkow. Rozwiaz ten problem.
- W celu wizualizacji symulacji na niewielkiej liczbie uzytkownikow, czas kopiowania ma byc sztucznie wydluzony poprzez usypianie watku na losowa liczbe sekund.
- Serwer posiada panel graficzny (np. Java FX) pokazujacy zawartosc 5ciu dyskow (serwerow) oraz aktualnie wykonywane operacje.
 */
