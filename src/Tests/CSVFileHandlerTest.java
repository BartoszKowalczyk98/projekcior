package Tests;

import org.junit.jupiter.api.Test;
import projektPaczkKlient.CSVFileHandler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class CSVFileHandlerTest {

    @Test
    void createCSVFile() {
        try {
            assertTrue(CSVFileHandler.createCSVFile("C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv"));
            assertTrue(!CSVFileHandler.createCSVFile("C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv"));
            new File("C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv").delete();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void appendingToCSVFile() {
        try {
            CSVFileHandler.createCSVFile("C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv");
            assertTrue(CSVFileHandler.appendingToCSVFile("test","testfile","C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv"));
            assertTrue(!CSVFileHandler.appendingToCSVFile("test","testfile","C:\\Users\\kowal\\Desktop\\projek\\test"));
            new File("C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv").delete();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void searchingForFiles() {
        try {
            CSVFileHandler.createCSVFile("C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv");
            CSVFileHandler.appendingToCSVFile("test","testfile","C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv");
            CSVFileHandler.appendingToCSVFile("test1","testfile1","C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv");
            CSVFileHandler.appendingToCSVFile("test","testfile2","C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv");
            ArrayList<String> expected = new ArrayList<>();
            expected.add("testfile");
            expected.add("testfile2");
            assertEquals(expected, CSVFileHandler.searchingForFiles("C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv","test"));
            new File("C:\\Users\\kowal\\Desktop\\projek\\test\\test.csv").delete();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}