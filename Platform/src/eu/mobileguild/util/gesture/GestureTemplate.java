/*******************************************************************************
 * Copyright (c) 2011 Maksim Golivkin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package eu.mobileguild.util.gesture;

import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;

public abstract class GestureTemplate  {
	
	protected float spacing(MotionEvent event) {
	   float x = event.getX(0) - event.getX(1);
	   float y = event.getY(0) - event.getY(1);
	   return FloatMath.sqrt(x * x + y * y);
	}

	protected float twoFingerVectorSin(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
	
		return y / FloatMath.sqrt(x*x + y*y);
	}
	
	/** Show an event in the LogCat view, for debugging */
	protected void dumpEvent(MotionEvent event) {
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
	   
	   sb.append(" historySize() = ");
	   sb.append(event.getHistorySize());
	   
	   Log.d(this.getClass().getSimpleName(), sb.toString());
	}
	
	public float calculateDistance(MotionEvent e1,  float previousX, float previousY) {
		
		float x = e1.getX(0) - previousX;
		float y = e1.getY(0) - previousY;
		
		return FloatMath.sqrt(x * x + y * y);
	}
	
	public boolean compare(MotionEvent e1, float previousX, float previousY) {
		return 
			e1.getX() == previousX &&
			e1.getY() == previousY;
	}
	
	protected float twoEventsCos(MotionEvent event) {
		float x = event.getX(0) - event.getHistoricalX(0);
		float y = event.getY(0) - event.getHistoricalY(0);
	
		return x / FloatMath.sqrt(x*x + y*y);
	}


}
