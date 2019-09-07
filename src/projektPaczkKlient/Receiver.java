package projektPaczkKlient;

import java.io.*;
import java.util.Random;

/**
 * thread class that is used for receiving an object from stream
 */
public class Receiver implements Runnable {
    /**stream from which read an object     */
    public ObjectInputStream ois;
    /**who is the owner of the task     */
    public String from;
    /**object that holds information about file that is transmitted     */
    public FileWithUsername fileWithUsername;

    /**
     * constructor for class
     * @param objectInputStream stream
     * @param from who started this task
     */
    public Receiver(ObjectInputStream objectInputStream, String from) {
        this.ois = objectInputStream;
        this.from = from;

    }


    /**
     * method reads object from stream and assigns it to fileWithUsername
     */
    @Override
    public void run() {
        try {
            fileWithUsername = (FileWithUsername) ois.readObject();
            Thread.sleep(new Random().nextInt(100));
        } catch (IOException ioex) {
            ioex.printStackTrace();
        } catch (ClassNotFoundException cnfex) {
            cnfex.printStackTrace();
        }catch (InterruptedException intex){
            intex.printStackTrace();
        }
    }
}
