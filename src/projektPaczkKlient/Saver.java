package projektPaczkKlient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

/**
 * thread class that is used to save given parameters as files
 */
public class Saver implements Runnable {
    /**object of Receiver class that holds information to be saved     */
    public Receiver receiver;
    /**path where to save that iformation     */
    public String dirpath;

    /**
     * constructor for class Saver
     * @param receiver object of Receiver class that holds information to be saved
     * @param dirpath path where to save that iformation
     */
    public Saver(Receiver receiver, String dirpath) {
        this.receiver = receiver;
        this.dirpath = dirpath;
    }

    /**
     * main method of class that creates file in @dirpath with filename recovered
     * from receiver and than proceeds to fill that empty file with data held in receiver
     */
    @Override
    public void run() {
        try {
            File file = new File(dirpath + receiver.fileWithUsername.filename);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            Thread.sleep(new Random().nextInt(500));
            //wpisywanie do pliku
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(receiver.fileWithUsername.bytesarray);

            //zamkniecie tego co nie potrzebne
            fileOutputStream.close();
        } catch (FileNotFoundException fifex) {
            fifex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException intex) {
            intex.printStackTrace();
        }

    }
}
