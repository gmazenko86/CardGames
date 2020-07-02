public class Player {
    // the money a player brings to the table. Updated after each bet is settled.
    double bankroll;

    void adjustBankroll(double toAdd){
        this.bankroll += toAdd;
    }

}
