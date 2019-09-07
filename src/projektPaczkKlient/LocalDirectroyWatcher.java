package projektPaczkKlient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
/**Class that handles supervising single directory */

public class LocalDirectroyWatcher {
    /**string that hold path to directory we want to supervise*/
    public String dirpath;
    /**list of files that actually are in directory     */
    public ArrayList<File> checkpoint;
    /**list of files that are waiting to be sent to server     */
    public ArrayList<File> toBeSent;
    /**Directory that is being supervised      */
    public File file;

    /**
     * constructor for class LocalDirectoryWatcher
     * @param dirpath path to directory
     */
    public LocalDirectroyWatcher(String dirpath) {
        this.dirpath = dirpath;
        this.file =new File(dirpath);
        this.toBeSent = new ArrayList<>();
    }

    /**
     * method that makes a snapshot of actual files in directory
     * @throws IOException TBA
     */
    public void startup() throws  IOException{
        if(!file.isDirectory()){
            throw new IOException();
        }
        this.checkpoint=checkInsideDirectory(new ArrayList<>(Arrays.asList(file.listFiles())));
    }

    /**
     * method that is used to recursively add to the list of files
     * @param list list of files
     * @return updated list of files
     */
    private ArrayList<File> checkInsideDirectory(ArrayList<File> list){
        ArrayList<File> resultList = new ArrayList<>();
        for( File f:list){
            if(f.isDirectory()){
                ArrayList<File> temp = new ArrayList<>(Arrays.asList(f.listFiles()));
                resultList.addAll(checkInsideDirectory(temp));
            }
            else
                resultList.add(f);
        }
        return resultList;
    }

    /**
     *  method that updates list of files in supervised directory
     * @throws IOException it is being thrown when @file is not a directory
     */
    public void check_For_New() throws IOException {
        if(!file.isDirectory()){
            throw new IOException();
        }
        ArrayList<File> current = checkInsideDirectory( new ArrayList<>(Arrays.asList(file.listFiles())));

        for(File f:current){
            if(!checkpoint.contains(f)){
                toBeSent.add(f);
                checkpoint.add(f);
            }
        }
    }


    /**
     * method that returns filenames form list of files
     * @return list of strings
     */
    public String getFileNames(){
        String result=new String();
        for(File f : checkpoint){

            result=result+';'+f.getName();
        }
        return result;
    }
}
