import projektPaczkKlient.*;
import projektPaczkKlient.Window;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static projektPaczkKlient.CSVFileHandler.appendingToCSVFile;
import static projektPaczkKlient.CSVFileHandler.createCSVFile;

/**
 * main class that is used to start server
 */
public class Serwer {
    /**map that holds info about currently connected cleints */
    protected static Map<String, Socket> mapOfClients;
    /**list of objects that hold information about directories that simulate server discs */
    protected static List<DirectroyWithSize> disc = new ArrayList<>();
    /**semaphore for accsess to csv file */
    private static final Semaphore semaphoreForCSV = new Semaphore(1);
    /**list of tasks that require something to be done with them */
    protected static List<TaskToBeDone> taskToBeDoneList;
    /**class that creates application window  */
    protected static Window window;
    /**list that hold text areas */
    private static ArrayList<JTextArea> lists=new ArrayList<>();

    /**
     * main method that initializes window creates directories starts controller and listens for clients
     * @param args TBA
     */
    public static void main(String[] args) {
        //creating 5 directories
        String dirpath = "C:\\Users\\kowal\\Desktop\\projek\\Dysk";
        try {
            for (int i = 1; i < 6; i++) {
                File dir = new File(dirpath + i);
                if (!dir.mkdir()) {
                    System.out.println("directory already exists!");
                }
                createCSVFile(dirpath + i + "\\info.csv");
            }
            for (int i = 1; i < 6; i++) {
                disc.add(new DirectroyWithSize(dirpath + i));
            }
        } catch (IOException ioex) {
            System.out.println("problem with cvsfilecreating");
            return;
        }

        window = new Window("Server");
        for (int i=0;i<5;i++){
            JTextArea jTextArea = new JTextArea(40,20);
            jTextArea.setEditable(false);
            lists.add(jTextArea);
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx=i;
            gridBagConstraints.gridy=0;
            window.jPanel.add(jTextArea,gridBagConstraints);
        }
        window.jLabel.setText("Waiting");
        window.jFrame.pack();
        window.jFrame.setVisible(true);

        for(int i=0;i<5;i++){
            lists.get(i).append("Disc" +(i+1)+"\n");
            for(String s : disc.get(i).getFileNames()){
                lists.get(i).append(s+"\n");
            }
        }

        try (ServerSocket listener = new ServerSocket(59898)) {
            mapOfClients = new TreeMap<>();
            taskToBeDoneList = new ArrayList<>();
            ExecutorService pool = Executors.newFixedThreadPool(10);
            Controller controller = new Controller(pool);
            pool.execute(controller);
            while (!window.finished) {
                pool.execute(new ClientHandler(listener.accept()));
            }
        } catch (IOException ioex) {
            System.out.println("problem with setting up server");
        }

    }

    /**
     * method that is used for chcecking and closing server application
     */
    protected static void exitApplication(){
        synchronized (mapOfClients){
            if(window.finished){
                synchronized (mapOfClients){
                    if(mapOfClients.isEmpty()){
                        window.jFrame.setVisible(false);
                        window.jFrame.dispose();
                        System.exit(0);
                    }
                }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * helper class that is only used to hold information
     */
    private static class TaskToBeDone {

        Receiver receiver;
        String forwho;

        /**
         * constructor for class that assigns receiver.from to field forwho
         * @param receiver
         */
        TaskToBeDone(Receiver receiver) {
            this.receiver = receiver;
            this.forwho = receiver.from;
        }

        /**
         * constructor for class that assigns given forwho to its forwho field
         * @param receiver
         * @param forwho who is supposed to be new owner of the file
         */
        TaskToBeDone(Receiver receiver, String forwho) {

            this.receiver = receiver;
            this.forwho = forwho;
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * controller class that operates saving files and checks wheater to close application
     */
    private static class Controller implements Runnable {
        /**pool of threads */
        ExecutorService executorService;
        /**simple iterator through discs */
        int discIterator;
        /**simple iterator thorugh list clients */
        int userIterator;
        /**field to hold name of last operated user */
        String lastuser;
        /**
         * @param executorService pool of threads
         */
        Controller(ExecutorService executorService) {
            this.executorService = executorService;


            for(DirectroyWithSize d : disc){
                d.updateSize();
            }
            Collections.sort(disc);
            lastuser = "none";
            userIterator = 0;
            discIterator=0;
        }

        /**
         * main method that checks if there is something to be done
         * if there is then proceeds to save file form list and append its name
         * to .csv file
         */
        @Override
        public void run() {

            while (true) {
                if (discIterator == 5)
                    discIterator = 0;
                boolean hasSomethingBeenDone = false;
                TaskToBeDone temp;
                String forwho = "";
                String filename = "";
                synchronized (taskToBeDoneList) {
                    int howManyTasks = taskToBeDoneList.size();
                    if (howManyTasks > 0) {
                        window.jLabel.setText("copying files");
                        while (lastuser.equals(taskToBeDoneList.get(userIterator).receiver.from) && userIterator < howManyTasks - 1) {
                            userIterator++;
                        }
                        temp = taskToBeDoneList.get(userIterator);
                        taskToBeDoneList.remove(userIterator);
                        forwho = temp.forwho;
                        filename = temp.receiver.fileWithUsername.filename;
                        lastuser = temp.receiver.from;
                        String helper = disc.get(discIterator).dirpath;
                        int whichDiscIsIt = Integer.valueOf( helper.substring(helper.length()-1));//potrzebuje ostatnią cyferke z calego dirpatha z aktualnego disc w uzyciu
                        lists.get(whichDiscIsIt-1).append(filename+"\n");
                        try {
                            TimeUnit.MILLISECONDS.sleep(750);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        executorService.execute(new Saver(temp.receiver, disc.get(discIterator).dirpath+ "\\"));

                        hasSomethingBeenDone = true;
                        userIterator = 0;

                    }
                }
                if (hasSomethingBeenDone) {
                    while (!semaphoreForCSV.tryAcquire()) ;
                    try {
                        appendingToCSVFile(forwho, filename, disc.get(discIterator).dirpath+ "\\" + "info.csv");

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        semaphoreForCSV.release();
                        discIterator++;
                    }

                }
                window.jLabel.setText("waiting");
                exitApplication();

            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * class that communicates with client and adds tasks for controller
     */
    private static class ClientHandler implements Runnable {
        /**socket through which communication happens */
        private Socket socket;
        /**stream to read communicates and objects */
        ObjectInputStream objectInputStream;
        /**stream to write communicates and objects */
        ObjectOutputStream objectOutputStream;

        /**name of connected user */
        private String username;
        /**semaphore for objectOutputStream */
        final Semaphore semaphore;

        /**
         * constructor for class
         * @param socket socket through which client is connected
         */
        ClientHandler(Socket socket) {
            this.socket = socket;
            try {
                this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
                this.objectInputStream = new ObjectInputStream(socket.getInputStream());
                objectOutputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
            this.semaphore = new Semaphore(1);
            this.username = "unknown";


        }

        /**
         * main communication function it works until connection is kept alive
         */
        @Override
        public void run() {
            System.out.println("connected to " + socket);
            try {
                username = objectInputStream.readUTF();
                System.out.println(username);

                synchronized (mapOfClients) {  ////////////////////////////////// moja proba synchronizacji mapy klientow
                    if (mapOfClients.containsKey(username)) {

                        objectOutputStream.writeUTF("nametaken");
                        objectOutputStream.flush();
                        socket.close();

                        return;
                    }
                }
                System.out.println("username accepted");
                objectOutputStream.writeUTF("welcome");
                objectOutputStream.flush();

                synchronized (mapOfClients) {   ////////////////////////////////// moja proba synchronizacji mapy klientow
                    mapOfClients.put(username, socket);
                }
                String whatClientSaid;
                do {
                    whatClientSaid = objectInputStream.readUTF();

                    if (whatClientSaid.equals("clientlist")) {
                        sendclientlist();
                    } else if (whatClientSaid.equals("sendto")) {
                        receivefor();
                    } else if (whatClientSaid.equals("anythingnew")) {
                        sendEverything();
                    } else {
                        int howmany = Integer.parseInt(whatClientSaid);
                        givemefiles(howmany);
                    }
                } while (!whatClientSaid.equals("kaniet"));//dopóki nie dostanie kaniet to ma czekac na wiadomosc
                socket.close();
            }catch (EOFException eof){
                System.out.println("client disconected");
            }
            catch (IOException ioex) {
                ioex.printStackTrace();
            } catch (InterruptedException intex) {
                intex.printStackTrace();
            }
            finally {
                synchronized (mapOfClients){
                    mapOfClients.remove(username);
                }
            }

        }

        /**
         * method that sends actual list of clients to client that asked
         * @throws IOException
         */
        private void sendclientlist() throws IOException {
            String clientlist = new String();
            synchronized (mapOfClients) { ////////////////////////////////// moja proba synchronizacji mapy klientow
                for (String key : mapOfClients.keySet()) {
                    clientlist = clientlist + key + ',';
                }
            }
            objectOutputStream.writeUTF(clientlist);
            objectOutputStream.flush();
        }

        /**
         * method that reads object that is destined for someone else than @username
         * @throws IOException
         */
        private void receivefor() throws IOException {
            String forwho = objectInputStream.readUTF();

            synchronized (mapOfClients) { ////////////////////////////////// moja proba synchronizacji mapy klientow
                if (!mapOfClients.containsKey(forwho)) {

                    objectOutputStream.writeUTF("nosuchclient");
                    objectOutputStream.flush();
                    return;
                }
            }

            objectOutputStream.writeUTF("gimme");
            objectOutputStream.flush();

            Receiver receiver = new Receiver(objectInputStream, username);
            Thread thread = new Thread(receiver);
            thread.start();

            while (thread.isAlive()) ;
            synchronized (taskToBeDoneList) {
                taskToBeDoneList.add(new TaskToBeDone(receiver, forwho));
            }

            objectOutputStream.writeUTF("received");
            objectOutputStream.flush();

        }

        /**
         * method that reads actual list of files from client and sends everything hr does not have
         * but it is on server
         * @throws IOException
         * @throws InterruptedException
         */
        private void sendEverything() throws IOException, InterruptedException {

            String listoffilenames = objectInputStream.readUTF();
            String[] arroffilenames = listoffilenames.split(";");
            ArrayList<String> currentlyOwnedByUser = new ArrayList<>();
            for (String i : arroffilenames) {
                currentlyOwnedByUser.add(i);
            }

            List<String> whatDoYouOwnDisc;
            ArrayList<String> toBeSent = new ArrayList<>();
            for (int disciterator = 0; disciterator < 5; disciterator++) {
                while (!semaphoreForCSV.tryAcquire()) ;

                whatDoYouOwnDisc = CSVFileHandler.searchingForFiles(disc.get(disciterator).dirpath + "\\info.csv", username);
                semaphoreForCSV.release();
                for (String s : whatDoYouOwnDisc) {
                    if (!currentlyOwnedByUser.contains(s)) {
                        toBeSent.add(disc.get(disciterator).dirpath + "\\" + s);
                    }
                }

            }
            int howmany = toBeSent.size();


            objectOutputStream.writeUTF(String.valueOf(howmany));
            objectOutputStream.flush();
            if (howmany > 0) {
                ExecutorService pool = Executors.newFixedThreadPool(howmany);
                for (int i = 0; i < howmany; i++) {
                    pool.execute(new Sender(objectOutputStream, "server", toBeSent.get(i), semaphore));
                }
                pool.shutdown();
                while (!pool.isTerminated()) ;
            }

            objectOutputStream.writeUTF("nomore");
            objectOutputStream.flush();

        }

        /**
         * method that reads @howmany files form client
         * @param howmany
         * @throws IOException
         * @throws InterruptedException
         */
        private void givemefiles(int howmany) throws IOException, InterruptedException {
            if (howmany == 0)
                return;
            //dodawanie do kolejki obiektow receivera(zeby zapisywal pool.execute->kolejka.add
            ExecutorService pool = Executors.newFixedThreadPool(howmany);
            List<Receiver> receiverList = new ArrayList<>();
            for (int i = 0; i < howmany; i++) {
                receiverList.add(new Receiver(objectInputStream, username));
            }

            for (int j = 0; j < howmany; j++) {
                pool.execute(receiverList.get(j));
                Thread.sleep(500);
            }
            pool.shutdown();
            if (!pool.awaitTermination(60, TimeUnit.SECONDS)) ;
            synchronized (taskToBeDoneList) {
                for (Receiver r : receiverList) {
                    taskToBeDoneList.add(new TaskToBeDone(r));
                }
            }
            objectOutputStream.writeUTF("received");
            objectOutputStream.flush();

        }

    }

}

