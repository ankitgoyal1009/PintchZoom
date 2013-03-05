package com.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

public class TouchExampleView extends View {
	private static final String TAG = "TouchExampleView";
	private Drawable mIcon,mIcon2;
    private float mPosX;
    private float mPosY;
    Matrix matrix = new Matrix();
    private float mLastTouchX;
    private float mLastTouchY;
    private static final int INVALID_POINTER_ID = -1;

 // The ‘active pointer’ is the one currently moving our object.
 private int mActivePointerId = INVALID_POINTER_ID;
 private float mScaleFactor = 1.f;
 private ScaleGestureDetector mScaleDetector;

    public TouchExampleView(Context context) {
        this(context, null, 0);
    }
    
    public TouchExampleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public TouchExampleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mIcon = context.getResources().getDrawable(R.drawable.ic_launcher);
        mIcon.setBounds(0, 0, mIcon.getIntrinsicWidth(), mIcon.getIntrinsicHeight());
        mIcon2 = context.getResources().getDrawable(R.drawable.ic_launcher);
        mIcon2.setBounds(100, 0, mIcon2.getIntrinsicWidth(), mIcon2.getIntrinsicHeight());
     // Create our ScaleGestureDetector
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.save();
        canvas.translate(mPosX, mPosY);
        canvas.scale(mScaleFactor, mScaleFactor);

        mIcon.draw(canvas);
        mIcon2.draw(canvas);
        canvas.restore();
    }

@Override
public boolean onTouchEvent(MotionEvent ev) {
	mScaleDetector.onTouchEvent(ev);

    final int action = ev.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
    case MotionEvent.ACTION_DOWN: {
        final float x = ev.getX();
        final float y = ev.getY();
        
        mLastTouchX = x;
        mLastTouchY = y;

        // Save the ID of this pointer
        mActivePointerId = ev.getPointerId(0);
        break;
    }
        
    case MotionEvent.ACTION_MOVE: {
        // Find the index of the active pointer and fetch its position
    	final int pointerIndex = ev.findPointerIndex(mActivePointerId);
        final float x = ev.getX(pointerIndex);
        final float y = ev.getY(pointerIndex);

        // Only move if the ScaleGestureDetector isn't processing a gesture.
        if (!mScaleDetector.isInProgress()) {
            final float dx = x - mLastTouchX;
            final float dy = y - mLastTouchY;

            mPosX += dx;
            mPosY += dy;
//limit drag position(mPosX and mPosY to min and max values)here-Ankit
            
            invalidate();
        }
        limitDrag(mPosX,mPosY);
        mLastTouchX = x;
        mLastTouchY = y;

        break;

    }
        
    case MotionEvent.ACTION_UP: {
        mActivePointerId = INVALID_POINTER_ID;
        break;
    }
        
    case MotionEvent.ACTION_CANCEL: {
        mActivePointerId = INVALID_POINTER_ID;
        break;
    }
    
    case MotionEvent.ACTION_POINTER_UP: {
        // Extract the index of the pointer that left the touch sensor
        final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastTouchX = ev.getX(newPointerIndex);
            mLastTouchY = ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
        }
        break;
    }
    }
    
    return true;
}
private void limitDrag(float x, float y) {
	int viewWidth = getResources().getDisplayMetrics().widthPixels;
    int viewHeight = getResources().getDisplayMetrics().heightPixels;
    Rect bounds = mIcon.getBounds();
    
    int width = bounds.right - bounds.left;
    int height = bounds.bottom - bounds.top;
    
    int offsetX = 20;
    int offsetY = 80;
    float minX = (-width + 20) * mScaleFactor; 
    float minY = (-height + 20) * mScaleFactor;
   
    
    //float minX = 1*mScaleFactor; 
    //float minY = 1*mScaleFactor;
   
    float maxX = minX+viewWidth; 
    float maxY = minY+viewHeight-offsetY;
   if(x>maxX){
	   mPosX = maxX;
   }
   if(x<minX){
	   mPosX = minX;
   }
   if(y>maxY){
	   mPosY = maxY;
   }
   if(y<minY){
	   mPosY = minY;
   }
}

private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        mScaleFactor *= detector.getScaleFactor();
        
        // Don't let the object get too small or too large.
        mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));

        invalidate();
        return true;
    }
}
}
