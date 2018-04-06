package cpu.edu.ph.klondike;

import android.graphics.Canvas;
import java.util.LinkedList;
import java.util.List;

public class FoundationPile {
    private static final String TAG = MainGameView.class.getSimpleName();

    private LinkedList<Card> cards = new LinkedList<>();
    private int x;
    private int y;

    public FoundationPile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void addCard(Card card) {
        card.move(x, y);
        cards.add(card);
    }

    public void addCards(List<Card> cards) {
        for (Card card : cards) {
            addCard(card);
        }
    }

    public LinkedList<Card> getCards() {
        return cards;
    }

    public Card getTopmostCard() {
        if (cards.isEmpty()) {
            return null;
        }

        return cards.get(cards.size() - 1);
    }

    public int getDistance(Card draggedCard) { // using the pythagorean theorem
        return (int) Math.sqrt(
                Math.pow(draggedCard.getX() - x, 2) +
                        Math.pow(draggedCard.getY() - y, 2)
        );
    }

    public void removeTop() {
        cards.removeLast();
    }

    public void draw(Canvas canvas) {
        for (Card card : new LinkedList<>(cards)) {
            card.draw(canvas);
        }
    }

    public boolean canAccept(Card draggedCard) {
        if (cards.isEmpty()) {
            return draggedCard.getValue() == 1;
        }
        else {
            Card topCard = getTopmostCard();
            int difference = draggedCard.getValue() - topCard.getValue();

            return topCard.getSuit() == draggedCard.getSuit()
                && difference == 1;
        }
    }
}
