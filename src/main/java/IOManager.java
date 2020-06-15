import java.io.IOException;
import java.util.Objects;

public class IOManager {

    Character getApprovedInputChar(String inputString, char... array){
        System.out.print(inputString);

        boolean foundChar;
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

    //TODO: DbaseExercises contains printlnBlue and printlnYellow. Build a reusable library
    static  public void printRedText(String str){
        System.out.print("\033[31m"); // This turns the text to red
        System.out.print(str);
        System.out.print("\033[0m"); // This resets the text back to default
    }
    static public void printYellowText(String str){
        System.out.print("\033[33m"); // This turns the text to Yellow
        System.out.print(str);
        System.out.print("\033[0m"); // This resets the text back to default
    }

    static public void printGreenText(String str){
        System.out.print("\033[32m"); // This turns the text to Green
        System.out.print(str);
        System.out.print("\033[0m"); // This resets the text back to default
    }

    static public void printBlueText(String str){
        System.out.print("\033[34m"); // This turns the text to Blue
        System.out.print(str);
        System.out.print("\033[0m"); // This resets the text back to default
    }
}
