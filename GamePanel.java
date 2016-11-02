package com.kp.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;



public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {

    public static final int MOVESPEED = -5;
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int BRICKS = 15;
    private long missileStartTime;
    private long smokeStartTime;
    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<Border> topBorder;
    private ArrayList<Border> botBorder;
    private boolean readyToPlay;
    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean disappear;
    private boolean started;
    private int topScore;
    private SharedPreferences prefs;

    public GamePanel(Context context, SharedPreferences prefs) {
        super(context);
        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);
        //make gamePanel focusable so it can handle events
        setFocusable(true);
        readyToPlay = true;
        this.prefs = prefs;
        topScore = prefs.getInt("Top Score", 0);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int count = 0;
        while(retry && count < 1000) {
            count++;
            try{
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;
            } catch(InterruptedException e){
                e.printStackTrace();
            }
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65,
                            25, 3);
        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        smoke = new ArrayList<>();
        missiles = new ArrayList<>();
        botBorder = new ArrayList<>();
        topBorder = new ArrayList<>();

        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();
        //we can safely start the game loop
        thread = new MainThread(getHolder(), this);
        thread.setRunning(true);
        thread.start();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying() && reset && !readyToPlay) {
                player.setPlaying(true);
            }
            if (player.getPlaying()) {
                reset = false;
                if (!started) {
                    started = true;
                }
            }
            player.setUp(true);
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void update() {
        if (player.getPlaying()) {
            readyToPlay = true;
            bg.update();
            player.update();
            updateBorders();
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - player.getScore() / 2)) {
                missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),
                             R.drawable.missile), WIDTH + 10, BRICKS + (int)(Math.random() *
                             (HEIGHT - 3 * BRICKS)), 45, 15, player.getScore(), 13));
                missileStartTime = System.nanoTime();
            }
            for (int i = 0; i < missiles.size(); i++) {
                missiles.get(i).update();
                if (player.collision(missiles.get(i))) {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                } else if (missiles.get(i).getX() < -50) {
                    missiles.remove(i);
                }
            }

            for (Border border : topBorder) {
                if (player.collision(border)) {
                    player.setPlaying(false);
                }
            }
            for (Border border : botBorder) {
                if (player.collision(border)) {
                    player.setPlaying(false);
                }
            }

            long smokeElapsed = (System.nanoTime() - smokeStartTime) / 1000000;
            if (smokeElapsed > 120) {
                smoke.add(new Smokepuff(player.getX(), player.getY() + 10));
                smokeStartTime = System.nanoTime();
            }
            for (int i = 0; i < smoke.size(); i++) {
                smoke.get(i).update();
                if (smoke.get(i).getX() < - 10) {
                    smoke.remove(i);
                }
            }
        } else {
            if (!reset) {
                startReset = System.nanoTime();
                reset = true;
                disappear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),
                                          R.drawable.explosion), player.getX(), player.getY() - 30,
                                          100, 100, 25);
            }
            explosion.update();
            long resetElapsed = (System.nanoTime() - startReset) / 1000000;
            if (readyToPlay && resetElapsed > 2500) {
                newGame();
            }
        }
    }
    @Override
    public void draw(Canvas canvas) {

        final float scaleFactorX = getWidth() / (float) WIDTH;
        final float scaleFactorY = getHeight() / (float) HEIGHT;

        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            if (!disappear) {
                player.draw(canvas);
            }
            for (Smokepuff puff : smoke) {
                puff.draw(canvas);
            }
            for (Missile missile : missiles) {
                missile.draw(canvas);
            }

            for (Border border : topBorder) {
                border.draw(canvas);
            }
            for (Border border : botBorder) {
                border.draw(canvas);
            }
            if (started) {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);
        }
    }

    private void updateBorders() {
        for (int i = 0; i < topBorder.size(); i++) {
            topBorder.get(i).update();
            botBorder.get(i).update();
            if (topBorder.get(i).getX() <= -20) {
                topBorder.remove(i);
            }
            if (botBorder.get(i).getX() <= -20) {
                botBorder.remove(i);
            }
        }
        if (topBorder.size() * 20 <= WIDTH + 20) {
            topBorder.add(new Border(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    topBorder.get(topBorder.size() - 1).getX() + 20, 0));
            botBorder.add(new Border(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botBorder.get(botBorder.size() - 1).getX() + 20, HEIGHT - BRICKS));
        }
    }

    public void newGame() {
        disappear = false;
        topBorder.clear();
        botBorder.clear();
        missiles.clear();
        smoke.clear();
        topScore = Math.max(topScore, player.getScore() * 3);
        prefs.edit().putInt("Top Score", topScore).apply();
        player.resetScore();
        player.resetDY();
        player.setY(HEIGHT / 2);
        for (int i = 0; (i * 20) < (WIDTH + 40); i++) {
            topBorder.add(new Border(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    i * 20, 0));
            botBorder.add(new Border(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    i * 20, HEIGHT - BRICKS));
        }
        readyToPlay = false;
    }

    public void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setTextSize(30);
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE = " + player.getScore() * 3, 10, BRICKS + 25, paint);
        canvas.drawText("BEST = " + topScore, WIDTH - 215, BRICKS + 25, paint);

        if (!player.getPlaying() && reset && !readyToPlay) {
            Paint paint1 = new Paint();
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH / 2 - 50, HEIGHT / 2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH / 2 - 50, HEIGHT / 2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH / 2 - 50, HEIGHT / 2 + 40, paint1);
        }
    }
}