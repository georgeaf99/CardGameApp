package com.gfarcasiu.cardgameapp;

import android.app.Activity;
import android.content.ClipData;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.gfarcasiu.client.Client;
import com.gfarcasiu.client.MultiServer;
import com.gfarcasiu.game.Entity;
import com.gfarcasiu.game.Game;
import com.gfarcasiu.game.PlayingCard;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;


public class HandActivity extends Activity {
    private Entity currentPlayer;
    private boolean isServer;

    private View viewBeingDragged; // TODO think of a better name...
    private HashMap<View, PlayingCard> cardMap = new HashMap<>(); // TODO there must be a better way

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);

        setContentView(R.layout.activity_hand);

        // Initialize game and player settings
        isServer = getIntent().getExtras().getBoolean("isServer");
        currentPlayer = new Entity(52);

        try {
            executeAction(Game.class.getMethod("addPlayer", Entity.class), currentPlayer);
        } catch (NoSuchMethodException e) {
            Log.e("Error", "<Player could not be added/>");
            this.onStop();
            return;
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.i("Debug", "<Player info: " + Arrays.toString(MultiServer.getGame().getPlayers()) + "/>");

        // Set listeners
        findViewById(R.id.playing_card).setOnTouchListener(new MyTouchListener());

        MyDragEventListener dragEventListener = new MyDragEventListener();
        findViewById(R.id.bottom).setOnDragListener(dragEventListener);
        findViewById(R.id.middle).setOnDragListener(dragEventListener);
        findViewById(R.id.top).setOnDragListener(dragEventListener);
        findViewById(R.id.deck_button).setOnDragListener(dragEventListener);
        findViewById(R.id.trash_button).setOnDragListener(dragEventListener);
        findViewById(R.id.table_button).setOnDragListener(dragEventListener);
    }

    private final class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            Log.i("Debug", "<On touch/>");
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                Log.i("Debug", "<On touch action down/>");

                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(data, shadowBuilder, view.getParent(), 0);
                view.setVisibility(View.INVISIBLE);

                cardMap.put(view, new PlayingCard(10, 1, true));

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
                Log.i("Debug", "<Action drag started/>");
            } else if (action == DragEvent.ACTION_DROP) {
                Log.i("Debug", "<Action drop/>");
                Log.i("Debug", "<Card info: " + cardMap.get(viewBeingDragged) + "/>");

                final int containerId = v.getId();

                switch (containerId) {
                    case R.id.table_button:
                        Log.i("Debug", "<Table button dragged/>");
                        break;
                    case R.id.trash_button:
                        Log.i("Debug", "<Trash button dragged/>");
                        break;
                    case R.id.deck_button:
                        Log.i("Debug", "<Deck button dragged/>");
                        break;
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

    // HELPER METHOD
    private void executeAction(Method method, Object...args) {
        if (!isServer)
            Client.getInstance().executeAction(method, args);
        else
            MultiServer.executeAction(method, args);
    }
}


