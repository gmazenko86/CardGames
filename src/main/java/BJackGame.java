public class BJackGame extends CardGame {

    void playGame(){
        initializePlayers();
        initializeDeck();
        initializeHands();

        for(Card card : deck.cards){
            card.displayCardSignature();
            System.out.print("| ");
        }
        System.out.println("\n");

        displayAllHands();
        System.out.println("end of demo message");
    }
    void initializePlayers(){
        Player dealer = new Player();
        players.add(dealer);
        Player greg = new Player();
        players.add(greg);
//        Player carolyn = new Player();
//        players.add(carolyn);
    }

    void initializeHands(){
        // deal 2 cards for each hand, including the dealer
        for(int i = 0; i < 2; i++) {
            for (Player player : this.players) {
                for (Hand hand : player.hands) {
                    hand.drawCard(this.deck);
                }
            }
        }
    }

    void displayAllHands(){
        for (Player player : this.players) {
            for (Hand hand : player.hands) {
                for(Card card: hand.cards){
                    card.displayCardSignature();
                    System.out.print(" | ");
                }
            }
            System.out.println("\n");
        }
    }
}
