package projektPaczkKlient;

import java.io.Serializable;

/**
 * class that is only used to be read/written though streams
 */
public class FileWithUsername implements Serializable {
    /**name of the owner of the file  */
    public String Username;
    /**file converted to array of bytes */
    public byte[] bytesarray;
    /**name of the file */
    public String filename;

    /**
     * constructor for this class
     *
     * @param username name fo the owner
     * @param bytearr file converted to array of bytes
     * @param filename name of the file
     */
    public FileWithUsername(String username,byte[] bytearr,String filename) {
        this.Username = username;
        this.bytesarray = bytearr;
        this.filename=filename;
    }
}
