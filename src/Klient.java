import projektPaczkKlient.LocalDirectroyWatcher;
import projektPaczkKlient.Receiver;
import projektPaczkKlient.Sender;

import static projektPaczkKlient.Messenger.*;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Klient {


    public static void main(String[] args) throws IOException {
        if(args.length!=2){
            System.out.println("Not enough arguments!");
            return;
        }
        String username = args[0];
        String filepath = args[1];
        Socket socket;
        try{
        socket = new Socket("127.0.0.1", 59898);}
        finally {
            System.out.println("connected to sever");
        }
        //starting up the client and receivning all filles
        sendMessage(socket,username);//who am i? now server knows :>
        String commander;
        while((commander=receiveMessage(socket)).equals("more")){
            new Receiver(socket, username, filepath).run();
            //komunikat odbieram
        }
        if(commander.equals("nomore")){
            System.out.println("for now i received all them sweet files");
            //komunikat odebrałem czy ki huj
        }

        //startowanie obserwatora folderu czytaj robi snapshot tego co sie aktualnie tam znajduje
        LocalDirectroyWatcher directroyWatcher = new LocalDirectroyWatcher(filepath);
        try {
            directroyWatcher.startup();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //petla komunikacyjna z serverem
        while(!((commander=receiveMessage(socket)).equals("error"))){
            directroyWatcher.check_For_New();
            if(commander.equals("catch")){
                //odbierz plik
                new Receiver(socket,username,filepath).run();

            }
            else if(commander.equals("ready")){
                //wyslij pliki musi odpalic x watkow do wysylania bo potrzebuje wyczyscic kolejke toBeSent
                if(!directroyWatcher.toBeSent.isEmpty()){
                    sendMessage(socket,"nothing");
                }
                else {
                    sendMessage(socket,String.valueOf(directroyWatcher.toBeSent.size()));
                    ExecutorService pool = Executors.newFixedThreadPool(directroyWatcher.toBeSent.size());
                    for(int i =0;i<directroyWatcher.toBeSent.size();i++){
                        pool.execute(new Sender(socket,username,directroyWatcher.toBeSent.get(i).getPath()));
                    }
                }
            }

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