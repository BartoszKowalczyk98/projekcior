package projektPaczkKlient;

import java.io.File;
import java.util.ArrayList;

/**
 * Class that is used to simulate discs on server
 *
 */
public class DirectroyWithSize implements Comparable<DirectroyWithSize>{
    /**path to directory that is disc      */
    public String  dirpath;
    public int size=0;

    /**
     * Constructor for class
     * @param dirpath path to directory
     */
    public DirectroyWithSize(String dirpath) {
        this.dirpath = dirpath;
        size=0;
    }

    /**
     * method that returns filenames as an arraylist of stringa
     * @return arraylsit of strings of the names of files in that directory
     */
    public ArrayList<String> getFileNames(){
        File directory = new File(dirpath);
        ArrayList<String> result = new ArrayList<>();
        for(File file: directory.listFiles()){
            if(!file.getName().equals("info.csv")) {
                result.add(file.getName());
            }
        }
        return result;
    }

    /**
     * this method updates filed size within class
     */
    public void updateSize(){
        File f = new File(dirpath);
        for(File asd : f.listFiles()){
            size++;
        }
    }

    /**
     * method required to use Collections.sort
     * @param o another object of class DirectoryWithSize that we want to compare to
     * @return positive number if size of this is bigger, 0 if sizes of both objects are equal , negative number if object o has bigger size
     */
    @Override
    public int compareTo(DirectroyWithSize o) {
        return this.size-o.size;
    }
}
