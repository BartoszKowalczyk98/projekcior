import projektPaczkKlient.*;
import projektPaczkKlient.Window;

import javax.swing.*;

import static java.lang.Thread.sleep;


import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * main class that is used to start and operate client
 */
public class Klient implements Runnable {
    /**
     * name of user
     */
    protected String username;
    /**
     * path to local directory
     */
    String filepath;
    /**
     * socket through which connection is established
     */
    Socket socket;
    /**
     * stream that is used to read objects from server
     */
    ObjectInputStream objectInputStream;
    /**
     * stream that is used to write objects for server
     */
    ObjectOutputStream objectOutputStream;
    /**
     * semaphore for objectOutputStream
     */
    Semaphore semaphore = new Semaphore(1);
    /**
     * name of the client that is supposed to receive file
     */
    String forwho;
    /**
     * path to the file that is destined for @forwho
     */
    String whichFile;
    /**
     * object of class LocalDirectoryWatcher that supervises @filepath
     */
    LocalDirectroyWatcher localDirectroyWatcher;
    /**
     * object that creates window for application
     */
    Window window;
    /**
     * text field for displaying acutal files
     */
    JTextArea files = new JTextArea(20, 40);
    /**
     * boolean that says wheather button for list of clients was pressed or not
     */
    boolean buttonPressed = false;


    /**
     * main constructor for class
     *
     * @param username name of user 1st parameter of program
     * @param filepath path to local directory 2nd parameter of program
     * @throws IOException TBA
     */
    public Klient(String username, String filepath) throws IOException {
        this.username = username;
        this.filepath = filepath;
        try {
            this.socket = new Socket("127.0.0.1", 59898);
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.flush();

        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        localDirectroyWatcher = new LocalDirectroyWatcher(filepath);
        localDirectroyWatcher.startup();
        window = new Window(username);
        window.jPanel.add(files);
        window.jLabel.setText("Setting up");
        files.setEditable(false);
        JButton jButton = new JButton("List of clients");
        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buttonPressed = true;
            }
        });
        window.jFrame.add(jButton, BorderLayout.SOUTH);
        window.jFrame.pack();
        forwho = "";
        whichFile = "";
        ////////tworzenie okna do interfejsu graficznego
    }

    /**
     * main method used to initialize client and start run method and check if 2 arguments were given
     *
     * @param args 1st arg is username 2nd is path to local directory
     * @throws IOException TBA
     */
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Not enough arguments!");
            return;
        }
        Klient client = new Klient(args[0], args[1]);

        client.run();

    }

    /**
     * method that is used to establish and keep communication with server
     */
    @Override
    public void run() {
        window.jFrame.setVisible(true);
        try {


            objectOutputStream.writeUTF(username);
            objectOutputStream.flush();

            if (!objectInputStream.readUTF().equals("welcome")) {
                window.jLabel.setText("Username taken or one of restricted words!");

                try {
                    TimeUnit.SECONDS.sleep(2);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException intex) {
                    intex.printStackTrace();
                }
                return;
            }
            localDirectroyWatcher.startup();
            for (File f : localDirectroyWatcher.checkpoint) {
                files.append(f.getName() + "\n");
            }

            try {
                while (!window.finished) {//z opcja zmiany na hitbutton to kaniet
                    window.jLabel.setText("waiting");
                    checkIfServerHasNewFiles();
                    TimeUnit.MILLISECONDS.sleep(500);//sleepy tylko po to zeby bylo widac zmiane labela
                    checkForNewAndSendThem();//glowny watcher i wysylacz
                    TimeUnit.MILLISECONDS.sleep(500);
                    if (buttonPressed) {
                        window.jLabel.setText("choosing client and file");
                        buttonPressed = false;

                        ArrayList<String> listOfClients = getOtherClients();
                        if (listOfClients.size() > 1) {


                            Window showMeClients = new Window("List of avaliable clients");
                            showMeClients.jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                            int position = 0;
                            for (String s : listOfClients) {
                                JButton cbutton = new JButton(s);
                                cbutton.addActionListener(new ActionListener() {
                                    @Override
                                    public void actionPerformed(ActionEvent e) {
                                        forwho = cbutton.getText();
                                        showMeClients.jFrame.setVisible(false);
                                        showMeClients.jFrame.dispose();
                                    }
                                });
                                GridBagConstraints gridBagConstraints = new GridBagConstraints();
                                gridBagConstraints.gridx = position++;
                                gridBagConstraints.gridy = 0;
                                showMeClients.jPanel.add(cbutton, gridBagConstraints);
                            }

                            showMeClients.jFrame.pack();
                            showMeClients.jFrame.setVisible(true);
                            while (forwho.equals("")) {
                                TimeUnit.MILLISECONDS.sleep(500);
                            }

                            Window showMeFiles = new Window("Which file");
                            showMeFiles.jFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                            position = 0;
                            if (localDirectroyWatcher.checkpoint.size() > 0) {

                                for (File f : localDirectroyWatcher.checkpoint) {
                                    JButton fButton = new JButton(f.getName());
                                    fButton.addActionListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            whichFile = f.getPath();
                                            showMeFiles.jFrame.setVisible(false);
                                            showMeFiles.jFrame.dispose();
                                        }
                                    });
                                    GridBagConstraints gridBagConstraints = new GridBagConstraints();
                                    gridBagConstraints.gridx = position++;
                                    gridBagConstraints.gridy = 0;
                                    showMeFiles.jPanel.add(fButton, gridBagConstraints);
                                }
                                showMeFiles.jFrame.pack();
                                showMeFiles.jFrame.setVisible(true);
                                while (whichFile.equals("")) {
                                    TimeUnit.MILLISECONDS.sleep(500);
                                }


                                sendToUser(forwho, whichFile);

                                forwho = "";
                                whichFile = "";
                            } else {
                                window.jLabel.setText("you have no files in your directory to send!");
                                TimeUnit.SECONDS.sleep(2);

                            }
                        } else {
                            window.jLabel.setText("You are the only client on the server!");
                            TimeUnit.SECONDS.sleep(2);
                        }
                    }
                    TimeUnit.MILLISECONDS.sleep(300);

                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException intex) {
                intex.printStackTrace();
            } finally {
                objectOutputStream.writeUTF("kaniet");
                window.jLabel.setText("Clearing up mess and exiting application");
                try {
                    TimeUnit.SECONDS.sleep(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                window.jFrame.setVisible(false);
                window.jFrame.dispose();
                socket.close();
                System.exit(0);
            }
        } catch (
                IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * method that sends list of files to server and checks if there is anything new for client there
     *
     * @throws IOException
     */
    private void checkIfServerHasNewFiles() throws IOException {
        objectOutputStream.writeUTF("anythingnew");
        objectOutputStream.flush();
        objectOutputStream.writeUTF(localDirectroyWatcher.getFileNames());
        objectOutputStream.flush();

        List<Receiver> receiverList = new ArrayList<>();

        int howmany = Integer.valueOf(objectInputStream.readUTF());

        if (howmany > 0) {
            window.jLabel.setText("Receiving files from server");
            ExecutorService pool = Executors.newFixedThreadPool(howmany);

            for (int i = 0; i < howmany; i++) {
                receiverList.add(new Receiver(objectInputStream, username));
            }

            for (int j = 0; j < howmany; j++) {
                pool.execute(receiverList.get(j));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }

        if (objectInputStream.readUTF().equals("nomore")) {

            if (howmany > 0) {
                ExecutorService poolForSaver = Executors.newFixedThreadPool(howmany);
                for (int j = 0; j < howmany; j++) {
                    files.append(receiverList.get(j).fileWithUsername.filename + "\n");
                    poolForSaver.execute(new Saver(receiverList.get(j), filepath + "\\"));

                }
                poolForSaver.shutdown();
                while (!poolForSaver.isTerminated()) ;
                localDirectroyWatcher.check_For_New();
                localDirectroyWatcher.toBeSent.clear();
            }
            return;
        } else
            throw new IOException();
    }

    /**
     * this method checks if there are new files in local directory and if there are any it sends them to server
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void checkForNewAndSendThem() throws IOException, InterruptedException {

        localDirectroyWatcher.check_For_New();
        if (localDirectroyWatcher.toBeSent.isEmpty())
            return;
        int howmany = localDirectroyWatcher.toBeSent.size();
        window.jLabel.setText("Sending new files to server");
        objectOutputStream.writeUTF(String.valueOf(howmany));
        objectOutputStream.flush();
        ExecutorService pool = Executors.newFixedThreadPool(howmany);
        for (int i = 0; i < howmany; i++) {
            files.append(localDirectroyWatcher.toBeSent.get(i).getName() + "\n");
            pool.execute(new Sender(objectOutputStream, username, localDirectroyWatcher.toBeSent.get(i).getPath(), semaphore));
        }
        sleep(100);

        localDirectroyWatcher.toBeSent.clear();//czyszczenie kolejki do wyslania
        while (!(objectInputStream.available() > 6)) ;
        if (!objectInputStream.readUTF().equals("received"))
            throw new IOException();
    }

    /**
     * asks server for list of clients connected to it
     *
     * @return
     * @throws IOException
     */
    private ArrayList<String> getOtherClients() throws IOException {
        objectOutputStream.writeUTF("clientlist");
        objectOutputStream.flush();
        String result = objectInputStream.readUTF();
        result = result.substring(0, result.length() - 1);
        String[] temp = result.split(",");
        ArrayList<String> arrayListResult = new ArrayList<>();
        for (String s : temp) {
            arrayListResult.add(s);
        }
        return arrayListResult;

    }

    /**
     * method that is used to send files to someone else
     *
     * @param forWho     username of who is supposed to receive new file
     * @param filetosend path to file that is supposed to be transmitted
     * @throws IOException
     */
    private void sendToUser(String forWho, String filetosend) throws IOException {

        objectOutputStream.writeUTF("sendto");
        objectOutputStream.flush();
        objectOutputStream.writeUTF(forWho);
        objectOutputStream.flush();

        String whatDoesServersay = objectInputStream.readUTF();
        if (whatDoesServersay.equals("nosuchclient")) return;
        else if (whatDoesServersay.equals("gimme")) {
            Thread thread = new Thread(new Sender(objectOutputStream, username, filetosend, semaphore));
            thread.start();
            while (thread.isAlive()) ;
        }

        objectInputStream.readUTF();

    }
}