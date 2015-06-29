package fruitiex.arcwatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.Gravity;
import android.view.SurfaceHolder;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class WatchFace extends CanvasWatchFaceService {
    static float hourSize = 12.f;
    static float minSize = 6.f;
    static float lineSize = 1.5f;
    static float minOffs = 25;
    static float hourOffs = 50;
    static float textSize;
    static float textSpacing;
    static float smallTextSize = 20;

    static Paint burninHourPaint;
    static Paint burninMinutePaint;
    static Paint activeHourPaint;
    static Paint activeMinutePaint;
    static Paint textHourPaint;
    static Paint textMinutePaint;
    static Paint textAmPmPaint;
    static Paint datePaint;

    static Values val;

    // device screen details
    boolean mLowBitAmbient;
    boolean mBurnInProtection;

    static boolean toggle24h;

    public static void resetColors() {
        int hourColor = val.getColor("Hour");
        int minuteColor = val.getColor("Minute");
        int textHourColor = val.getColor("TextHour");
        int textMinuteColor = val.getColor("TextMinute");
        int dateColor = val.getColor("Date");
        toggle24h = val.getBoolean("Toggle24h");
        textSize = toggle24h ? 60 : 50;
        textSpacing = 10;
        //textSpacing = toggle24h ? 10 : 6;

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

        textHourPaint = new Paint();
        textHourPaint.setARGB(255,
                Color.red(textHourColor),
                Color.green(textHourColor),
                Color.blue(textHourColor));
        textHourPaint.setStrokeWidth(lineSize);
        textHourPaint.setAntiAlias(true);
        textHourPaint.setTextSize(textSize);
        textHourPaint.setStyle(Paint.Style.FILL);

        textMinutePaint = new Paint();
        textMinutePaint.setARGB(255,
                Color.red(textMinuteColor),
                Color.green(textMinuteColor),
                Color.blue(textMinuteColor));
        textMinutePaint.setStrokeWidth(lineSize);
        textMinutePaint.setAntiAlias(true);
        textMinutePaint.setTextSize(textSize);
        textMinutePaint.setStyle(Paint.Style.FILL);

        textAmPmPaint = new Paint();
        textAmPmPaint.setARGB(255,
                Color.red(textHourColor),
                Color.green(textHourColor),
                Color.blue(textHourColor));
        textAmPmPaint.setStrokeWidth(lineSize);
        textAmPmPaint.setAntiAlias(true);
        textAmPmPaint.setTextSize(smallTextSize);
        textAmPmPaint.setStyle(Paint.Style.FILL);

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
        static final int MSG_UPDATE_TIME = 0;
        static final int INTERACTIVE_UPDATE_RATE_MS = 1000;

        // timezone handling
        Calendar mCalendar;
        boolean mRegisteredTimeZoneReceiver = false;
        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mCalendar.setTimeZone(TimeZone.getDefault());
                invalidate();
            }
        };
        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            WatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }
        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            WatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        // handler to update the time once a second in interactive mode
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler
                                .sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            unregisterReceiver();
            super.onDestroy();
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
                textHourPaint.setAntiAlias(false);
                textMinutePaint.setAntiAlias(false);
                textAmPmPaint.setAntiAlias(false);
                datePaint.setAntiAlias(false);

                burninHourPaint.setARGB(255, 255, 255, 255);
                burninMinutePaint.setARGB(255, 255, 255, 255);
                activeHourPaint.setARGB(255, 255, 255, 255);
                activeMinutePaint.setARGB(255, 255, 255, 255);
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
            updateTimer();
        }
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if(visible) {
                registerReceiver();

                mCalendar.setTimeZone(TimeZone.getDefault());
            } else {
                unregisterReceiver();
            }

            updateTimer();
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

            mCalendar = Calendar.getInstance();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            canvas.drawColor(Color.BLACK);

            mCalendar.setTimeInMillis(System.currentTimeMillis());

            int width = bounds.width();
            int height = bounds.height();

            // Find the center. Ignore the window insets so that, on round watches with a
            // "chin", the watch face is centered on the entire screen, not just the usable
            // portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            float innerX, innerY, outerX, outerY;

            float seconds = mCalendar.get(Calendar.SECOND);
            float minutes = mCalendar.get(Calendar.MINUTE) + seconds / 60f;
            float hours = mCalendar.get(Calendar.HOUR_OF_DAY) + minutes / 60f;

            float minRot = minutes      / 60f * 360;

            if(isInAmbientMode()) {
                minRot = (float) Math.floor(minutes) / 60f * 360;
            }
            float hourRot =  (hours % 12) / 12f * 360;

            // draw the clock pointers

            // burn in protection mode is a little involved. we draw the outlines of an
            // arc by drawing two very thin arcs and connecting them with lines
            if(isInAmbientMode() && mBurnInProtection) {
                // minutes, don't draw anything if min == 0
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

                // hours, don't draw anything if hour == 0
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
            String hour;
            if (toggle24h) {
                hour = String.format("%02d", mCalendar.get(Calendar.HOUR_OF_DAY));
            } else {
                // 12h clocks are weird
                int hourInt = mCalendar.get(Calendar.HOUR);
                hourInt = hourInt == 0 ? 12 : hourInt;
                hour = Integer.toString(hourInt);
            }
            String minute = String.format("%02d", mCalendar.get(Calendar.MINUTE));
            String am_pm = mCalendar.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";

            String digital = hour + minute;

            float totalWidth = textHourPaint.measureText(digital) + (toggle24h ? 1 : 2) * textSpacing;
            totalWidth += toggle24h ? 0 : textAmPmPaint.measureText(am_pm);
            float hourWidth = textHourPaint.measureText(hour);
            float minuteWidth = textHourPaint.measureText(minute);

            float offsetX = centerX - totalWidth / 2;
            float offsetY = centerY + textSize / 2.5f;

            canvas.drawText(hour, offsetX, offsetY, textHourPaint);
            offsetX += hourWidth + textSpacing;
            canvas.drawText(minute, offsetX, offsetY, textMinutePaint);
            if (!toggle24h) {
                offsetX += minuteWidth + textSpacing;
                canvas.drawText(am_pm, offsetX, offsetY, textAmPmPaint);

                // some extra spacing here looks better
                offsetY += textSize / 10.0f;
            }

            offsetY += textSize / 2.0f;

            // draw current date below digital clock
            String date = mCalendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault()) + " " + mCalendar.get(Calendar.DAY_OF_MONTH);
            canvas.drawText(date, centerX, offsetY, datePaint);
        }
    }
}