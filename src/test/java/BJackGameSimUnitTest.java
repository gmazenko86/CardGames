import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BJackGameSimUnitTest {

    final String dbConfigFilePath = "src/main/resources/config.txt";
    //TODO: figure out why the test cannot access getEndIndex()
/*
    @Test
    void testgetEndIndex(){
        BJackGameSim gameSim = new BJackGameSim(5000, 32, dbConfigFilePath);
        System.out.println(gameSim.dbMgr.toString());
        int threadsPerPlayer = gameSim.numThreads/2;
        int blocksize = 312;
        int endIndex = gameSim.dbMgr.getEndIndex(15, blocksize, threadsPerPlayer);


    }
*/
}
