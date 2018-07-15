package hackman.trevor.copycat;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;

import hackman.trevor.tlibrary.library.TMath;

public class MainButton extends AppCompatButton {

    public MainButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    void flexSize(int height, int width) {
        ViewGroup.LayoutParams params = this.getLayoutParams();

        float scale = 0.40f;
        float minDimension = Math.min(height, width);
        float dimensionSize = minDimension * scale;
        float minSize = TMath.convertDpToPixel(100, getContext());

        int size = (int)Math.max(minSize, dimensionSize);

        params.height = size;
        params.width = size;

        // default unit of setTextSize(float size) is sp
        setTextSize(TypedValue.COMPLEX_UNIT_PX, size/2);
    }
}
