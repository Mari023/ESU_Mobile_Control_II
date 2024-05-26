/*
 Rocrail - Model Railroad Software

 Copyright (C) 2002-2011 - Rob Versluis <r.j.versluis@rocrail.net>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package net.rocrail.android.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class Slider extends View implements Runnable {
    double V = 0.0;
    double Range = 100.0;
    boolean isDown = false;
    boolean isMin = false;
    boolean downHandled = false;
    int sleep = 1000;

    List<SliderListener> m_Listeners = new ArrayList<>();

    public Slider(Context context) {
        super(context);
    }

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);
        new Thread(this).start();
    }

    public void addListener(SliderListener listener) {
        m_Listeners.add(listener);
    }

    void informListeners() {
        double realV = (V * Range) / 100.0;
        for (SliderListener listener : m_Listeners) {
            listener.onSliderChange(this, (int) realV);
        }
    }

    public void setRange(float range) {
        Range = range;
    }

    public void setV(int v) {
        V = (v * 100.0) / Range;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        double cx = getWidth();
        double cy = getHeight();
        double xu = cx / 10.0;
        double yu = cy / 10.0;


        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.rgb(170, 170, 170));

        RectF rect = new RectF();
        double y1 = 4.5 * yu;
        double x1 = .75 * xu;
        double y2 = 5.5 * yu;
        double x2 = 9.25 * xu;
        rect.left = (float) x1;
        rect.right = (float) x2;
        rect.top = (float) y1;
        rect.bottom = (float) y2;
        canvas.drawRoundRect(rect, (float) yu / 3, (float) yu / 3, paint);

        float x = (float) (.75 * xu + (V * xu * 8.5) / 100.0);

        rect.left = (float) (x - .75 * xu);
        rect.right = (float) (x + .75 * xu);
        rect.top = (float) yu * 1;
        rect.bottom = (float) yu * 9;

        paint.setColor(Color.rgb(70, 70, 70));
        canvas.drawRoundRect(rect, (float) yu / 2, (float) yu / 2, paint);

        rect.left = (float) (x - .75 * xu) + 2;
        rect.right = (float) (x + .75 * xu) - 2;
        rect.top = (float) yu * 1 + 2;
        rect.bottom = (float) yu * 9 - 2;

        paint.setColor(Color.rgb((int) V + 120, 120, 120));
        canvas.drawRoundRect(rect, (float) yu / 2, (float) yu / 2, paint);
        paint.setColor(Color.rgb(100, 100, 100));


        float ly1 = (float) (yu * 1.2);
        float ly2 = (float) (yu * 8.6);
        float lx1 = (float) ((1.5 * xu) / 5);

        double d = x - .75 * xu;
        canvas.drawLine((float) d + lx1 * 1, ly1, (float) d + lx1 * 1, ly2, paint);
        canvas.drawLine((float) d + lx1 * 2, ly1, (float) d + lx1 * 2, ly2, paint);
        canvas.drawLine((float) d + lx1 * 3, ly1, (float) d + lx1 * 3, ly2, paint);
        canvas.drawLine((float) d + lx1 * 4, ly1, (float) d + lx1 * 4, ly2, paint);

    }

    void adjustV() {
        if (isMin) {
            if (V > 0) V --;
            if (V < 0) V = 0;
        } else {
            if (V < 100) V --;
            if (V > 100) V = 100;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        double cx = getWidth();
        double xu = cx / 10.0;

        double x = event.getX();
        if (x < .75 * xu) x = .75 * xu;
        if (x > 9.25 * xu) x = 9.25 * xu;
        V = 100 * ((x - (.75 * xu)) / (8.5 * xu));

        invalidate();
        informListeners();
        return true;
    }

    @Override
    public void run() {
        do {
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException ignored) {
            }
            if (sleep == 100 && isDown) {
                sleep = 1000;
                continue;
            }
            if (isDown) {
                if (sleep > 500) {
                    sleep -= 100;
                }
                adjustV();
                downHandled = true;
                Slider.this.post(() -> {
                    informListeners();
                    invalidate();
                });
            }
        } while (true);
    }
}
