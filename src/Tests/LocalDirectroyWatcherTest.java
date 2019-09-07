package Tests;

import org.junit.jupiter.api.Test;
import projektPaczkKlient.LocalDirectroyWatcher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class LocalDirectroyWatcherTest {

    @Test
    void startup() {
        LocalDirectroyWatcher temp = new LocalDirectroyWatcher("C:\\Users\\kowal\\Desktop\\projek\\test");
        File file = new File("C:\\Users\\kowal\\Desktop\\projek\\test\\test.txt");

        try {
            file.createNewFile();
            temp.startup();
            assertTrue(!temp.checkpoint.isEmpty());
            file.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void check_For_New() {
        LocalDirectroyWatcher temp = new LocalDirectroyWatcher("C:\\Users\\kowal\\Desktop\\projek\\test");
        File file = new File(temp.dirpath+"\\test.txt");

        try {
            file.createNewFile();
            temp.startup();
            File file2 = new File(temp.dirpath+"\\test2.txt");
            file2.createNewFile();
            temp.check_For_New();
            assertTrue(temp.checkpoint.size()>1);
            file.delete();
            file2.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getFileNames() {
        LocalDirectroyWatcher temp = new LocalDirectroyWatcher("C:\\Users\\kowal\\Desktop\\projek\\test");
        File file = new File(temp.dirpath+"\\test.txt");
        File file2 = new File(temp.dirpath+"\\test2.txt");
        try {
            file.createNewFile();
            file2.createNewFile();
            temp.startup();
            String tempstring = ";"+file.getName()+";"+file2.getName();
            assertEquals(tempstring,temp.getFileNames());

            file.delete();
            file2.delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}