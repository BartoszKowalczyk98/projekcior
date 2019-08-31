import projektPaczkKlient.ClientNotFoundException;
import projektPaczkKlient.LocalDirectroyWatcher;
import projektPaczkKlient.Receiver;
import projektPaczkKlient.Sender;

import static java.lang.Thread.sleep;
import static projektPaczkKlient.Messenger.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Klient implements  Runnable{
    String username;
    String filepath;
    Socket socket;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    Semaphore semaphore;

    LocalDirectroyWatcher localDirectroyWatcher;

    public Klient(String username, String filepath) throws IOException {
        this.username = username;
        this.filepath = filepath;
        try {
            this.socket = new Socket("127.0.0.1", 59898);
            this.objectInputStream= new ObjectInputStream( socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
        }
        catch (IOException ioex){
            ioex.printStackTrace();
        }
        localDirectroyWatcher = new LocalDirectroyWatcher(filepath);
        localDirectroyWatcher.startup();

        ////////tworzenie okna do interfejsu graficznego
    }



    public static void main(String[] args) throws IOException {
        if(args.length!=2){
            System.out.println("Not enough arguments!");
            return;
        }
        Klient client = new Klient(args[0],args[1]);
        client.run();

        //rozpoczynamy watek klienta na dobra sprawe


    }

    @Override
    public void run() {
        //starting up the client and receivning all filles

        sendMessage(socket,username);//who am i? now server knows :>

        if(!receiveMessage(socket).equals("welcome")){
            System.out.println("wrong arguments kretynie");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }


        try {
            sendcurrentfilelist();//pobranie plikow na starcie tych co jeszcze ich nie mam
            while (true) {//z opcja zmiany na hitbutton to kaniet
                TimeUnit.SECONDS.sleep(4);
                checkForNewAndSendThem();//glowny watcher i wysylacz
                /*localDirectroyWatcher.check_For_New();// niech ustawi label na sprawdzam*/
                //System.out.println("przeszlo petle!");
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException intex)
        {
            intex.printStackTrace();
        }
        /*catch (ClientNotFoundException cnfex){
            cnfex.GetWarning();
        }*/
    }
    private void sendcurrentfilelist() throws IOException{
        sendMessage(socket,"anythingnew");
        sendMessage(socket,localDirectroyWatcher.getFileNames());
        //System.out.println(localDirectroyWatcher.getFileNames());
        int howmany = Integer.valueOf(receiveMessage(socket));
        if(howmany>0) {
            ExecutorService pool = Executors.newFixedThreadPool(howmany);
            for (int i = 0; i < howmany; i++){
                pool.execute(new Receiver(socket,username,filepath));
            }
            while(!pool.isTerminated());
        }

        if(receiveMessage(socket).equals("nomore")){
            return;
        }
        else
            throw new IOException();
    }

    private void checkForNewAndSendThem()throws  IOException ,InterruptedException{
        System.out.println("pacze czy nie ma cos nowego");
        localDirectroyWatcher.check_For_New();
        if(localDirectroyWatcher.toBeSent.isEmpty())
            return;
        int howmany=localDirectroyWatcher.toBeSent.size();
        sendMessage(socket,String.valueOf(howmany));
        ExecutorService pool = Executors.newFixedThreadPool(howmany);
        for(int i =0;i<howmany;i++){
            //try aquire mutexa
            pool.execute(new Sender(socket,username,localDirectroyWatcher.toBeSent.get(i).getPath()));
        }
        sleep(100);
        System.out.println("wyslalem");
        localDirectroyWatcher.toBeSent.clear();//czyszczenie kolejki do wyslania

    }

    private String [] getOtherClients(){
        sendMessage(socket,"clientlist");
        return receiveMessage(socket).split(",");
    }

    private void sendToUser(String forWho,String filetosend) throws ClientNotFoundException{
        sendMessage(socket,"sendto");
        sendMessage(socket,forWho);
        String whatDoesServersay = receiveMessage(socket);
        if(whatDoesServersay.equals("nosuchclient")){
            throw new ClientNotFoundException();
        }
        else{
            new Sender(socket,username,filetosend);
        }
        if(receiveMessage(socket).equals("received")){
            System.out.println("git");
        }
    }
}
/*


+ Uruchamiana jest z dwoma parametrami: nazwa uzytkownika i sciezka do lokalnego folderu
+ Kazdy klient ma swoj lokalny folder z plikami
+ Aplikacja po uruchomieniu odpytuje serwer o nowe pliki i je sciaga

- Aplikacja obserwuje lokalny folder i reaguje na zmiany. Jak pojawia sie tam nowe pliki, to wysyla je na serwer
- Jak pojawi sie nowy plik dla danego uzytkownika, to jest on pobierany do lokalnego folderu
- Wysylanie / odbieranie dzieje sie przy wykorzystaniu puli watkow
- Aplikacja kliencka ma interfejs graficzny (np. Java FX) pokazujacy w czasie rzeczywistym czym sie w danej chwili zajmuje klient ("Pobieram...", "Wysylam ...", "Sprawdzam ....") oraz wyswietlajacy liste aktualnych plikow w lokalnym folderze. Panel graficzny ma umozliwic takze udostepnienie danego pliku innemu uzytkownikowi. Liste dostepnych uzytkownikow nalezy pobrac z serwera.
- jak sie wylaczy apke kliencka to ma wyslac komunikat do servera ze ma sie wylaczyc

 */