import org.junit.jupiter.api.Test;


import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class KlientTest {

    @Test
    void Klient_test_constructor() {
        try {
            Serwer serwer = new Serwer();
            try {
                new Thread() {
                    @Override
                    public void run() {
                        String[] args = new String[0];
                        serwer.main(args);
                    }
                }.start();
                Thread.sleep(300);
                serwer.window.jFrame.setVisible(false);
            } catch (InterruptedException intex) {
                intex.printStackTrace();
            }
            Klient klient = new Klient("test", "C:\\Users\\kowal\\Desktop\\projek\\test");
            assertEquals("test", klient.username);
            serwer.exitApplication();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void run() {
        try {
            Serwer serwer = new Serwer();
            try {
                new Thread() {
                    @Override
                    public void run() {
                        String[] args = new String[0];
                        serwer.main(args);
                    }
                }.start();
                Thread.sleep(300);
                serwer.window.jFrame.setVisible(false);
            } catch (InterruptedException intex) {
                intex.printStackTrace();
            }
            Klient klient = new Klient("test", "C:\\Users\\kowal\\Desktop\\projek\\test");
            Thread watek = new Thread(klient);
            watek.start();

            watek.sleep(5000);
            ///wrzucic plik asd.txt do folderu
            File file = new File("C:\\Users\\kowal\\Desktop\\projek\\test\\asd.txt");
            file.createNewFile();
            FileWriter pisacz = new FileWriter(file);
            pisacz.write("ala ma kota a kot ma ale");
            pisacz.close();

            TimeUnit.SECONDS.sleep(1);
            klient.localDirectroyWatcher.checkpoint.contains(file);

            ////////////////koniec servera
            serwer.exitApplication();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}