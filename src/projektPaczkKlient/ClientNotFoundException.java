package projektPaczkKlient;

public class ClientNotFoundException extends Exception {
    public ClientNotFoundException() { }
    public String GetWarning(){
        return "No such client found";
    }
}
