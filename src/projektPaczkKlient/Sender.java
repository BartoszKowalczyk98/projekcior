package projektPaczkKlient;

import java.io.*;

import java.util.Random;
import java.util.concurrent.Semaphore;

/**
 * class that is used to write objects through stream
 */
public class Sender implements Runnable {
    private ObjectOutputStream oos;
    private String from;
    private String filepath;
    private Semaphore semaphore;

    /**
     * constructor of class sender
     * @param objectOutputStream stream for writing objects
     * @param from who initiated the task
     * @param filepath path to the file that is about to be sent
     * @param semaphore semaphore for stream
     */
    public Sender(ObjectOutputStream objectOutputStream, String from, String filepath, Semaphore semaphore) {
        this.oos = objectOutputStream;
        this.from = from;
        this.filepath = filepath;
        this.semaphore = semaphore;
        //this.run();
    }

    /**
     * method that is used to convert file to a bytearray
     * @param filepath path to file
     * @return bytearray made form file
     * @throws IOException
     */
    private byte[] fileToBytearr(String filepath) throws IOException {
        File file = new File(filepath);
        byte[] bytesarray = new byte[(int) file.length()];

        FileInputStream fis = new FileInputStream(file);
        fis.read(bytesarray);
        fis.close();
        return bytesarray;
    }

    /**
     * main method of this class
     * it acquires semaphore for objectOutputStream and proceeds to convert file
     * from path to bytearray and later writes object of class FileWithUsername through stream
     */
    @Override
    public void run() {
        try {
            semaphore.acquire();
            File f = new File(this.filepath);

            if ((!f.exists()) || f.isDirectory()) return;// if file does not exist or is a directory
            byte[] bytesarray = fileToBytearr(this.filepath);
            String filename = f.getName();


            oos.writeObject(new FileWithUsername(this.from, bytesarray, filename));
            oos.flush();

            Thread.sleep(new Random().nextInt(100));
        } catch (InterruptedException intex) {
            intex.printStackTrace();
        } catch (IOException ioex) {
            ioex.printStackTrace();

        } finally {
            semaphore.release();
        }
    }
}
