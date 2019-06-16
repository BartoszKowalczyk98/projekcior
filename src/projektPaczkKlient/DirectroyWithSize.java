package projektPaczkKlient;

import java.io.File;

public class DirectroyWithSize implements Comparable<DirectroyWithSize>{
    public String  dirpath;
    public long size;

    public DirectroyWithSize(String dirpath) {
        this.dirpath = dirpath;
        this.size = 0;
    }
    public void updateSize(){
        File directory = new File(dirpath);
        for(File file: directory.listFiles()){
            if(file.isFile()){
                size+=file.length();
            }
        }
    }

    @Override
    public int compareTo(DirectroyWithSize o) {
        if((this.size-o.size)>0)
            return 1;
        else if((this.size-o.size)<0)
            return -1;
        else
            return 0;
    }
}
