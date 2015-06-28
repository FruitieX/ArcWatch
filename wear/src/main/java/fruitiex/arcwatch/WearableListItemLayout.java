package fruitiex.arcwatch;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.support.wearable.view.WearableListView;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WearableListItemLayout extends LinearLayout
        implements WearableListView.OnCenterProximityListener {

    private ImageView mCircle;
    private TextView mName;

    private final float mFadedTextAlpha;

    static Values val;

    public WearableListItemLayout(Context context) {
        this(context, null);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WearableListItemLayout(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);

        mFadedTextAlpha = 0.5f;

        val = new Values(context);
    }

    // Get references to the icon and text in the item layout definition
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // These are defined in the layout file for list items
        // (see next section)
        mCircle = (ImageView) findViewById(R.id.circle);
        mName = (TextView) findViewById(R.id.name);
    }

    @Override
    public void onCenterPosition(boolean animate) {
        if (mName.getText().equals("toggle24h")) {
            ((GradientDrawable) mCircle.getDrawable()).setColor(Color.BLACK);
        } else {
            ((GradientDrawable) mCircle.getDrawable()).setColor(val.getColor((String) mName.getText()));
        }
        mName.setAlpha(1f);
    }

    @Override
    public void onNonCenterPosition(boolean animate) {
        if (mName.getText().equals("toggle24h")) {
            ((GradientDrawable) mCircle.getDrawable()).setColor(Color.BLACK);
        } else {
            ((GradientDrawable) mCircle.getDrawable()).setColor(val.getColor((String) mName.getText()));
        }
        mName.setAlpha(mFadedTextAlpha);
    }
}
