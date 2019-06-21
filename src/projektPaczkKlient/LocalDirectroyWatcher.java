package projektPaczkKlient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class LocalDirectroyWatcher {
    public String dirpath;
    public ArrayList<File> checkpoint;
    public ArrayList<File> toBeSent;
    public File file;

    public LocalDirectroyWatcher(String dirpath) {
        this.dirpath = dirpath;
        this.file =new File(dirpath);
        this.toBeSent = new ArrayList<>();
    }
    public void startup() throws  IOException{
        if(!file.isDirectory()){
            throw new IOException();
        }
        this.checkpoint= new ArrayList<>(Arrays.asList(file.listFiles()));

    }
    ///chceck_For_New method only updates class atributes
    public void check_For_New() throws IOException {
        if(!file.isDirectory()){
            throw new IOException();
        }
        ArrayList<File> current = new ArrayList<>(Arrays.asList(file.listFiles()));
        File tempfile;
        LocalDirectroyWatcher tempClass;
        for(File f:current){
            if(f.isDirectory()){
                tempClass= new LocalDirectroyWatcher(f.getAbsolutePath());
                tempClass.check_For_New();
                for(int i=0;i<tempClass.toBeSent.size();i++){
                    tempfile = tempClass.toBeSent.get(i);
                    if(checkpoint.contains(tempfile)==false) {
                        toBeSent.add(tempfile);
                        checkpoint.add(tempfile);
                    }
                }
            }
            else if(!checkpoint.contains(f)){
                toBeSent.add(f);
                checkpoint.add(f);
            }
        }
    }
    public String getFileNames(){
        String result=new String();
        for(File f : checkpoint){

            result=result+';'+f.getName();
        }
        return result;
    }
}
