package com.gfarcasiu.cardgameapp;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.gfarcasiu.client.Client;
import com.gfarcasiu.client.MultiServer;
import com.gfarcasiu.game.Entity;
import com.gfarcasiu.game.Game;
import com.gfarcasiu.game.PlayingCard;
import com.gfarcasiu.utilities.HelperFunctions;

import java.lang.reflect.Method;
import java.util.HashMap;


public class TableActivity extends Activity {
    private static String uniqueId;
    private boolean isServer;

    private View viewBeingDragged; // TODO think of a better name...
    private HashMap<View, PlayingCard> cardMap = new HashMap<>(); // TODO there must be a better way

    private long commandTimeStamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        setContentView(R.layout.activity_table);

        isServer = getIntent().getExtras().getBoolean("isServer");
        uniqueId = getIntent().getExtras().getString("uniqueId");

        Log.i("Debug", "<Device is server " + isServer + "/>");

        // Populate cards
        PlayingCard[] cards = HelperFunctions.getGame(isServer).getVis().getCards();
        for (PlayingCard card : cards)
            displayCard(card);

        // Set Listeners
        MyTouchListener touchListener = new MyTouchListener();
        findViewById(R.id.deck_card).setOnTouchListener(touchListener);

        MyDragEventListener dragEventListener = new MyDragEventListener();
        findViewById(R.id.top).setOnDragListener(dragEventListener);
        findViewById(R.id.middle).setOnDragListener(dragEventListener);
        findViewById(R.id.bottom).setOnDragListener(dragEventListener);
        findViewById(R.id.deck_card).setOnDragListener(dragEventListener);
    }

    public void toHand(View view) {
        this.onStop();

        Intent intent = new Intent(this, HandActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("isServer", isServer);
        intent.putExtra("isNewGame", false);

        startActivity(intent);
    }

    private final class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view.getParent(), 0);

                if (view.getId() == R.id.deck_card) {
                    cardMap.put(view, Game.getRandomCard(HelperFunctions.getGame(isServer).getDeck()));
                } else {
                    ((LinearLayout) view.getParent()).removeView(view);
                    cardMap.put(view, HelperFunctions.getPlayingCardFromImageName((String) view.getTag()));
                }

                viewBeingDragged = view;

                return true;
            } else {
                return false;
            }
        }
    }

    private final class MyDragEventListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            if (action == DragEvent.ACTION_DRAG_STARTED) {
                ((TextView) findViewById(R.id.notification_bar)).setText("TO HAND");
            } else if (action == DragEvent.ACTION_DROP) {
                Log.i("Debug", "<Card info: " + cardMap.get(viewBeingDragged) + "/>");

                final int containerId = v.getId();
                PlayingCard card = cardMap.get(viewBeingDragged);

                try {
                    switch (containerId) {
                        case R.id.bottom:
                            Log.i("Debug", "<Bottom dragged/>");
                            if (viewBeingDragged.getId() == R.id.deck_card) {
                                executeAction(
                                    Game.class.getMethod("deckToPlayer", PlayingCard.class, String.class),
                                    card, uniqueId);
                            } else {
                                executeAction(
                                    Game.class.getMethod("visibleToPlayer", PlayingCard.class, String.class),
                                    card, uniqueId);
                            }
                            break;
                        case R.id.middle:
                            Log.i("Debug", "<Middle dragged/>");

                            if (viewBeingDragged.getId() == R.id.deck_card) {
                                executeAction(Game.class.getMethod("deckToVis", PlayingCard.class),
                                    card);
                            }

                            break;
                        case R.id.deck_card:
                            Log.i("Debug", "<Top dragged/>");
                            if (viewBeingDragged.getId() != R.id.deck_card) {
                                executeAction(
                                    Game.class.getMethod("visibleToDeck", PlayingCard.class),
                                    card
                                );
                            }

                            break;
                    }
                } catch (NoSuchMethodException e) {
                    // THIS SHOULD NEVER HAPPEN
                    e.printStackTrace();
                }

                if (containerId == R.id.bottom) {
                    cardMap.remove(viewBeingDragged);
                } else if (containerId == R.id.middle) {
                    if (viewBeingDragged.getId() == R.id.deck_card)
                        displayCard(card);
                    else
                        ((LinearLayout)findViewById(R.id.middle)).addView(viewBeingDragged);
                } else if (containerId == R.id.top) {
                    if (viewBeingDragged.getId() != R.id.deck_card)
                        ((LinearLayout)findViewById(R.id.middle)).addView(viewBeingDragged);
                } else if (containerId == R.id.deck_button) {
                    if (viewBeingDragged.getId() != R.id.deck_card)
                        cardMap.remove(viewBeingDragged);
                }

                viewBeingDragged = null;
                ((TextView) findViewById(R.id.notification_bar)).setText("");
            }

            return true;
        }
    }

    // HELPER METHODS
    private void executeAction(final Method method, final Object...args) {
        // Wait to execute calls if they occur too quickly
        if (System.currentTimeMillis() - commandTimeStamp > 0.25 * Math.pow(10, 9)) {
            if (!isServer)
                Client.getInstance().executeAction(method, args);
            else
                MultiServer.executeAction(method, args);

            commandTimeStamp = System.currentTimeMillis();
        } else {
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!isServer)
                        Client.getInstance().executeAction(method, args);
                    else
                        MultiServer.executeAction(method, args);
                }
            }.start();

            commandTimeStamp = System.currentTimeMillis() + 200;
        }
    }

    private void displayCard(PlayingCard card) {
        String imageName = HelperFunctions.getImageNameFromPlayingCard(card);

        Context context = getApplicationContext();
        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier(imageName, "drawable",
                context.getPackageName());
        Drawable drawable = context.getResources().getDrawable(resourceId);

        ImageView cardView = new ImageView(getApplicationContext());

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 115, getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, getResources().getDisplayMetrics());

        // TODO margins are not applying
        LinearLayout.MarginLayoutParams layoutParams = new LinearLayout.MarginLayoutParams(width, height);
        layoutParams.setMargins(margin, 0, margin, 0);
        cardView.setLayoutParams(layoutParams);

        cardView.setBackground(drawable);
        cardView.setLayoutParams(layoutParams);
        cardView.setTag(imageName);
        cardView.setOnTouchListener(new MyTouchListener());

        ((LinearLayout) findViewById(R.id.middle)).addView(cardView);

        cardMap.put(cardView, card);
    }
}
