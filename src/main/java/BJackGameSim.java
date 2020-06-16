public class BJackGameSim extends BJackGame{
    int iterations;
    int gamesPlayed;

    BJackGameSim(int iterations){
        super();
        this.iterations = iterations;
        gamesPlayed = 0;
    }

    @Override
    void displayActiveHands() {
    }

    @Override
    void displayAllHands() {
    }

    @Override
    void displayResults() {
    }

    @Override
    boolean playAnotherHand() {
        gamesPlayed += 1;
        return gamesPlayed < iterations;
    }

    @Override
    boolean hitHand() {
        return false;
    }

    @Override
    boolean doubleDown() {
        return true;
    }

    @Override
    boolean splitPair() {
        return true;
    }

    @Override
    void displayPlayerBankrolls() {
    }
}
