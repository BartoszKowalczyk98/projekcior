import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;

class SerwerTest {

    @Test
    void Server_setting_up_test() {
        Serwer test = new Serwer();
        String [] args = new String[0];
        test.main(args);
        assertEquals(5,test.disc.size());
        //test.exitApplication();

    }


}