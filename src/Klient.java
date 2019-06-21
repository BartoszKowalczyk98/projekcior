import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import projektPaczkKlient.LocalDirectroyWatcher;
import projektPaczkKlient.Receiver;
import projektPaczkKlient.Sender;

import static projektPaczkKlient.Messenger.*;

import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public class Klient implements  Runnable{
    String username;
    String filepath;
    Socket socket;
    Semaphore semaphore = new Semaphore(1);
    LocalDirectroyWatcher localDirectroyWatcher;

    public Klient(String username, String filepath) throws IOException {
        this.username = username;
        this.filepath = filepath;
        this.socket = new Socket("127.0.0.1", 59898);
        localDirectroyWatcher = new LocalDirectroyWatcher(filepath);
    }



    public static void main(String[] args) throws IOException {
        if(args.length!=2){
            System.out.println("Not enough arguments!");
            return;
        }
        String arg1 = args[0];
        String arg2 = args[1];
        Klient client = new Klient(arg1,arg2);


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
    }
    private void sendcurrentfilelist() throws IOException{
        sendMessage(socket,"anythingnew");
        sendMessage(socket,localDirectroyWatcher.getFileNames());

        int howmany = Integer.valueOf(receiveMessage(socket));
        if(howmany>0) {
            ExecutorService pool = Executors.newFixedThreadPool(howmany);
            for (int i = 0; i < howmany; i++){
                pool.execute(new Receiver(socket,username,filepath,semaphore));
            }

        }
        if(receiveMessage(socket).equals("nomore")){
            return;
        }
        else
            throw new IOException();
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