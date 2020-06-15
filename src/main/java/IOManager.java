import java.io.IOException;
import java.util.Objects;

public class IOManager {

    Character getApprovedInputChar(String inputString, char... array){
        System.out.print(inputString);

        boolean foundChar = false;
        Character returnChar = null;

        do {
            try {
                byte[] bytes = new byte[256];
                System.in.read(bytes);
                // only interested in the first character input by the user
                returnChar = Character.valueOf((char) bytes[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            foundChar = arrayContains(returnChar, array);
            if (!foundChar) {
                System.out.print("Invalid input: " + inputString);
            }
        } while(!foundChar);
        assert (Objects.nonNull(returnChar));
        return returnChar;
    }

    boolean arrayContains(char toCheck, char[] array){
        for(char character : array){
            if(character == toCheck){
                return true;
            }
        }
        return false;
    }
}
