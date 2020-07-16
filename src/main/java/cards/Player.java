package cards;

public class Player {
    // the money a player brings to the table. Updated after each bet is settled.
    public double bankroll;

   public void adjustBankroll(double toAdd){
        this.bankroll += toAdd;
    }

}
