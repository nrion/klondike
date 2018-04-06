// NOTE: this is the updated one
package cpu.edu.ph.klondike;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.LinkedList;
import java.util.List;

public class MainGameView extends SurfaceView implements SurfaceHolder.Callback {
    private MainThread thread;
    private static final String TAG = MainGameView.class.getSimpleName();

    private Deck deck = new Deck();

    private Card touchedCard;
    private LinkedList<Card> draggedCards = new LinkedList<>();

    private TableauPile[] tableaus = new TableauPile[7];
    private TableauPile sourceTableau;

    private FoundationPile[] foundationPiles = new FoundationPile[4];
    private FoundationPile sourceFoundation;

    private WastePile wastePile;
    private WastePile sourceWastePile;
    private LinkedList<Card> undisplayedWaste = new LinkedList<>();

    private int touchedX;
    private int touchedY;

    private int deckX;
    private int deckY;

    private boolean isDragging;
    private String phase = "";
    private long lastUpdate = System.currentTimeMillis();
    private static Bitmap picture;

    public MainGameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new MainThread(getHolder(), this);
        setFocusable(true);
    }

    public void stopGame() {
        thread.setRunning(false);
    }

    public void startGame() {
        thread.setRunning(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public static Bitmap getPicture() {
        return picture;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        picture = BitmapFactory.decodeResource(
                getContext().getResources(),
                R.drawable.playingcards
        );
        picture = Bitmap.createScaledBitmap(
                picture, getWidth(), getHeight(), true
        );

        Card.setSizeOfAllCards(getWidth(), getHeight());

        // just create a new game when app is resumed after pressing back
        if (!thread.isAlive() && thread.getState() != Thread.State.NEW) {
            thread = new MainThread(getHolder(), this);
        }

        thread.setRunning(true);
        thread.start();

        deck.shuffle();

        deckX = getWidth() - Card.getWidth() - 50;
        deckY = getHeight() / 2 - Card.getHeight() * 2;

        deck.setX(deckX);
        deck.setY(deckY);

        for (Card card : deck.getCards()) {
            card.move(deckX, deckY);
        }

        phase = "dealing";

        for (int i = 0; i < tableaus.length; i++) {
            int tableauX = (i + 1) * (Card.getWidth() + 10) + 50;
            tableaus[i] = new TableauPile(tableauX);
        }

        for (int i = 1; i <= 7; i++) {
            int j;
            for (j = i; j <= 7; j++) {
                Card card = deck.drawFromTop();
                tableaus[j - 1].addCard(card);

                if (j == i) {
                    card.reveal();
                }
            }
        }

        int foundationY = 45;
        for (int i = 0; i < foundationPiles.length; i++) {
            foundationPiles[i] = new FoundationPile(20, foundationY);
            foundationY += Card.getHeight() + 15;
        }

        wastePile = new WastePile(deck.getCards().get(0).getX());
    }

    // NOTE: no touch....fine as it is
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            }
            catch (InterruptedException ex) {
                // do nothing
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isBusy()) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getX() >= getWidth() - 50 && event.getY() >= getHeight() - 50) {
                ((MainActivity) getContext()).restartGame();
            }

            if (event.getX() >= deckX && event.getX() <= deckX + Card.getWidth()) {
                if (event.getY() >= deckY && event.getY() <= deckY + Card.getHeight()) {
                    draggedCards.clear(); // clear the draggedCards first so that it won't show

                    if (!deck.getCards().isEmpty()) {
                        if (wastePile.getWastePile().isEmpty()) {
                            for (int i = 1; i <= 3; i++) {
                                Card card = deck.drawFromTop();

                                wastePile.addCard(card);
                                card.reveal();
                            }
                        }
                        else if (wastePile.getWastePile().size() <= 3
                                && wastePile.getWastePile().size() > 0) {
                            undisplayedWaste.addAll(wastePile.getWastePile());
                            wastePile.clearPile();

                            for (int i = 1; i <= 3; i++) {
                                if (deck.getTopmostCard() != null) {
                                    Card card = deck.drawFromTop();

                                    wastePile.addCard(card);
                                    card.reveal();
                                }
                            }
                        }
                    }
                    else {
                        List<Card> combined = new LinkedList<>();
                        combined.addAll(undisplayedWaste);
                        combined.addAll(wastePile.getWastePile());

                        deck.addCards(combined);

                        wastePile.clearPile();
                        undisplayedWaste.clear();
                        combined.clear();
                    }
                }
            }

            sourceTableau = null;
            for (TableauPile pile : tableaus) {
                for (int i = pile.getCards().size() - 1; i >= 0; i--) {
                    Card card = pile.getCards().get(i);

                    if (card.isOpen()) {
                        card.handleTouch((int) event.getX(), (int) event.getY());
                    }

                    if (card.isTouched()) {
                        touchedX = (int) event.getX();
                        touchedY = (int) event.getY();

                        touchedCard = card;
                        sourceTableau = pile;

                        draggedCards.clear();
                        int from = sourceTableau.getCards().indexOf(touchedCard);
                        draggedCards.addAll(sourceTableau.getCards().subList(
                                from, sourceTableau.getCards().size()
                        ));

                        int howMany = sourceTableau.getCards().size() - from;
                        sourceTableau.removeCards(howMany);

                        break;
                    }
                }
            }

            sourceFoundation = null;
            for (FoundationPile pile : foundationPiles) {
                if (pile != null) {
                    Card topmost = pile.getTopmostCard();

                    if (topmost != null) {
                        topmost.handleTouch((int) event.getX(), (int) event.getY());

                        if (topmost.isTouched()) {
                            touchedX = (int) event.getX();
                            touchedY = (int) event.getY();

                            touchedCard = topmost;
                            sourceFoundation = pile;

                            draggedCards.clear();
                            draggedCards.add(touchedCard);

                            pile.removeTop();
                        }
                    }
                }
            }

            sourceWastePile = null;
            if (!wastePile.getWastePile().isEmpty()) {
                Card topmostWaste = wastePile.getTopmostCard();

                topmostWaste.handleTouch((int) event.getX(), (int) event.getY());

                if (topmostWaste.isTouched()) {
                    touchedX = (int) event.getX();
                    touchedY = (int) event.getY();

                    touchedCard = topmostWaste;
                    sourceWastePile = wastePile;

                    draggedCards.clear();
                    draggedCards.add(touchedCard);

                    if (wastePile.getTopmostCard() != null) {
                        wastePile.removeTop();
                    }
                }
            }

            Log.d(TAG, "deck: " + deck.getCards());
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            isDragging = true;

            if (touchedCard != null) {
                int deltaX = (int) event.getX() - touchedX;
                int deltaY = (int) event.getY() - touchedY;
                touchedCard.moveByDelta(deltaX, deltaY);

                int y = touchedCard.getY();
                for (Card cardOnMove : draggedCards) {
                    cardOnMove.move(touchedCard.getX(), y);
                    y += Card.getHeight() / 4;
                }

                touchedX = (int) event.getX();
                touchedY = (int) event.getY();
            }
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            isDragging = false;

            if (touchedCard != null) {
                onDragEnd();

                touchedCard.setTouched(false);
                touchedCard = null;
            }
        }
        return true;
    }

    public void onDragEnd() {
        FoundationPile nearestFoundation = null;
        TableauPile nearestTableau = null;
        int shortestDistance = Integer.MAX_VALUE;
        int nearestPileIndex = -1;

        for (int i = 0; i < foundationPiles.length; i++) {
            int distance = foundationPiles[i].getDistance(touchedCard);

            if (distance < shortestDistance) {
                shortestDistance = distance;
                nearestPileIndex = i;
            }
        }

        if (shortestDistance <= 25) {
            nearestFoundation = foundationPiles[nearestPileIndex];
        }

        if (nearestFoundation != null) {
            if (nearestFoundation.canAccept(touchedCard)) {
                nearestFoundation.addCards(draggedCards);

                if (sourceTableau != null) {
                    sourceTableau.revealLast();
                }
            }
        }
        // else if there is no nearestFoundation,
        // the card must be pointed to another tableau
        else {
            for (int i = 0; i < tableaus.length; i++) {
                if (tableaus[i].getCards().isEmpty()) {
                    Log.d(TAG, "tableau " + i + " is empty!");

                    if (touchedCard.getValue() == 13) {
                        Log.d(TAG, "touchedCard is a King...");
                        // don't skip
                    }
                    else {
                        continue;
                    }
                }
                int distance = tableaus[i].getDistance(touchedCard);
                Log.d(TAG, "distance from " + touchedCard + " to " + i
                        + " is " + distance);

                if (distance < shortestDistance) {
                    shortestDistance = distance;
                    nearestPileIndex = i;
                }
            }

            if (shortestDistance <= 25) {
                nearestTableau = tableaus[nearestPileIndex];
            }

            if (nearestTableau != null) {
                if (nearestTableau.canAccept(touchedCard)) {
                    nearestTableau.addCards(draggedCards);

                    if (sourceTableau != null) {
                        sourceTableau.revealLast();
                    }
                }
                else {
                    if (sourceTableau != null) {
                        sourceTableau.addCards(draggedCards);
                    }
                    else if (sourceWastePile != null) {
                        sourceWastePile.addCard(draggedCards.get(0));
                    }
                    else if (sourceFoundation != null) {
                        sourceFoundation.addCards(draggedCards);
                    }
                }
            }
            else {
                if (sourceTableau != null) {
                    sourceTableau.addCards(draggedCards);
                }
                else if (sourceWastePile != null) {
                    sourceWastePile.addCard(draggedCards.get(0));
                }
                else if (sourceFoundation != null) {
                    sourceFoundation.addCards(draggedCards);
                }
            }
        }
    }

    public boolean isDealing() {
        return phase.equals("dealing");
    }

    public boolean isSnapping() {
        return phase.equals("snapping");
    }

    public boolean isBusy() {
        return isDealing();
    }

    private int animateCounter = 0;
    public void update() {
        long deltaTime = System.currentTimeMillis() - lastUpdate;

        phase = "playing";
        lastUpdate = System.currentTimeMillis();
    }

    public void drawSlots(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        paint.setColor(Color.LTGRAY);

        Paint tableauPaint = new Paint();
        tableauPaint.setStyle(Paint.Style.STROKE);
        tableauPaint.setStrokeWidth(2);
        tableauPaint.setColor(Color.DKGRAY);

        //tableauPile
        for (int i = 0; i < tableaus.length; i++) {
            int tableauX = (i + 1) * (Card.getWidth() + 10) + 50;
            int y = Card.getHeight() / 4;

            RectF rect = new RectF(tableauX,  y, tableauX + Card.getWidth(), y + Card.getHeight());
            canvas.drawRoundRect(rect, 10, 10, tableauPaint);
        }
        // foundationPile
        int y = 45;
        for (int i = 1; i <= 4; i++) {
            RectF rect = new RectF(20,  y, 20 + Card.getWidth(), y + Card.getHeight());
            canvas.drawRoundRect(rect, 10, 10, paint);
            y += Card.getHeight() + 15;
        }

        // wastePile
        int deckSlotX = getWidth() - Card.getWidth() - 50;
        int deckSlotY = getHeight() / 2 - Card.getHeight() * 2;
        RectF rect = new RectF(
             deckSlotX,
             deckSlotY,
             deckSlotX + Card.getWidth(),
             deckSlotY + Card.getHeight());
        canvas.drawRoundRect(rect, 10, 10, paint);

    }

    public void drawRestartButton(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.LTGRAY);

        Paint textPaint = new Paint();
        textPaint.setTextSize(25);
        textPaint.setColor(Color.LTGRAY);

        int x = getWidth() - 50;
        int y = getHeight() - 50;

        RectF rect = new RectF(x,  y, getWidth(),getHeight());
        canvas.drawRoundRect(rect, 10, 10, paint);

        canvas.drawText("RESTART", getWidth() - 160, getHeight() - 15, textPaint);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (canvas == null) {
            return;
        }

        canvas.drawRGB(0, 62, 62);
        drawSlots(canvas);
        drawRestartButton(canvas);

        // stupid
        for (Card card : new LinkedList<>(deck.getCards())) {
            card.draw(canvas);
        }

        for (TableauPile pile : tableaus) {
            pile.draw(canvas);
        }

        for (FoundationPile pile : foundationPiles) {
            if (pile != null) {
                pile.draw(canvas);
            }
        }

        if (wastePile != null) {
            wastePile.draw(canvas);
        }

        for (Card card : new LinkedList<>(draggedCards)) {
            card.draw(canvas);
        }
    }
}
