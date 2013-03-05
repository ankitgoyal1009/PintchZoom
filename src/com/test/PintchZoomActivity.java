package com.test;

/*import android.app.Activity;
import android.os.Bundle;

public class PintchZoomActivity extends Activity {
    *//** Called when the activity is first created. *//*
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
}*/
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

public class PintchZoomActivity extends Activity implements OnTouchListener {
private static final String TAG = "Touch";

private static final float MIN_ZOOM = 1.0f;
private static final float MAX_ZOOM = 5.0f;

// These matrices will be used to move and zoom image
Matrix matrix = new Matrix();
Matrix savedMatrix = new Matrix();

// We can be in one of these 3 states
static final int NONE = 0;
static final int DRAG = 1;
static final int ZOOM = 2;
int mode = NONE;

// Remember some things for zooming
PointF start = new PointF();
PointF mid = new PointF();
float oldDist = 1f;
ImageView view;
ImageView view2;

@Override
public void onCreate(Bundle savedInstanceState) {
   super.onCreate(savedInstanceState);
  // setContentView(R.layout.main);
   /* view = (ImageView) findViewById(R.id.imageView);
    view2 = (ImageView)findViewById(R.id.imageView2);
   //view.setLayoutParams(new GridView.LayoutParams(85, 85));
   view.setScaleType(ImageView.ScaleType.FIT_CENTER);
   view.setOnTouchListener(this); 
   view2.setOnTouchListener(this); */
   TouchExampleView view = new TouchExampleView(this);
   view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
           ViewGroup.LayoutParams.MATCH_PARENT));
  
   setContentView(view);

}

public boolean onTouch(View v, MotionEvent event) {
   ImageView view = (ImageView) v;
   view.setScaleType(ImageView.ScaleType.MATRIX);
   float scale;

   // Dump touch event to log
   dumpEvent(event);

   // Handle touch events here...
   switch (event.getAction() & MotionEvent.ACTION_MASK) {

   case MotionEvent.ACTION_DOWN: //first finger down only
      savedMatrix.set(matrix);
      start.set(event.getX(), event.getY());
      Log.d(TAG, "mode=DRAG" );
      mode = DRAG;
      break;
   case MotionEvent.ACTION_UP: //first finger lifted
   case MotionEvent.ACTION_POINTER_UP: //second finger lifted
      mode = NONE;
      Log.d(TAG, "mode=NONE" );
      break;
   case MotionEvent.ACTION_POINTER_DOWN: //second finger down
      oldDist = spacing(event);
      Log.d(TAG, "oldDist=" + oldDist);
      if (oldDist > 5f) {
         savedMatrix.set(matrix);
         midPoint(mid, event);
         mode = ZOOM;
         Log.d(TAG, "mode=ZOOM" );
      }
      break;

   case MotionEvent.ACTION_MOVE: 
      if (mode == DRAG) { //movement of first finger
         matrix.set(savedMatrix);
         if (view.getLeft() >= -39){
            matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
         }
      }
      else if (mode == ZOOM) { //pinch zooming
         float newDist = spacing(event);
         Log.d(TAG, "newDist=" + newDist);
         if (newDist > 5f) {
            matrix.set(savedMatrix);
            scale = newDist / oldDist; //thinking i need to play around with this value to limit it**
            matrix.postScale(scale, scale, mid.x, mid.y);
         }
      }
      break;
   }
limitZoom(matrix);
limitDrag(matrix);
   // Perform the transformation
  // view.setImageMatrix(matrix);
applyzoomOnAllImages(matrix);
   return true; // indicate event was handled
}
void applyzoomOnAllImages(Matrix matrix){
	 view.setImageMatrix(matrix);
	 view2.setImageMatrix(matrix);
}
private void limitZoom(Matrix m) {

    float[] values = new float[9];
    m.getValues(values);
    float scaleX = values[Matrix.MSCALE_X];
    float scaleY = values[Matrix.MSCALE_Y];
    if(scaleX > MAX_ZOOM) {
        scaleX = MAX_ZOOM;
    } else if(scaleX < MIN_ZOOM) {
        scaleX = MIN_ZOOM;
    }

    if(scaleY > MAX_ZOOM) {
        scaleY = MAX_ZOOM;
    } else if(scaleY < MIN_ZOOM) {
        scaleY = MIN_ZOOM;
    }

    values[Matrix.MSCALE_X] = scaleX;
    values[Matrix.MSCALE_Y] = scaleY; 
    m.setValues(values);
}
private void limitDrag(Matrix m) {
    float[] values = new float[9];
    m.getValues(values);
    float transX = values[Matrix.MTRANS_X];
    float transY = values[Matrix.MTRANS_Y];
    float scaleX = values[Matrix.MSCALE_X];
    float scaleY = values[Matrix.MSCALE_Y];

    ImageView iv = (ImageView)findViewById(R.id.imageView);
   
    Rect bounds = iv.getDrawable().getBounds();
    int viewWidth = getResources().getDisplayMetrics().widthPixels;
    int viewHeight = getResources().getDisplayMetrics().heightPixels;

    int width = bounds.right - bounds.left;
    int height = bounds.bottom - bounds.top;
int offsetX = 20;
int offsetY = 80;
    float minX = (-width + 20) * scaleX; 
    float minY = (-height + 20) * scaleY;
    float maxX = minX+viewWidth+offsetX; 
    float maxY = minY+viewHeight-offsetY;
    Log.d(TAG, "minX:"+minX);
    Log.d(TAG, "maxX:"+maxX);
    Log.d(TAG, "minY:"+minY);
    Log.d(TAG, "maxY:"+maxY);
    if(transX > (maxX)) {
        //transX = viewWidth - 20;
    	Log.d(TAG, "transX >");
    	transX = maxX;
    	} else if(transX < minX) {
    		Log.d(TAG, "transX <");
    		transX = minX;
    }

    if(transY > (maxY)) {
//        transY = viewHeight - 80;
    	Log.d(TAG, "transY >");
        transY = maxY;
    } else if(transY < minY) {
        transY = minY;
        Log.d(TAG, "transY <");
    }

    values[Matrix.MTRANS_X] = transX;
    values[Matrix.MTRANS_Y] = transY; 
    m.setValues(values);
}
private float spacing(MotionEvent event) {
   float x = event.getX(0) - event.getX(1);
   float y = event.getY(0) - event.getY(1);
   return FloatMath.sqrt(x * x + y * y);
}

private void midPoint(PointF point, MotionEvent event) {
   float x = event.getX(0) + event.getX(1);
   float y = event.getY(0) + event.getY(1);
   point.set(x / 2, y / 2);
}

/** Show an event in the LogCat view, for debugging */
private void dumpEvent(MotionEvent event) {
   String names[] = { "DOWN" , "UP" , "MOVE" , "CANCEL" , "OUTSIDE" ,
      "POINTER_DOWN" , "POINTER_UP" , "7?" , "8?" , "9?" };
   StringBuilder sb = new StringBuilder();
   int action = event.getAction();
   int actionCode = action & MotionEvent.ACTION_MASK;
   sb.append("event ACTION_" ).append(names[actionCode]);
   if (actionCode == MotionEvent.ACTION_POINTER_DOWN
         || actionCode == MotionEvent.ACTION_POINTER_UP) {
      sb.append("(pid " ).append(
      action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
      sb.append(")" );
   }
   sb.append("[" );
   for (int i = 0; i < event.getPointerCount(); i++) {
      sb.append("#" ).append(i);
      sb.append("(pid " ).append(event.getPointerId(i));
      sb.append(")=" ).append((int) event.getX(i));
      sb.append("," ).append((int) event.getY(i));
      if (i + 1 < event.getPointerCount())
         sb.append(";" );
   }
   sb.append("]" );
   Log.d(TAG, sb.toString());
}
}