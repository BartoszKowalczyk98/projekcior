package projektPaczkKlient;

import java.io.Serializable;

public class FileWithUsername implements Serializable {
    public String Username;
    public byte[] bytesarray;
    public String filename;
    public FileWithUsername(String username,byte[] bytearr,String filename) {
        this.Username = username;
        this.bytesarray = bytearr;
        this.filename=filename;
    }
}
