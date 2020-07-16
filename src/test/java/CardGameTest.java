
/*
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.getDigits;
*/


public class CardGameTest {
    public static void main(String... args){
        String dbConfigFilePath = "src/main/resources/config.txt";
//        String dbConfigFilePath = "/home/greg/Documents/config_aws_rds.txt";

        BJackGameSim bJackGameSim = new BJackGameSim(5000, 4, dbConfigFilePath);
        bJackGameSim.playGameWrapper();

//        bJackGameSim.dbMgr.truncateTable("dealerhands");
//        bJackGameSim.dbMgr.truncateTable("playerhands");

//        BJackTableWriter bJackTableWriter = new BJackTableWriter(10000, dbConfigFilePath);
//        bJackTableWriter.playGameWrapper();

    }
}
