package org.hcilab.circog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class MOTActivity extends AppCompatActivity {

    public static final int TASK_ID = 3;
    private static final String	TAG	= MOTActivity.class.getSimpleName();

    private final static int NUM_CIRCLES = 8;
    private final static int NUM_CIRCLE_TARGETS = 4;
    private final static int FRAME_RATE = 5;
    private final static int CIRCLE_SPEED = 5;
    private final static long TASK_DURATION = 10000; //ms
    private final static long TASK_START_DELAY = 2000; //ms
    private final static int MAX_TASKS = 5; //5
    private final static int MAX_OVERLAP_LIMIT = 50; //maximum number of overlaps when trying to stop the animation

    private int radius;
    private boolean taskCompleted;
    private int selectedTargets;
    private int taskCount;
    private int correctSelections;
    private int totalTargetsSeen;
    private boolean allTasksCompleted;
    private long startTasksTime;
    private long endTasksTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(this));

        //make sure screen keeps turned on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //check whether daily survey has been answered yet
        Date lastDailySurveyTaken = new Date(Util.getLong(getApplicationContext(), CircogPrefs.DATE_LAST_DAILY_SURVEY_MS, 0));
        Date now = new Date(System.currentTimeMillis());
        boolean dailySurveyAnswered = Util.isSameDay(lastDailySurveyTaken, now);
        if(!dailySurveyAnswered) {
            startActivity(new Intent(getApplicationContext(), DailySurveyActivity.class));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(!allTasksCompleted) {
            int alertness = Util.getInt(this, CircogPrefs.LEVEL_ALERTNESS, -1);
            boolean caffeinated = Util.getBool(this, CircogPrefs.CAFFEINATED, false);
            endTasksTime = System.currentTimeMillis();
            LogManager.logMOT(taskCount, correctSelections, totalTargetsSeen, startTasksTime, endTasksTime, allTasksCompleted, alertness, caffeinated);
        }
    }

    class MyView extends View {

        private ArrayList<Circle> circles;
        private int canvasWidth;
        private int canvasHeight;
        private int overlapCount; //makes sure that there is a maximum number of overlaps in order to not stall the app forever

        //manages the highlight phase
        private Handler blinkTaskHandler = new Handler();
        private Runnable blinkTRunnable = new Runnable() {

            @Override
            public void run() {
                //make targets blink
                for(int i=0; i<NUM_CIRCLES; i++) {
                    Circle circle = circles.get(i);
                    if (circle.isTarget) {
                        circle.blink();
                    }
                }
            }
        };

        //manages the next task start
        private Handler nextStartTaskHandler = new Handler();
        private Runnable nextStartTRunnable = new Runnable() {

            @Override
            public void run() {
                initTask();
            }
        };

        //manages the task start
        private Handler startTaskHandler = new Handler();
        private Runnable startTRunnable = new Runnable() {

            @Override
            public void run() {
                //make targets blink
                for(int i=0; i<NUM_CIRCLES; i++) {
                    Circle circle = circles.get(i);
                    circle.moving = true;
                }
                taskCompleted = false;
                deselectCircles();
            }
        };

        //manages the end of the task
        private Handler stopTaskHandler = new Handler();
        private Runnable stopTRunnable = new Runnable() {

            @Override
            public void run() {
                //make targets blink
                if (!circlesOverlap() || overlapCount > MAX_OVERLAP_LIMIT) {
                    for (int i = 0; i < NUM_CIRCLES; i++) {
                        Circle circle = circles.get(i);
                        circle.moving = false;
                    }
                    taskCompleted = true;
                } else {
                    //re-schedule in case of overlaps
                    long now = System.currentTimeMillis();
                    long delay = 250; //ms
                    stopTaskHandler.postAtTime(stopTRunnable, now + delay);
                    stopTaskHandler.postDelayed(stopTRunnable, delay);
                }
            }
        };

        public MyView(Context context) {
            super(context);
            correctSelections = 0;
            totalTargetsSeen = 0;
            allTasksCompleted = false;
            overlapCount = 0;
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldW, int oldH) {
            canvasWidth = width;
            canvasHeight = height;
            if(circles==null || circles.size()==0) {
                initTask();
            }
        }

        protected void onDraw(Canvas c) {
            if(circles!=null) {
                for (int i = 0; i < circles.size(); i++) {

                    int w = getWidth();
                    int h = getHeight();
                    int rightLimit = w - radius;
                    int bottomLimit = h - radius;
                    Circle circle = circles.get(i);

                    //move
                    if (circle.moving) {

                        if (circle.centerX >= rightLimit) {
                            circle.centerX = rightLimit;
                            circle.angle = (circle.angle + Math.PI/2)%(Math.PI*2);

                        }
                        if (circle.centerX <= radius) {
                            circle.centerX = radius;
                            circle.angle = (circle.angle + Math.PI/2)%(Math.PI*2);
                        }
                        if (circle.centerY >= bottomLimit) {
                            circle.centerY = bottomLimit;
                            circle.angle = (circle.angle + Math.PI/2)%(Math.PI*2);
                        }
                        if (circle.centerY <= radius) {
                            circle.centerY = radius;
                            circle.angle = (circle.angle + Math.PI/2)%(Math.PI*2);
                        }

                        int dx = (int) (circle.speed * Math.cos(circle.angle));
                        int dy = (int) (circle.speed * Math.sin(circle.angle));

                        circle.centerX += dx;
                        circle.centerY += dy;

                    }
                    circle.draw(c);
                }
            }
            postInvalidateDelayed(FRAME_RATE);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            if(taskCompleted) {
                for(int i=0; i<circles.size(); i++) {
                    Circle circle = circles.get(i);
                    if(circle.contains(event.getX(), event.getY())) {
                        Log.i(TAG, "Circle touched");
                        if(circle.selected) {
                            circle.selected = false;
                            selectedTargets-=1;
                        } else {
                            circle.selected = true;
                            selectedTargets+=1;
                        }
                        break;
                    }
                }
                if(selectedTargets==NUM_CIRCLE_TARGETS) {
                    evaluateSelection();
                }
            }

            return false;
        }

        private void initTask() {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            String instructions;
            if(taskCount==0) {
                instructions = String.format(getString(R.string.mot_explanation), NUM_CIRCLE_TARGETS);
            } else {
                int roundsLeft = MAX_TASKS-taskCount;
                if(roundsLeft==1) {
                    instructions = String.format(getString(R.string.mot_last_task), roundsLeft);
                } else {
                    instructions = String.format(getString(R.string.mot_next_task), roundsLeft);
                }
            }
            alertDialogBuilder.setMessage(instructions);
            alertDialogBuilder.setPositiveButton(getString(R.string.pvt_dialog_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    circles = new ArrayList<Circle>();
                    radius = canvasWidth/20;
                    for(int i=0; i<NUM_CIRCLES; i++) {

                        //make sure circles don't overlap at time of creation
                        Circle circle;
                        do {
                            //make sure circles are created WITHIN canvas (i.e. centers are within canvas-radius
                            int startPosX = new Random().nextInt(canvasWidth-2*radius)+radius;
                            int startPosY = new Random().nextInt(canvasHeight-2*radius)+radius;
                            boolean isTarget = false;
                            double angle = Math.PI * 2 * Math.random(); // Math.PI * 2 = 360 degrees
                            circle = new Circle(startPosX, startPosY, CIRCLE_SPEED, angle, radius, isTarget);
                        }
                        while(circleOverlaps(circle));

                        if (i<NUM_CIRCLE_TARGETS) {
                            circle.isTarget = true;
                        }
                        circles.add(circle);
                    }

                    //schedule start, including blinking of targets
                    long now = System.currentTimeMillis();
                    blinkTaskHandler.postAtTime(blinkTRunnable, now + TASK_START_DELAY);
                    blinkTaskHandler.postDelayed(blinkTRunnable, TASK_START_DELAY);
                    if(CircogPrefs.DEBUG_MODE) {
                        Log.i(TAG, "+ blink scheduled in: " + TASK_START_DELAY);
                    }

                    long delay = TASK_START_DELAY + (Circle.BLINKING_DURATION * Circle.BLINK_FREQUENCY) * 2;
                    if(CircogPrefs.DEBUG_MODE) {
                        Log.i(TAG, "+ start scheduled in: " + delay);
                    }
                    startTaskHandler.postAtTime(startTRunnable, now + delay);
                    startTaskHandler.postDelayed(startTRunnable, delay);

                    //schedule stop
                    delay += TASK_DURATION;
                    stopTaskHandler.postAtTime(stopTRunnable, now + delay);
                    stopTaskHandler.postDelayed(stopTRunnable, delay);

                    startTasksTime = System.currentTimeMillis();
                }
            });
            alertDialogBuilder.show();

            //show alert assessments
            ArrayList<Integer> tasklist = TaskList.getTaskList(getApplicationContext());
            if(taskCount==0 && tasklist.size()==MainActivity.COMPLETE_TASKLIST.length) {
                final Intent intent = new Intent(getApplicationContext(), AlertnessSurveyActivity.class);
                startActivity(intent);
            }
        }

        /**
         * checks all circles for overlaps (overlap: center distance < 2*radius)
         * @return boolean overlap exists
         */
        private boolean circlesOverlap() {
            for(int i=0; i<NUM_CIRCLES; i++) {
                Circle c1 = circles.get(i);
                for(int j=0; j<NUM_CIRCLES; j++) {
                    Circle c2 = circles.get(j);
                    if(c1!=c2) {
                        double dist = Math.sqrt(Math.pow(c1.centerX - c2.centerX, 2) + Math.pow(c1.centerY - c2.centerY, 2));
                        if (dist < 2 * c1.radius) {
                            overlapCount++;
                            Log.i(TAG, "circle overlaps: " + Double.toString(dist) + " (" + Double.toString(2 * radius) + "), count: " + overlapCount);
                            return true;
                        }
                    }
                }

            }
            return false;
        }

        /**
         * checks whether a given circle overlaps with the existing circles
         * @param c1 reference circle
         * @return boolean whether circle overlaps with any other
         */
        private boolean circleOverlaps(Circle c1) {
            for(int i=0; i<circles.size(); i++) {
                Circle c2 = circles.get(i);
                double dist = Math.sqrt(Math.pow(c1.centerX - c2.centerX, 2) + Math.pow(c1.centerY- c2.centerY, 2));
                if(dist<2*c1.radius) {
                    if(CircogPrefs.DEBUG_MODE) {
                        Log.i(TAG, "circle overlaps: " + Double.toString(dist) + " (" + Double.toString(2*radius) + ")");
                    }
                    return true;
                }
            }
            return false;
        }

        private void deselectCircles() {
            for(int i=0; i<circles.size(); i++) {
                Circle circle = circles.get(i);
                circle.selected = false;
            }
            selectedTargets = 0;
        }

        private void evaluateSelection() {
            int correct = 0;

            //show result
            for(int i=0; i<circles.size(); i++) {
                Circle circle = circles.get(i);

                if(circle.isTarget && circle.selected) {
                    correct+=1;
                }

                if(circle.isTarget && !circle.selected) {
                    circle.blink(); //GREEN
                }
                if(circle.selected && !circle.isTarget) {
                    circle.blink(); //RED
                }
            }
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "# of correct target selections: " + correct + "/" + NUM_CIRCLE_TARGETS);
            }
            correctSelections+=correct;
            totalTargetsSeen+=NUM_CIRCLE_TARGETS;

            taskCount++;
            if(CircogPrefs.DEBUG_MODE) {
                endTasksTime = System.currentTimeMillis();
                showFinishScreen();
            }
            else if(taskCount < MAX_TASKS) {
                long delay;
                if(correct==NUM_CIRCLE_TARGETS) {
                    delay = 0;
                } else {
                    delay = TASK_START_DELAY + (Circle.BLINKING_DURATION * Circle.BLINK_FREQUENCY) * 2;
                }
                scheduleNextTask(delay);
            } else {
                endTasksTime = System.currentTimeMillis();
                showFinishScreen();
            }
        }

        /**
         * schedules the beginning of the next start (initTask() call)
         * @param delay
         */
        private void scheduleNextTask(long delay) {
            long now = System.currentTimeMillis();
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "+ start scheduled in: " + delay);
            }
            nextStartTaskHandler.postAtTime(nextStartTRunnable, now + delay);
            nextStartTaskHandler.postDelayed(nextStartTRunnable, delay);
        }

        /**
         * launches next task in tasklist and finishes current activity
         */
        private void launchNextTask() {
            int task = TaskList.getCurrentTask(getApplicationContext());
            if(task==PVTActivity.TASK_ID) {
                if(CircogPrefs.DEBUG_MODE) {
                    Log.i(TAG, "launchPVT()");
                }
                Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_PVT);
                final Intent intent = new Intent(getApplicationContext(), PVTActivity.class);
                startActivity(intent);
            }
            else if(task==GoNoGoActivity.TASK_ID) {
                if(CircogPrefs.DEBUG_MODE) {
                    Log.i(TAG, "launchGoNoGo()");
                }
                Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_GNG);
                final Intent intent = new Intent(getApplicationContext(), GoNoGoActivity.class);
                startActivity(intent);
            }
            else if(task==MOTActivity.TASK_ID) {
                if(CircogPrefs.DEBUG_MODE) {
                    Log.i(TAG, "launchMOT()");
                }
                Util.putString(getApplicationContext(), CircogPrefs.CURRENT_TASK, LogManager.KEY_MOT);
                final Intent intent = new Intent(getApplicationContext(), MOTActivity.class);
                startActivity(intent);
            }

            //check whether this is the last task
            ArrayList<Integer> tasklist = TaskList.getTaskList(getApplicationContext());
            if(tasklist.size()==0) {

                //increment DAILY_TASK_COUNT
                TaskList.incrementDailyTaskCount(getApplicationContext());

                //bye bye message
                Toast.makeText(MOTActivity.this, R.string.task_toast_finish, Toast.LENGTH_LONG).show();
            }
            finish();
        }

        private void showFinishScreen() {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
//            String txt = String.format(getString(R.string.mot_dialog_finish), taskCount, correctSelections, totalTargetsSeen);
            String txt = TaskList.getNextTask(getApplicationContext());

            alertDialogBuilder.setMessage(txt);

            //TODO: set icon
            alertDialogBuilder.setPositiveButton(getString(R.string.pvt_dialog_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //back to MainActivity
//                    finish();
                    launchNextTask();
                }
            });
            alertDialogBuilder.show();

            Log.i(TAG, "# of tasks: " + taskCount);
            Log.i(TAG, "# of correct selections: " + correctSelections + " out of " + totalTargetsSeen);

            allTasksCompleted = true;
            int alertness = Util.getInt(getApplicationContext(), CircogPrefs.LEVEL_ALERTNESS, -1);
            boolean caffeinated = Util.getBool(getApplicationContext(), CircogPrefs.CAFFEINATED, false);
            LogManager.logMOT(taskCount, correctSelections, totalTargetsSeen, startTasksTime, endTasksTime, allTasksCompleted, alertness, caffeinated);
        }
    }

    class Circle {

        private static final int CIRCLE_STROKE_COLOR = Color.BLACK;
        private static final int CIRCLE_BORDER_WIDTH = 8;
        private static final int CIRCLE_COLOR = Color.BLUE;
        private static final int CIRCLE_COLOR_INCORRECT = Color.RED;
        private static final int CIRCLE_COLOR_TARGET = Color.GREEN;
        private static final int BLINKING_DURATION = 300; //ms
        private static final int BLINK_FREQUENCY = 5; //number of times circles blink

        private Paint paint;
        private float radius;
        private int centerX;
        private int centerY;
        private double speed;
        private double angle; //in radians
        private boolean isTarget;
        private boolean moving;
        private boolean blinking = false;
        private boolean selected = false;

        //manages the blinking
        private Handler blinkHandler = new Handler();
        private Runnable blinkRunnable = new Runnable() {

            @Override
            public void run() {
                if(!blinking) {
                    blinking = true;
                } else {
                    blinking = false;
                }
            }
        };

        public Circle(int posX, int posY, double speed, double angle, float radius, boolean isTarget) {

            this.centerX = posX;
            this.centerY = posY;
            this.speed = speed;
            this.angle = angle;
            this.radius = radius;
            this.isTarget = isTarget;
            this.paint = new Paint();
            this.moving = false;

            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "Circle created at: " + this.centerX + ", " + this.centerY + ", speed: " + this.speed + ", angle: " + this.angle);
            }

        }

        public void draw (Canvas c) {

            int saveColor = this.paint.getColor();
            if(this.blinking && this.selected && !this.isTarget) { //RED for false selections
                this.paint.setColor(CIRCLE_COLOR_INCORRECT);
            }
            else if(this.blinking || this.selected) {
                this.paint.setColor(CIRCLE_COLOR_TARGET); //GREEN for blinking targets
            }
            else {
                this.paint.setColor(CIRCLE_COLOR);//DEFAULT
            }
            Paint.Style saveStyle = this.paint.getStyle();
            this.paint.setStyle(Paint.Style.FILL);
            c.drawCircle(this.centerX, this.centerY, radius, this.paint);
            if (CIRCLE_BORDER_WIDTH > 0) {
                this.paint.setColor(CIRCLE_STROKE_COLOR);
                this.paint.setStyle(Paint.Style.STROKE);
                float saveStrokeWidth = this.paint.getStrokeWidth();
                this.paint.setStrokeWidth(CIRCLE_BORDER_WIDTH);
                c.drawCircle(this.centerX, this.centerY, radius - (CIRCLE_BORDER_WIDTH / 2), this.paint);
                this.paint.setStrokeWidth(saveStrokeWidth);
            }
            this.paint.setColor(saveColor);
            this.paint.setStyle(saveStyle);
        }


        public void blink() {
            long now = System.currentTimeMillis();

            //highlight
            blinkHandler.postDelayed(blinkRunnable, 0);

            int dehighlightDelay = BLINKING_DURATION;
            int highlightDelay = BLINKING_DURATION * 2;
            for(int i=1; i<BLINK_FREQUENCY; i++) {

                //de-highlight
                blinkHandler.postAtTime(blinkRunnable, now + dehighlightDelay);
                blinkHandler.postDelayed(blinkRunnable, dehighlightDelay);
                dehighlightDelay+=BLINKING_DURATION * 2;

                //highlight
                blinkHandler.postAtTime(blinkRunnable, now + highlightDelay);
                blinkHandler.postDelayed(blinkRunnable, highlightDelay);
                highlightDelay+=BLINKING_DURATION * 2;
            }

            //finally de-highlight
            if(CircogPrefs.DEBUG_MODE) {
                Log.i(TAG, "final de-highlight set at: " + dehighlightDelay);
            }
            blinkHandler.postAtTime(blinkRunnable, now + dehighlightDelay);
            blinkHandler.postDelayed(blinkRunnable, dehighlightDelay);
        }

        public boolean contains(float posX, float posY)
        {
            float distanceX = this.centerX - posX;
            float distanceY = this.centerY - posY;

            return Math.sqrt((distanceX * distanceX) + (distanceY * distanceY)) <= this.radius;
        }
    }
}
