import java.io.IOException;
import java.util.Objects;

public class MyIOUtils {

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
