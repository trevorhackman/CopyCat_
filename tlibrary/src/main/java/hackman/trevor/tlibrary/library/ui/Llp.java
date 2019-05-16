package hackman.trevor.tlibrary.library.ui;

import android.view.Gravity;
import android.widget.LinearLayout;

import static hackman.trevor.tlibrary.library.TDimensions.mdToPixels;

// A class purely for convenience. Much less typing and shorter code lines
public class Llp extends LinearLayout.LayoutParams {
    public static int WRAP = LinearLayout.LayoutParams.WRAP_CONTENT;
    public static int MATCH = LinearLayout.LayoutParams.MATCH_PARENT;

    public Llp() {
        super(WRAP, WRAP); // width, height
    }

    public Llp(int param) {
        super(param, param);
    }

    // Use WRAP or MATCH, else size by md
    public Llp(int param1, int param2) {
        super(param1 == WRAP || param1 == MATCH ? param1 : mdToPixels(param1), param2 == WRAP || param2 == MATCH ? param2 : mdToPixels(param2));
    }

    public void zeroMargins() {
        super.setMargins(0, 0, 0, 0);
    }

    // Uses md unit
    public void setMargins(float margins) {
        setMargins(margins, margins, margins, margins);
    }

    // Override super method
    public void setMargins(int left, int top, int right, int bottom) {
        setMargins((float)left, (float)top, (float)right, (float) bottom);
    }

    public void setMargins(float left, float top, float right, float bottom) {
        super.setMargins(mdToPixels(left), mdToPixels(top), mdToPixels(right), mdToPixels(bottom));
    }

    public void setPixelMargins(int pixelMargins) {
        setPixelMargins(pixelMargins, pixelMargins, pixelMargins, pixelMargins);
    }

    public void setPixelMargins(int left, int top, int right, int bottom) {
        super.setMargins(left, top, right, bottom);
    }

    public Llp center() {
        this.gravity = Gravity.CENTER;
        return this;
    }
}
