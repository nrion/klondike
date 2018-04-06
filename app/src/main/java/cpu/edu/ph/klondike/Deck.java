package cpu.edu.ph.klondike;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Deck {
    private static final String TAG = MainGameView.class.getSimpleName();

    private LinkedList<Card> cards = new LinkedList<>(); // is array list better?
    int x;
    int y;

    public Deck() {
        char[] suits = {'H', 'D', 'C', 'S'};
        for (int i = 1; i <= 13; i++) {
            for (char suit : suits) {
                cards.add(new Card(i, suit, 0, 0));
            }
        }
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void addCards(List<Card> allCards) {
        LinkedList<Card> sorted = new LinkedList<>(allCards);

        for (Card card : new LinkedList<>(allCards)) {
            card.conceal();
            card.move(x, y);

            cards.addFirst(card);
        }
    }

    public List<Card> getCards() {
        return cards;
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card drawFromTop() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.removeLast();
    }

    public Card getTopmostCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.get(cards.size() - 1);
    }
}
