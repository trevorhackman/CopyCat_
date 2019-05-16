package hackman.trevor.tlibrary.library.ui;

import android.widget.GridLayout;

import static hackman.trevor.tlibrary.library.TDimensions.mdToPixels;

// A class purely for convenience. Much less typing and shorter code lines
public class Glp extends GridLayout.LayoutParams {
    // There is no WRAP or MATCH_PARENT for Glp. Glp is set to WRAP by default.

    // Sets the width and height to size in md
    public Glp(float size) {
        width = mdToPixels(size);
        height = mdToPixels(size);
    }

    public void zeroMargins() {
        super.setMargins(0, 0, 0, 0);
    }

    // Uses md unit
    public void setMargins(float margins) {
        setMargins(margins, margins, margins, margins);
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
}
