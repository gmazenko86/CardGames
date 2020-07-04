
/*
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.getDigits;
*/


public class CardGameTest {
    public static void main(String... args){
        String dbConfigFilePath = "src/main/resources/config.txt";

//        BJackGameSim bJackGameSim = new BJackGameSim(1000, dbConfigFilePath);
//        bJackGameSim.playGameWrapper();

        BJackTableWriter bJackTableWriter = new BJackTableWriter(10000, dbConfigFilePath);
        bJackTableWriter.playGameWrapper();

    }
}
