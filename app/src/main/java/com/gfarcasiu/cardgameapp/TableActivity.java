package com.gfarcasiu.cardgameapp;

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
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.gfarcasiu.client.Client;
import com.gfarcasiu.client.MultiServer;
import com.gfarcasiu.game.Entity;
import com.gfarcasiu.game.Game;
import com.gfarcasiu.game.PlayingCard;

import java.lang.reflect.Method;
import java.util.HashMap;


public class TableActivity extends Activity {
    private static Entity currentPlayer;
    private boolean isServer;

    private View viewBeingDragged; // TODO think of a better name...
    private HashMap<View, PlayingCard> cardMap = new HashMap<>(); // TODO there must be a better way

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        setContentView(R.layout.activity_table);

        isServer = getIntent().getExtras().getBoolean("isServer");

        Log.i("Debug", "<isServer " + isServer + "/>");

        // Populate cards
        PlayingCard[] cards = getGame().getVis().getCards();
        for (PlayingCard card : cards)
            displayCard(card);
    }

    public void toHand(View view) {
        Intent intent = new Intent(this, HandActivity.class);
        intent.putExtra("isServer",isServer);
        intent.putExtra("isNewGame", false);

        startActivity(intent);
    }

    private final class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            //Log.i("Debug", "<On touch/>");
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                //Log.i("Debug", "<On touch action down/>");

                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view.getParent(), 0);
                view.setVisibility(View.INVISIBLE);

                cardMap.put(view, getPlayingCardFromImageName((String)view.getTag()));

                viewBeingDragged = view;

                // TODO make clipdata the card information

                return true;
            } else {
                return true;
            }
        }
    }

    private final class MyDragEventListener implements View.OnDragListener {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            if (action == DragEvent.ACTION_DRAG_STARTED) {
                //Log.i("Debug", "<Action drag started/>");
            } else if (action == DragEvent.ACTION_DROP) {
                //Log.i("Debug", "<Action drop/>");
                Log.i("Debug", "<Card info: " + cardMap.get(viewBeingDragged) + "/>");

                final int containerId = v.getId();
                PlayingCard card = cardMap.get(viewBeingDragged);

                try {
                    switch (containerId) {
                        case R.id.table_button:
                            Log.i("Debug", "<Table button dragged/>");
                            executeAction(Game.class.getMethod("playerToVisible",
                                            PlayingCard.class, Entity.class),
                                    cardMap.get(viewBeingDragged));
                            break;
                        case R.id.trash_button:
                            Log.i("Debug", "<Trash button dragged/>");
                            break;
                        case R.id.deck_button:
                            Log.i("Debug", "<Deck button dragged/>");
                            break;
                    }
                } catch (NoSuchMethodException e) {
                    // THIS SHOULD NEVER HAPPEN
                }

                if (containerId != R.id.table_button && containerId != R.id.trash_button
                        && containerId != R.id.deck_button) {
                    ((LinearLayout)viewBeingDragged.getParent()).removeView(viewBeingDragged);
                    ((LinearLayout)findViewById(R.id.middle)).addView(viewBeingDragged);
                    viewBeingDragged.setVisibility(View.VISIBLE);
                }

                viewBeingDragged = null;
            }

            return true;
        }
    }

    // HELPER METHODS
    private void executeAction(Method method, Object...args) {
        if (!isServer)
            Client.getInstance().executeAction(method, args);
        else
            MultiServer.executeAction(method, args);
    }

    private Game getGame() {
        if (!isServer)
            return Client.getInstance().getGame();
        else
            return MultiServer.getGame();
    }

    private String getImageNameFromPlayingCard(PlayingCard card) {
        return "c_" + (4 * (14 - card.getValue()) + card.getSuit() + 1);
    }

    private PlayingCard getPlayingCardFromImageName(String name) {
        int num = Integer.parseInt(name.substring(2));
        return new PlayingCard(
                14 - (num - 1) / 4,
                (num - 1) % 4,
                true);
    }

    private void displayCard(PlayingCard card) {
        String imageName = getImageNameFromPlayingCard(card);

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
