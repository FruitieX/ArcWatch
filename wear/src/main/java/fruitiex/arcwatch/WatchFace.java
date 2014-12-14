package fruitiex.arcwatch;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WatchFace extends CanvasWatchFaceService {
    private static final String TAG = "AnalogWatchFaceService";
    float hourSize = 12.f;
    float minSize = 6.f;
    float lineSize = 2.f;
    float minOffs = 25;
    float hourOffs = 50;
    float textSize = 60;
    float smallTextSize = 20;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        Paint ambientHourPaint;
        Paint ambientMinutePaint;
        Paint activeHourPaint;
        Paint activeMinutePaint;
        Paint mTickPaint;
        Paint textPaint;
        Paint smallTextPaint;
        Time mTime;

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "onTimeTick: ambient = " + isInAmbientMode());
            }
            invalidate();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
        }
        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if(inAmbientMode) {
                textPaint.setStyle(Paint.Style.STROKE);
            } else {
                textPaint.setStyle(Paint.Style.FILL);
            }
            invalidate();
        }
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            invalidate();
        }

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setStatusBarGravity(Gravity.CENTER | Gravity.CENTER_HORIZONTAL)
                    .setPeekOpacityMode(WatchFaceStyle.PEEK_OPACITY_MODE_TRANSLUCENT)
                    .setShowSystemUiTime(false)
                    .build());

            ambientHourPaint = new Paint();
            ambientHourPaint.setARGB(255, 170, 160, 150);
            ambientHourPaint.setStrokeWidth(lineSize);
            ambientHourPaint.setStyle(Paint.Style.STROKE);
            ambientHourPaint.setAntiAlias(true);

            ambientMinutePaint = new Paint();
            ambientMinutePaint.setARGB(255, 150, 160, 170);
            ambientMinutePaint.setStrokeWidth(lineSize);
            ambientMinutePaint.setStyle(Paint.Style.STROKE);
            ambientMinutePaint.setAntiAlias(true);

            activeHourPaint = new Paint();
            activeHourPaint.setARGB(255, 170, 160, 150);
            activeHourPaint.setStrokeWidth(hourSize);
            activeHourPaint.setStyle(Paint.Style.STROKE);
            activeHourPaint.setAntiAlias(true);

            activeMinutePaint = new Paint();
            activeMinutePaint.setARGB(255, 150, 160, 170);
            activeMinutePaint.setStrokeWidth(minSize);
            activeMinutePaint.setStyle(Paint.Style.STROKE);
            activeMinutePaint.setAntiAlias(true);

            mTickPaint = new Paint();
            mTickPaint.setARGB(100, 255, 255, 255);
            mTickPaint.setStrokeWidth(lineSize);
            mTickPaint.setAntiAlias(true);

            textPaint = new Paint();
            textPaint.setARGB(160, 255, 255, 255);
            textPaint.setStrokeWidth(lineSize);
            textPaint.setAntiAlias(true);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(textSize);
            textPaint.setStyle(Paint.Style.FILL);
            
            smallTextPaint = new Paint();
            smallTextPaint.setARGB(160, 255, 255, 255);
            smallTextPaint.setStrokeWidth(lineSize);
            smallTextPaint.setAntiAlias(true);
            smallTextPaint.setTextAlign(Paint.Align.CENTER);
            smallTextPaint.setTextSize(smallTextSize);
            smallTextPaint.setStyle(Paint.Style.FILL);

            mTime = new Time();
        }

        private String formatTwoDigitNumber(int hour) {
            return String.format("%02d", hour);
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            canvas.drawColor(Color.BLACK);

            mTime.setToNow();

            int width = bounds.width();
            int height = bounds.height();

            // Find the center. Ignore the window insets so that, on round watches with a
            // "chin", the watch face is centered on the entire screen, not just the usable
            // portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            float innerX, innerY, outerX, outerY;

            // Draw the ticks.
            float innerTickRadius = centerX - 15;
            float outerTickRadius = centerX - 5;
            for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
                float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
                innerX = (float) Math.sin(tickRot) * innerTickRadius;
                innerY = (float) -Math.cos(tickRot) * innerTickRadius;
                outerX = (float) Math.sin(tickRot) * outerTickRadius;
                outerY = (float) -Math.cos(tickRot) * outerTickRadius;
                canvas.drawLine(centerX + innerX, centerY + innerY,
                        centerX + outerX, centerY + outerY, mTickPaint);
            }

            float minRot = mTime.minute      / 60f * 360;
            float hourRot =  (mTime.hour % 12) / 12f * 360;

            if(!isInAmbientMode()) {
                canvas.drawArc(new RectF(minOffs + minSize / 2, minOffs + minSize / 2, width - minOffs - minSize / 2, height - minOffs - minSize / 2), -90, minRot, false, activeMinutePaint);
                canvas.drawArc(new RectF(hourOffs + hourSize / 2, hourOffs + hourSize / 2, width - hourOffs - hourSize / 2, height - hourOffs - hourSize / 2), -90, hourRot, false, activeHourPaint);
            } else {
                /* minutes */
                if(minRot != 0) {
                    // draw the arcs
                    canvas.drawArc(new RectF(minOffs, minOffs, width - minOffs, height - minOffs), -90, minRot, false, ambientMinutePaint);
                    canvas.drawArc(new RectF(minOffs + minSize, minOffs + minSize, width - (minOffs + minSize), height - (minOffs + minSize)), -90, minRot, false, ambientMinutePaint);

                    // draw the "end caps" between the arcs
                    innerX = (float) Math.sin(minRot / 360f * 2 * Math.PI) * (centerX - minOffs - minSize);
                    innerY = (float) -Math.cos(minRot / 360f * 2 * Math.PI) * (centerX - minOffs - minSize);
                    outerX = (float) Math.sin(minRot / 360f * 2 * Math.PI) * (centerX - minOffs);
                    outerY = (float) -Math.cos(minRot / 360f * 2 * Math.PI) * (centerX - minOffs);
                    canvas.drawLine(centerX, minOffs, centerX, minOffs + minSize, ambientMinutePaint);
                    canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, ambientMinutePaint);
                }

                /* hours */
                if(hourRot != 0) {
                    // draw the arcs
                    canvas.drawArc(new RectF(hourOffs, hourOffs, width - hourOffs, height - hourOffs), -90, hourRot, false, ambientHourPaint);
                    canvas.drawArc(new RectF(hourOffs + hourSize, hourOffs + hourSize, width - (hourOffs + hourSize), height - (hourOffs + hourSize)), -90, hourRot, false, ambientHourPaint);

                    // draw the "end caps" between the arcs

                    innerX = (float) Math.sin(hourRot / 360f * 2 * Math.PI) * (centerX - hourOffs - hourSize);
                    innerY = (float) -Math.cos(hourRot / 360f * 2 * Math.PI) * (centerX - hourOffs - hourSize);
                    outerX = (float) Math.sin(hourRot / 360f * 2 * Math.PI) * (centerX - hourOffs);
                    outerY = (float) -Math.cos(hourRot / 360f * 2 * Math.PI) * (centerX - hourOffs);
                    canvas.drawLine(centerX, hourOffs, centerX, hourOffs + hourSize, ambientHourPaint);
                    canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, ambientHourPaint);
                }
            }

            Rect digitalBounds = new Rect();
            String digital = formatTwoDigitNumber(mTime.hour) + ":" + formatTwoDigitNumber(mTime.minute);
            textPaint.getTextBounds(digital, 0, digital.length(), digitalBounds);
            canvas.drawText(digital, centerX, centerY + digitalBounds.height() / 2, textPaint);

            String date = new SimpleDateFormat("MMM dd").format(new Date());

            Rect dateBounds = new Rect();
            //String date = mTime.format("%d %m");
            smallTextPaint.getTextBounds(date, 0, date.length(), dateBounds);
            canvas.drawText(date, centerX, centerY + dateBounds.height() / 2 + digitalBounds.height(), smallTextPaint);
        }
    }
}
