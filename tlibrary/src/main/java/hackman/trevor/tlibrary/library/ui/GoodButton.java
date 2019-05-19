package hackman.trevor.tlibrary.library.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import static hackman.trevor.tlibrary.library.TDimensions.mdToPixels;

public class GoodButton extends android.support.v7.widget.AppCompatButton {

    public GoodButton(Context context) {
        super(context);
        setAllCaps(true);
        setTypeface(null, Typeface.BOLD);
        // setTextColor(Color.BLACK); // Default text color is a colorStateList (for example different color on enabled vs disabled)
        setTextSize(16.5f);
    }

    public GoodButton(Context context, Drawable drawable) {
        this(context);
        setBackground(drawable);
    }

    @Override // Sets text size with md unit
    public void setTextSize(float size) {
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, mdToPixels(size));
        requestLayout(); // The size of the text may escape the bounds of the prior layout
    }

    public void setPixelTextSize(float size) {
        super.setTextSize(TypedValue.COMPLEX_UNIT_PX, (int)size);
        requestLayout(); // The size of the text may escape the bounds of the prior layout
    }
}
