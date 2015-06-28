package fruitiex.arcwatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;

/**
 * Created by rasse on 4/23/15.
 */
public class Values {
    SharedPreferences sharedPref;
    Context appContext;
    public Values(Context c) {
        appContext = c;
        sharedPref = c.getSharedPreferences(c.getString(R.string.app_name), Context.MODE_PRIVATE);
    }
    public int getColor(String element) {
        Resources res = appContext.getResources();
        int r = sharedPref.getInt(element + "R", res.getInteger(res.getIdentifier(element + "R", "integer", appContext.getPackageName())));
        int g = sharedPref.getInt(element + "G", res.getInteger(res.getIdentifier(element + "G", "integer", appContext.getPackageName())));
        int b = sharedPref.getInt(element + "B", res.getInteger(res.getIdentifier(element + "B", "integer", appContext.getPackageName())));

        return Color.rgb(r, g, b);
    }
    public boolean getBoolean(String element) {
        Resources res = appContext.getResources();

        // NOTE: for some reason getBool doesn't want to play ball, so here's this hack instead.
        return sharedPref.getInt(element, res.getInteger(res.getIdentifier(element, "integer", appContext.getPackageName()))) == 1 ? true : false;
    }
    public void setBoolean(String element, boolean value) {
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(element, value ? 1 : 0);

        editor.commit();
        WatchFace.resetColors();
    }
    public void setColor(String element, int color) {
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putInt(element + "R", Color.red(color));
        editor.putInt(element + "G", Color.green(color));
        editor.putInt(element + "B", Color.blue(color));

        editor.commit();
        WatchFace.resetColors();
    }
    public void resetValues() {
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.clear();
        editor.commit();
        WatchFace.resetColors();
    }
}
