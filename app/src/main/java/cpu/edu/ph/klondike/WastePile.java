package cpu.edu.ph.klondike;

import android.graphics.Canvas;

import java.util.LinkedList;
import java.util.List;

public class WastePile {
    private static final String TAG = MainGameView.class.getSimpleName();

    private LinkedList<Card> pile = new LinkedList<>();
    private int x;

    public WastePile(int x) {
        this.x = x;
    }

    public void addCard(Card card) {
        Card topmost = getTopmostCard();
        int y;

        if (pile.isEmpty()) {
            y = Card.getHeight() * 2;
        }
        else {
            y = topmost.getY() + Card.getHeight() / 4;
        }

        card.move(x, y);
        pile.add(card);
    }

    public void clearPile() {
        pile.clear();
    }

    public List<Card> getWastePile() {
        return pile;
    }

    public Card getTopmostCard() {
        if (pile.isEmpty()) {
            return null;
        }
        return pile.get(pile.size() - 1);
    }

    public void removeTop() {
        pile.removeLast();
    }

    public void draw(Canvas canvas) {
        for (Card card : new LinkedList<>(pile)) {
            card.draw(canvas);
        }
    }
}
