package com.example.brickbreaker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.os.Handler;
import java.util.Random;


public class GameView extends View {
    // Variables for the game context, ball, paddle, and more
    Context context;
    float ballX, ballY;
    Velocity velocity = new Velocity(24, 32);
    Handler handler;
    final long UPDATE_MILLIS = 30;
    Runnable runnable;
    Paint healthPaint = new Paint();
    Paint brickPaint = new Paint();
    Paint textPaint = new Paint();
    float TEXT_SIZE = 120;
    float paddleX, paddleY;
    float oldX, oldPaddleX;
    int life = 3;
    Bitmap ball, paddle;
    int dWidth, dHeight;
    int ballWidth, ballHeight;
    MediaPlayer mpHit, mpMiss, mpBreak;
    Random random;

    //hashmap for brick storage
    BrickMap brickMap = new BrickMap();
    int numBrick = 0;
    int brokenBricks = 0;
    boolean gameOver = false;

    VelocityManager velocityManager;

    //Point Array
    int[] pointsArray = new int[240]; // Array to hold points, assuming a maximum of 100 points
    int pointsIndex = 0;
    int points;


    public GameView(Context context) {
        super(context);
        this.context = context;
        ball = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
        paddle = BitmapFactory.decodeResource(getResources(), R.drawable.paddle);
        handler = new Handler();

        createBricks();

        velocityManager = new VelocityManager();

        // Runnable for updating the game view
        runnable = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };

        // Initialize MediaPlayer for sound effects and paint for drawing on the canvas
        mpHit = MediaPlayer.create(context, R.raw.hit);
        mpMiss = MediaPlayer.create(context, R.raw.miss);
        mpBreak = MediaPlayer.create(context, R.raw.breaking);
        textPaint.setColor(Color.RED);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTextAlign(Paint.Align.LEFT);
        healthPaint.setColor(Color.GREEN);
        brickPaint.setColor(Color.argb(255, 249, 129, 0));
        Display display = ((Activity) getContext()).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        dWidth = size.x;
        dHeight = size.y;
        random = new Random();
        ballX = random.nextInt(dWidth - 50);
        ballY = dHeight / 3;
        paddleY = (dHeight * 4) / 5;
        paddleX = dWidth / 2 - paddle.getWidth() / 2;
        ballWidth = ball.getWidth();
        ballHeight = ball.getHeight();
        createBricks();
    }

    // Get screen dimensions
    private void createBricks() {
        int brickWidth = dWidth / 8;
        int brickHeight = dHeight / 16;
        for (int column = 0; column < 8; column++) {
            for (int row = 0; row < 3; row++) {
                int key = numBrick; // Use numBrick as the key for each brick
                Brick newBrick = new Brick(row, column, brickWidth, brickHeight);
                brickMap.put(key, newBrick); // Store the brick in the BrickMap with its key
                numBrick++;
            }
        }
    }

    // Method to add points to the array
    private void addPoints(int value) {
        if (pointsIndex < pointsArray.length) {
            pointsArray[pointsIndex] = value;
            pointsIndex++;
        }
    }

    //Point storing

    // Method to calculate the total points from the array
    private int calculateTotalPoints() {
        int total = 0;
        for (int i = 0; i < pointsIndex; i++) {
            total += pointsArray[i];
        }
        return total;
    }

    // Method to update the points by adding to the array
    private void updatePoints(int value) {
        addPoints(value); // Add new points to the array
        points = calculateTotalPoints(); // Update points with the total from the array
    }



    // Method for creating bricks in the game
    @Override
    protected void onDraw(Canvas canvas) {
        // The main game loop where everything is drawn and updated
        //Game Loop
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);
        ballX += velocity.getX();
        ballY += velocity.getY();
        if((ballX >= dWidth - ball.getWidth()) || ballX <= 0){
            velocity.setX(velocity.getX() * -1);
        }
        if(ballY <= 0){
            velocity.setY(velocity.getY() * -1);
        }
        if(ballY > paddleY + paddle.getHeight()) {
            ballX = 1 + random.nextInt(dWidth - ball.getWidth() - 1);
            ballY = dHeight / 3;
            if (mpMiss != null) {
                mpMiss.start();
            }
            velocity.setX(xVelocity());
            velocity.setY(32);
            life--;

            if (life == 0) {
                gameOver = true;
                launchGameOver();
            }

            if (life == 1) {
                velocityManager.increaseVelocity(5, 5);
            }else if (life == 2) {
                velocityManager.increaseVelocity(50, 50);
            }else {
                velocityManager.increaseVelocity(100, 100);
            }
        }


        //hitting paddle algo
        //collision detection
            if(((ballX + ball.getWidth()) >= paddleX)
            && (ballX <= paddleX + paddle.getWidth())
            && (ballY + ball.getHeight() >= paddleY)
            && (ballY + ball.getHeight() <= paddleY + paddle.getHeight())){
                if (mpHit != null){
                    mpHit.start();
                }
                velocity.setX(velocity.getX() + 1);
                velocity.setY((velocity.getY() + 1) * -1);
            }
            canvas.drawBitmap(ball, ballX, ballY, null);
            canvas.drawBitmap(paddle, paddleX, paddleY, null);

        for (int i = 0; i < brickMap.size(); i++) {
            Brick currentBrick = brickMap.get(i);
            if (currentBrick != null && currentBrick.getVisibility()) {
                canvas.drawRect(
                        currentBrick.column * currentBrick.width + 1,
                        currentBrick.row * currentBrick.height + 1,
                        (currentBrick.column + 1) * currentBrick.width - 1,
                        (currentBrick.row + 1) * currentBrick.height - 1,
                        brickPaint
                );
            }
        }
            //life color change
//            canvas.drawText("" + points, 20, TEXT_SIZE, textPaint);
        canvas.drawText("" + calculateTotalPoints(), 20, TEXT_SIZE, textPaint);
        if(life == 2){
                healthPaint.setColor(Color.YELLOW);
            }else  if (life == 1){
                healthPaint.setColor(Color.RED);
            }
            canvas.drawRect(dWidth-2, 30, dWidth-200 + 60 * life, 80, healthPaint);

            // ball hitting brick algo
            //collision detection
            for(int i=0; i<numBrick; i++){
                if(brickMap.get(i).getVisibility()){
                    if (ballX + ballWidth >= brickMap.get(i).column * brickMap.get(i).width
                    && ballX <= brickMap.get(i).column * brickMap.get(i).width + brickMap.get(i).width
                    && ballY <= brickMap.get(i).row * brickMap.get(i).height + brickMap.get(i).height
                    && ballY >= brickMap.get(i).row * brickMap.get(i).height){
                        if(mpBreak != null){
                            mpBreak.start();
                        }
                        velocity.setY((velocity.getY() + 1) * -1);
                        brickMap.get(i).setInvisible();
                        updatePoints(10);
                        brokenBricks++;

                        if(brokenBricks == 24){
                            launchGameOver();
                        }
                    }
                }
            }

            if(brokenBricks == numBrick){
                gameOver = true;
            }
            if(!gameOver){
                handler.postDelayed(runnable, UPDATE_MILLIS);
            }

        if(brokenBricks == numBrick){
            gameOver = true;
            launchGameOver();
        }
        }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //handling user input
        // Handle touch events for controlling the paddle
        float touchX = event.getX();
        float touchY = event.getY();
        if(touchY >= paddleY){
            int action = event.getAction();
            if(action == MotionEvent.ACTION_DOWN){
                oldX = event.getX();
                oldPaddleX = paddleX;
            }
            if(action == MotionEvent.ACTION_MOVE){
                float shift = oldX - touchX;
                float newPaddleX = oldPaddleX - shift;
                if(newPaddleX <= 0){
                    paddleX = 0;
                }else if(newPaddleX >= dWidth - paddle.getWidth()){
                    paddleX = dWidth - paddle.getWidth();
                }else {
                    paddleX = newPaddleX;
                }
            }
        }
        return true;
    }

    private void launchGameOver(){
        handler.removeCallbacksAndMessages(null);

        Intent intent = new Intent(context, GameOver.class);
        intent.putExtra("points", calculateTotalPoints());
        context.startActivity(intent);
        ((Activity) context).finish();
    }

    private int xVelocity() {
        // Generate random horizontal velocity for the ball
        int[] values = {-35, -30, -25, 25, 30, 35};
        int index = random.nextInt(6);
        return values[index];
    }
}


