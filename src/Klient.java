import projektPaczkKlient.Sender;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class Klient {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;

    private Socket socket;

    /**
     * Creates a WatchService and registers the given directory
     */
    Klient(Path dir) throws IOException {
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<WatchKey, Path>();

        walkAndRegisterDirectories(dir);
    }

    /**
     * Register the given directory with the WatchService; This function will be called by FileVisitor
     */
    private void registerDirectory(Path dir) throws IOException
    {
        WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * Register the given directory, and all its sub-directories, with the WatchService.
     */
    private void walkAndRegisterDirectories(final Path start) throws IOException {
        // register directory and sub-directories
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                registerDirectory(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents(String username,Socket socket) {
        for (;;) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                @SuppressWarnings("rawtypes")
                WatchEvent.Kind kind = event.kind();

                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked")
                Path name = ((WatchEvent<Path>)event).context();
                Path child = dir.resolve(name);

                // print out event
                //System.out.format("%s: %s\n", event.kind().name(), child);
                if(kind == ENTRY_CREATE || kind == ENTRY_MODIFY){
                    System.out.printf("what kind? %s\n", kind.name());
                    new Sender(socket,username,child.toString()).run();
                }
                // if directory is created, and watching recursively, then register it and its sub-directories
                if (kind == ENTRY_CREATE) {
                    try {
                        if (Files.isDirectory(child)) {
                            walkAndRegisterDirectories(child);
                        }
                    } catch (IOException x) {
                        // do something useful
                    }
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }

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
        Path dir = Paths.get(filepath);
        new Klient(dir).processEvents(username,socket);
        // TODO: 16.06.2019  odpytywanie o nowe pliki 
    }
}