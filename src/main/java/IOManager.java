import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class IOManager {

    Character getApprovedInputChar(String inputString, char... array){
        System.out.print(inputString);

        int arrayIndex = 0;
        Character returnChar = null;

        do {
            try {
                byte[] bytes = new byte[256];
                System.in.read(bytes);
                // only interested in the first character input by the user
                returnChar = Character.valueOf((char) bytes[0]);
                // now ignore everything else. fill the byte array with zeros
                Arrays.fill(bytes, (byte) 0x0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Arrays.binarySearch() will return < 0 if the char was not found
            arrayIndex = Arrays.binarySearch(array, Character.valueOf(returnChar));
            if (arrayIndex < 0) {
                System.out.print("Invalid input: " + inputString);
            }
        } while(arrayIndex < 0);
        assert (Objects.nonNull(returnChar));
        return returnChar;
    }
}
