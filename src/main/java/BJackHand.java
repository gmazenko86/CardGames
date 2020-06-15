public class BJackHand extends Hand{

    // these attributes drive special handling
    enum HandAttribute {NONE, STICK, BLACKJACK, BUST, SPLITHAND, DOUBLEDOWN, SURRENDER}
    // HandResult represents the final disposition of a player hand and determines payments
    enum  HandResult {PENDING, WIN, LOSE, PUSH}
    boolean playingThis;

    HandAttribute handAttribute;
    HandResult handResult;
    double bet;

    BJackHand(){
        super();
        handAttribute = HandAttribute.NONE;
        handResult = HandResult.PENDING;
        bet = 100.;
        playingThis = false;
    }

    public int getHandTotal(){
        int total = 0;
        int aceCount =  this.faceCount(Card.CardFace.ACE);
        for(Card card: this.cards){
            total += card.getCardValue();
        }
        while((aceCount > 0) && (total > 21)){
            total -= 10;
            aceCount -= 1;
        }
        return total;
    }

    void setBlackJackFlag(){
        if(this.cards.size() == 2 && this.getHandTotal() == 21){
            this.handAttribute = BJackHand.HandAttribute.BLACKJACK;
        }
    }

    void handleDoubleDown(Deck deck){
        this.drawCard(deck);
        this.handAttribute = BJackHand.HandAttribute.DOUBLEDOWN;
        this.bet *= 2;
    }

    boolean havePair(){
        if(this.cards.size() == 2) {
            return this.cards.get(0).cardFace == this.cards.get(1).cardFace;
        }
        return false;
    }

    boolean pairAces(){
        if(this.cards.size() == 2) {
            return this.cards.get(0).cardFace == Card.CardFace.ACE &&
                    this.cards.get(1).cardFace == Card.CardFace.ACE;
        }
        return false;
    }

    boolean canHit(){
        if(this.handAttribute == BJackHand.HandAttribute.SPLITHAND &&
                this.cards.get(0).cardFace == Card.CardFace.ACE){
            return false;
        }
        return this.handAttribute != BJackHand.HandAttribute.DOUBLEDOWN;
    }

    boolean checkDoubleDown(){
        if(this.cards.size() == 2){
            if(this.faceCount(Card.CardFace.ACE) > 0){
                return this.handAttribute != BJackHand.HandAttribute.SPLITHAND;
            }
            return 9 <= this.getHandTotal() && this.getHandTotal() <= 11;
        }
        return false;
    }

    void setLoseForBust(){
        if(handAttribute == BJackHand.HandAttribute.BUST){
            handResult = BJackHand.HandResult.LOSE;
        }
    }

    void setWinForDealerBust(BJackHand dealerHand){
        if(dealerHand.handAttribute == BJackHand.HandAttribute.BUST &&
                this.handResult == BJackHand.HandResult.PENDING){
            this.handResult = BJackHand.HandResult.WIN;
        }
    }

    void setResultPerTotals(BJackHand dealerHand){
        int dealerTotal = dealerHand.getHandTotal();
        int handTotal = this.getHandTotal();
        if(handResult == BJackHand.HandResult.PENDING) {
            if (handTotal == dealerTotal) {
                handResult = BJackHand.HandResult.PUSH;
            } else if (handTotal > dealerTotal) {
                handResult = BJackHand.HandResult.WIN;
            } else {
                handResult = BJackHand.HandResult.LOSE;
            }
        }
    }

    void setResultPerBJacks(BJackHand dealerHand){
        if(dealerHand.handAttribute == BJackHand.HandAttribute.BLACKJACK){
            if(this.handAttribute == BJackHand.HandAttribute.BLACKJACK){
                this.handResult = BJackHand.HandResult.PUSH;
            } else {
                this.handResult = BJackHand.HandResult.LOSE;
            }
        }
        if(this.handAttribute == BJackHand.HandAttribute.BLACKJACK &&
                dealerHand.handAttribute != BJackHand.HandAttribute.BLACKJACK){
            this.handResult = BJackHand.HandResult.WIN;
        }
    }

    void displayHand(){
        for(Card card: this.cards){
            if (this.playingThis){
                IOManager.printBlueText(card.getCardSignature());
                IOManager.printBlueText(" | ");
            } else{
                card.displayCardSignature();
                System.out.print(" | ");
            }
        }
        System.out.print("");
    }

    void displayHandWithTotal(boolean printResults){
        displayHand();
        System.out.print("Total is " + getHandTotal());

        if(handAttribute == BJackHand.HandAttribute.BUST){
            IOManager.printRedText(" ::: BUST");
        }
        if(handAttribute == BJackHand.HandAttribute.BLACKJACK){
            IOManager.printGreenText(" ::: BLACKJACK");
        }
        if(printResults){
            if(handResult == BJackHand.HandResult.WIN){
                IOManager.printGreenText(" -----Player result = " + handResult.name());
            } else if(handResult == BJackHand.HandResult.LOSE){
                IOManager.printRedText(" -----Player result = " + handResult.name());
            } else if(handResult == BJackHand.HandResult.PUSH){
                IOManager.printBlueText(" -----Player result = " + handResult.name());
            }
            else{ assert(false);}
        }
        System.out.println();
    }
}
