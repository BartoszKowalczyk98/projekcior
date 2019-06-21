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
import java.util.concurrent.Semaphore;

import static java.lang.Thread.sleep;
import static projektPaczkKlient.CSVFileHandler.createCSVFile;
import static projektPaczkKlient.Messenger.receiveMessage;
import static projektPaczkKlient.Messenger.sendMessage;

public class Serwer {
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
                // TODO: 20.06.2019 tak jak teraz mysle to moze lepiej server zrobic jako taki hub ktory dostaje wiadomosci i odpala konkretne metody/watki
                //czytaj taka komunikacja klient mowi ze wysyla x plikow to server odpala x watkow
                //jak klient powie ze startup to sie odpali startup
                //ale pytanie czy nie bedzie sie zazebialo przesylanie bo wiadomosc i plik leci tym samym socketem

                username =receiveMessage(socket);
                List<String> whatDoYouOwnDisc;
                for(int disciterator =0;disciterator<5;disciterator++){
                    whatDoYouOwnDisc= CSVFileHandler.searchingForFiles(disc.get(disciterator).dirpath+"\\info.csv",username);
                    for(int listiterator = 0;listiterator<whatDoYouOwnDisc.size();listiterator++){
                        sendMessage(socket,"more");
                        new Sender(socket,username,whatDoYouOwnDisc.get(listiterator),semaphore).run();

                    }
                }
                sendMessage(socket,"nomore");
                String flaga = receiveMessage(socket);
                ExecutorService pool;
                while (!flaga.equals("kaniet")) {
                    disc.get(0).updateSize();
                    Collections.sort(disc);
                    //String howmany = receiveMessage(socket);
                    System.out.println(flaga);
                    int lim = Integer.parseInt(flaga);
                    pool = Executors.newFixedThreadPool(lim);

                    for(int i =0;i<lim;i++)
                    {
                        disc.get(0).updateSize();
                        Collections.sort(disc);
                        pool.execute(new Receiver(socket,"server",disc.get(0).dirpath,semaphore));
                        try {
                            sleep(1000);//tymczasowy sleep nie do konca potrzebny ale jest
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        /*while(!semaphore.tryAcquire()){
                            //label ze jest w trakcie wysylania czekaj tu kurwa


                            semaphore.release();
                        }*/

                    }
                    // TODO: 21.06.2019 rozbic to na metody prywatne 
                    sendMessage(socket,"mam");
                    flaga=receiveMessage(socket);
                }

                while  (true);

            }
            catch (IOException ioex) {
                System.out.println("IOException in server running!");
            }
            finally {
                System.out.println("client fucked up something");
            }
        }


    }

}


// TODO: 16.06.2019 drugi klient

// TODO: 21.06.2019 semafor na csv
/*
C. Serwer:
+ 5 folderow, ktore symulujÄ 5 serwerow lub 5 dyskow
+- Klient wysyla np. 10 plikow, wiec serwer uruchamia iles watkow na ktorych rownolegle kopiuje pliki do tych dyskow (folderow)
+- Wymagany jest kontroler, ktory tak rozlozy ruch, ze do kazdego z dyskow (folderow) jednoczesnie jest kopiowana taka sama liczba plikow
+- Jezeli podlaczy sie drugi klient, ktory zacznie wysylac pliki, nie moze on czekac az skoncza sie zadania pierwszego klienta. Lista zadan na serwerze musi ulec reorganizacji, tak aby obydwaj klienci mieli wrazenie natychmiastowej obslugi (zaproponuj stosowny algorytm)
+- Na kazdym dysku serwera znajduje sie plik tekstowy (np. csv), w ktorym jest opisana zawartosc danego dysku i kto jest jego wlascicielem. Zauwaz, ze plik bedzie uaktualniany przez wiele watkow. Rozwiaz ten problem.
- W celu wizualizacji symulacji na niewielkiej liczbie uzytkownikow, czas kopiowania ma byc sztucznie wydluzony poprzez usypianie watku na losowa liczbe sekund.
- Serwer posiada panel graficzny (np. Java FX) pokazujacy zawartosc 5ciu dyskow (serwerow) oraz aktualnie wykonywane operacje.
 */
