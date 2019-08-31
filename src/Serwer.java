import projektPaczkKlient.*;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import static projektPaczkKlient.CSVFileHandler.createCSVFile;
import static projektPaczkKlient.Messenger.receiveMessage;
import static projektPaczkKlient.Messenger.sendMessage;

public class Serwer {
    public static Map<String,Socket> mapOfClients;
    public static List<DirectroyWithSize> disc = new ArrayList<>();

    public static void main(String[] args){
        //creating 5 directories
        String dirpath = "C:\\Users\\kowal\\Desktop\\projek\\Dysk";
        try {
            for(int i =1;i<6;i++){
                File dir = new File(dirpath+i);
                dir.mkdir();
                createCSVFile(dirpath+i+"\\info.csv");
            }
            for(int i =1;i<6;i++){
                disc.add(new DirectroyWithSize(dirpath+i));
            }
        }
        catch (IOException ioex)
        {
            System.out.println("problem with cvsfilecreating");
            return;
        }
        try (ServerSocket listener = new ServerSocket(59898)){
            mapOfClients= new TreeMap<>();
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

    /////////////////////////////////////////////////////////////////////////////////////////////
    private static class ClientHandler implements Runnable{
        private Socket socket;


        private String  username;
        final Semaphore semaphore;
        ClientHandler(Socket socket,String directory) {
            this.socket=socket;
            this.semaphore = new Semaphore(1);
            this.username="unknown";


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
                //sendEverything();
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
                sendMessage(socket,"gimme");

                /*ReceiverThreads recfor = new ReceiverThreads(1,socket,forwho);
                receiverThreadsArrayList.add(recfor);
                while(!recfor.done);*/

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
            if(howmany>0) {
                ExecutorService pool = Executors.newFixedThreadPool(howmany);
                for (int i = 0; i < howmany; i++) {
                    pool.execute(new Sender(socket, "server", toBeSent.get(i)));
                }
                while (!pool.isTerminated()) ;
            }
            sendMessage(socket,"nomore");

        }
        private void givemefiles(int howmany){
            //ReceiverThreads gibfilez = new ReceiverThreads(howmany,socket);
            System.out.println("mam odbierac");

            /*receiverThreadsArrayList.add(gibfilez);
            while(!gibfilez.done);
            */
            sendMessage(socket,"received");
        }

    }

}








// TODO: 21.06.2019 semafor na csv
// TODO: 31.08.2019 cos typu zadanie co walne do kolejki i bedzie dzialalo
// TODO: 31.08.2019 semafor na odbieranie i wysylanie
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
