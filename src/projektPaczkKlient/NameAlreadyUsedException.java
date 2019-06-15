package projektPaczkKlient;

public class NameAlreadyUsedException extends Exception {
    public NameAlreadyUsedException() { }
    public String GetWarning(){
        return "Name is already in use";
    }
}
