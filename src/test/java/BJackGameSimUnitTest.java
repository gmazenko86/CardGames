import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BJackGameSimUnitTest {

    final String dbConfigFilePath = "src/main/resources/config.txt";

    @Test
    void testgetEndIndex(){
        BJackGameSim gameSim = new BJackGameSim(5000, 32, dbConfigFilePath);
        BJackGameSim.DBMgrSim dbMgrSim = gameSim.new DBMgrSim(dbConfigFilePath);
        checkEndIndex(gameSim, dbMgrSim);

        // check corner case of having only 1 iteration
        gameSim.iterations = 1;
        gameSim.numThreads = 32;
        checkEndIndex(gameSim, dbMgrSim);

        // check corner case of having only 2 threads (must be even number)
        gameSim.iterations = 100;
        gameSim.numThreads = 2;
        checkEndIndex(gameSim, dbMgrSim);

        // check corner case of iterations = threads + 1;
        gameSim.iterations = 33;
        gameSim.numThreads = 32;
        checkEndIndex(gameSim, dbMgrSim);

        // try largest prime number under 1 million
        gameSim.iterations = 999983;
        gameSim.numThreads = 32;
        checkEndIndex(gameSim, dbMgrSim);

        // try most commonly run parameters
        gameSim.iterations = 10000;
        gameSim.numThreads = 32;
        checkEndIndex(gameSim, dbMgrSim);

    }

    void checkEndIndex(BJackGameSim gameSim, BJackGameSim.DBMgrSim dbMgrSim){
        int threadsPerPlayer = gameSim.numThreads/2;
        int blocksize = gameSim.iterations/threadsPerPlayer;
        int totalEntries = 0;
        for (int i = 0; i < threadsPerPlayer; i++){
            int beginIndex = dbMgrSim.getBeginIndex(i, blocksize);
            int endIndex = dbMgrSim.getEndIndex(i, blocksize, threadsPerPlayer);
            int length = endIndex - beginIndex;
            totalEntries += length;
        }
        assertEquals(gameSim.iterations, totalEntries, " Sum of partitions != number of iterations");
    }
}
