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
import android.view.Gravity;
import android.view.SurfaceHolder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class WatchFace extends CanvasWatchFaceService {
    static float hourSize = 12.f;
    static float minSize = 6.f;
    static float lineSize = 1.5f;
    static float minOffs = 25;
    static float hourOffs = 50;
    static float textSize = 60;
    static float textSpacing = 10;
    static float smallTextSize = 20;

    static Paint burninHourPaint;
    static Paint burninMinutePaint;
    static Paint activeHourPaint;
    static Paint activeMinutePaint;
    static Paint mTickPaint;
    static Paint textHourPaint;
    static Paint textMinutePaint;
    static Paint datePaint;

    static Values val;

    // device screen details
    boolean mLowBitAmbient;
    boolean mBurnInProtection;

    public static void resetColors() {
        int hourColor = val.getColor("hour");
        int minuteColor = val.getColor("minute");
        int tickColor = val.getColor("tick");
        int textHourColor = val.getColor("textHour");
        int textMinuteColor = val.getColor("textMinute");
        int dateColor = val.getColor("date");

        burninHourPaint = new Paint();
        burninHourPaint.setARGB(255,
                Color.red(hourColor),
                Color.green(hourColor),
                Color.blue(hourColor));
        burninHourPaint.setStrokeWidth(lineSize);
        burninHourPaint.setStyle(Paint.Style.STROKE);
        burninHourPaint.setAntiAlias(true);

        burninMinutePaint = new Paint();
        burninMinutePaint.setARGB(255,
                Color.red(minuteColor),
                Color.green(minuteColor),
                Color.blue(minuteColor));
        burninMinutePaint.setStrokeWidth(lineSize);
        burninMinutePaint.setStyle(Paint.Style.STROKE);
        burninMinutePaint.setAntiAlias(true);

        activeHourPaint = new Paint();
        activeHourPaint.setARGB(255,
                Color.red(hourColor),
                Color.green(hourColor),
                Color.blue(hourColor));
        activeHourPaint.setStrokeWidth(hourSize);
        activeHourPaint.setStyle(Paint.Style.STROKE);
        activeHourPaint.setAntiAlias(true);

        activeMinutePaint = new Paint();
        activeMinutePaint.setARGB(255,
                Color.red(minuteColor),
                Color.green(minuteColor),
                Color.blue(minuteColor));
        activeMinutePaint.setStrokeWidth(minSize);
        activeMinutePaint.setStyle(Paint.Style.STROKE);
        activeMinutePaint.setAntiAlias(true);

        mTickPaint = new Paint();
        mTickPaint.setARGB(255,
                Color.red(tickColor),
                Color.green(tickColor),
                Color.blue(tickColor));
        mTickPaint.setStrokeWidth(lineSize);
        mTickPaint.setAntiAlias(true);

        textHourPaint = new Paint();
        textHourPaint.setARGB(255,
                Color.red(textHourColor),
                Color.green(textHourColor),
                Color.blue(textHourColor));
        textHourPaint.setStrokeWidth(lineSize);
        textHourPaint.setAntiAlias(true);
        textHourPaint.setTextAlign(Paint.Align.CENTER);
        textHourPaint.setTextSize(textSize);
        textHourPaint.setStyle(Paint.Style.FILL);

        textMinutePaint = new Paint();
        textMinutePaint.setARGB(255,
                Color.red(textMinuteColor),
                Color.green(textMinuteColor),
                Color.blue(textMinuteColor));
        textMinutePaint.setStrokeWidth(lineSize);
        textMinutePaint.setAntiAlias(true);
        textMinutePaint.setTextAlign(Paint.Align.CENTER);
        textMinutePaint.setTextSize(textSize);
        textMinutePaint.setStyle(Paint.Style.FILL);

        datePaint = new Paint();
        datePaint.setARGB(255,
                Color.red(dateColor),
                Color.green(dateColor),
                Color.blue(dateColor));
        datePaint.setStrokeWidth(lineSize);
        datePaint.setAntiAlias(true);
        datePaint.setTextAlign(Paint.Align.CENTER);
        datePaint.setTextSize(smallTextSize);
        datePaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public Engine onCreateEngine() {
        val = new Values(getApplicationContext());
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        Time mTime;

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION,
                    false);
        }
        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);

            // low-bit ambient mode disables antialiasing and sets colors to white
            if(inAmbientMode && mLowBitAmbient) {
                burninHourPaint.setAntiAlias(false);
                burninMinutePaint.setAntiAlias(false);
                activeHourPaint.setAntiAlias(false);
                activeMinutePaint.setAntiAlias(false);
                mTickPaint.setAntiAlias(false);
                textHourPaint.setAntiAlias(false);
                textMinutePaint.setAntiAlias(false);
                datePaint.setAntiAlias(false);

                burninHourPaint.setARGB(255, 255, 255, 255);
                burninMinutePaint.setARGB(255, 255, 255, 255);
                activeHourPaint.setARGB(255, 255, 255, 255);
                activeMinutePaint.setARGB(255, 255, 255, 255);
                mTickPaint.setARGB(255, 255, 255, 255);
                textHourPaint.setARGB(255, 255, 255, 255);
                textMinutePaint.setARGB(255, 255, 255, 255);
                datePaint.setARGB(255, 255, 255, 255);
            } else {
                resetColors();
            }

            // ambient burn in protection mode uses outlined fonts
            if(inAmbientMode && mBurnInProtection) {
                textHourPaint.setStyle(Paint.Style.STROKE);
                textMinutePaint.setStyle(Paint.Style.STROKE);
                //datePaint.setStyle(Paint.Style.STROKE);
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

            resetColors();

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

            // draw the ticks/dials
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

            // draw the clock pointers

            // burn in protection mode is a little involved. we draw the outlines of an
            // arc by drawing two very thin arcs and connecting them with lines
            if(isInAmbientMode() && mBurnInProtection) {
                /* minutes, don't draw anything if min == 0 */
                if(minRot != 0) {
                    // draw the arcs
                    canvas.drawArc(new RectF(minOffs, minOffs, width - minOffs, height - minOffs), -90, minRot, false, burninMinutePaint);
                    canvas.drawArc(new RectF(minOffs + minSize, minOffs + minSize, width - (minOffs + minSize), height - (minOffs + minSize)), -90, minRot, false, burninMinutePaint);

                    // draw the "end caps" between the arcs
                    innerX = (float) Math.sin(minRot / 360f * 2 * Math.PI) * (centerX - minOffs - minSize);
                    innerY = (float) -Math.cos(minRot / 360f * 2 * Math.PI) * (centerX - minOffs - minSize);
                    outerX = (float) Math.sin(minRot / 360f * 2 * Math.PI) * (centerX - minOffs);
                    outerY = (float) -Math.cos(minRot / 360f * 2 * Math.PI) * (centerX - minOffs);
                    canvas.drawLine(centerX, minOffs, centerX, minOffs + minSize, burninMinutePaint);
                    canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, burninMinutePaint);
                }

                /* hours, don't draw anything if hour == 0 */
                if(hourRot != 0) {
                    // draw the arcs
                    canvas.drawArc(new RectF(hourOffs, hourOffs, width - hourOffs, height - hourOffs), -90, hourRot, false, burninHourPaint);
                    canvas.drawArc(new RectF(hourOffs + hourSize, hourOffs + hourSize, width - (hourOffs + hourSize), height - (hourOffs + hourSize)), -90, hourRot, false, burninHourPaint);

                    // draw the "end caps" between the arcs

                    innerX = (float) Math.sin(hourRot / 360f * 2 * Math.PI) * (centerX - hourOffs - hourSize);
                    innerY = (float) -Math.cos(hourRot / 360f * 2 * Math.PI) * (centerX - hourOffs - hourSize);
                    outerX = (float) Math.sin(hourRot / 360f * 2 * Math.PI) * (centerX - hourOffs);
                    outerY = (float) -Math.cos(hourRot / 360f * 2 * Math.PI) * (centerX - hourOffs);
                    canvas.drawLine(centerX, hourOffs, centerX, hourOffs + hourSize, burninHourPaint);
                    canvas.drawLine(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY, burninHourPaint);
                }
            } else {
                canvas.drawArc(new RectF(minOffs + minSize / 2, minOffs + minSize / 2, width - minOffs - minSize / 2, height - minOffs - minSize / 2), -90, minRot, false, activeMinutePaint);
                canvas.drawArc(new RectF(hourOffs + hourSize / 2, hourOffs + hourSize / 2, width - hourOffs - hourSize / 2, height - hourOffs - hourSize / 2), -90, hourRot, false, activeHourPaint);
            }

            // draw digital clock in the middle
            Rect digitalBounds = new Rect();
            String digital = formatTwoDigitNumber(mTime.hour) + formatTwoDigitNumber(mTime.minute);
            textHourPaint.getTextBounds(digital, 0, digital.length(), digitalBounds);

            canvas.drawText(formatTwoDigitNumber(mTime.hour), centerX - digitalBounds.width() / 4 - textSpacing / 2, centerY + digitalBounds.height() / 2, textHourPaint);
            canvas.drawText(formatTwoDigitNumber(mTime.minute), centerX + digitalBounds.width() / 4 + textSpacing / 2, centerY + digitalBounds.height() / 2, textMinutePaint);

            // draw current date below digital clock
            String date = new SimpleDateFormat("MMM dd").format(new Date());
            Rect dateBounds = new Rect();
            datePaint.getTextBounds(date, 0, date.length(), dateBounds);
            canvas.drawText(date, centerX, centerY + dateBounds.height() / 2 + digitalBounds.height(), datePaint);
        }
    }
}
