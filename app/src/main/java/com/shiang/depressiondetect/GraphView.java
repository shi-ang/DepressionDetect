package com.shiang.depressiondetect;

import android.util.AttributeSet;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import java.util.ArrayList;

/**
 * Handles the rendering of the graph view at the bottom
 */
public class GraphView extends View {
    /**
     * Paint object used to color the lines
     */
    Paint paint = new Paint();
    /**
     * Array list containing one of the voice's happiness values
     */
    public ArrayList<Float> facialValues = new ArrayList<Float>();
    public ArrayList<Float> voiceValues = new ArrayList<Float>();

    /**
     * The maximum number of points on the graph the more points the more detail the graph and also the lower the performance
     */
    public static int MaxNumberOfGraphPoints = 300;

    /**
     * Required initializer
     *
     * @param context Context object of the activity
     */
    public GraphView(Context context) {
        super(context);
    }

    /**
     * Initial population of the happiness arrays
     */
    public void populateEmptyArray() {
        for (int i = 0; i < MaxNumberOfGraphPoints; i++) {
            facialValues.add(-1.f);
            voiceValues.add(-1.f);
        }
    }

    /**
     * Required initializer
     *
     * @param context The context of the activity
     * @param attrs   The attributes of the activity
     */
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        populateEmptyArray();
    }

    /**
     * Required initializer
     *
     * @param context  The context of the activity
     * @param attrs    The attributes of the activity
     * @param defStyle The style def of the activity
     */
    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Returns the minimum X value required for drawing the graph. As the list gets more and more points the front ones are poped out one by one so the list always renders MaxNumberOfGraphPoints
     *
     * @param arrayList
     * @return
     */
    public int getMinXValue(ArrayList<Float> arrayList) {
        Boolean done = false;
        int i = 0;
        while (!done && i < arrayList.size()) {
            if (arrayList.get(i) >= 0.f) {
                done = true;
            } else {
                i += 1;
            }
        }
        return i;
    }

    /**
     * Draws the graph lines based on the happiness values
     *
     * @param canvas          The canvas object used for drawing
     * @param happinessValues The happinesvalues used for drawing the lines
     */
    private void drawGraph(Canvas canvas, ArrayList<Float> happinessValues) {

        int index = getMinXValue(happinessValues);
        if (index < happinessValues.size()) {
            float point = happinessValues.get(index);
            float pointX1 = (float) getWidth() * ((float) index / (float) happinessValues.size());
            float pointY1 = (float) ((float) getHeight() * (0.5 - 0.5 * point));

            float pointX2 = 0.f;
            float pointY2 = 0.f;

            int element = 1;

            while (element < happinessValues.size()) {
                point = happinessValues.get(element);
                if (point >= 0.f) {
                    pointX2 = (float) getWidth() * ((float) element / (float) happinessValues.size());
                    pointY2 = (float) ((float) getHeight() * (0.5 - 0.5 * point));

                    canvas.drawLine(pointX1, pointY1, pointX2, pointY2, paint);
                    pointX1 = pointX2;
                    pointY1 = pointY2;
                }


                element++;
            }
        }
    }

    /**
     * Draws the lines on the canvas
     *
     * @param canvas The canvas object that draws the lines
     */
    @Override
    public void onDraw(Canvas canvas) {

        canvas.drawColor(Color.TRANSPARENT);
        paint.setStrokeWidth((float) 1.0); // set the line width
        paint.setColor(Color.RED);
        drawGraph(canvas, facialValues);
        paint.setColor(Color.BLUE);
        drawGraph(canvas, voiceValues);
    }

}