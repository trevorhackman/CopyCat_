package hackman.trevor.tlibrary.library.ui;

import android.widget.RelativeLayout;

import static hackman.trevor.tlibrary.library.TDimensions.mdToPixels;

// A class purely for convenience. Much less typing and shorter code lines
public class Rlp extends RelativeLayout.LayoutParams {
    public static int WRAP = RelativeLayout.LayoutParams.WRAP_CONTENT; // Value is -2
    public static int MATCH = RelativeLayout.LayoutParams.MATCH_PARENT; // Value is -1

    public Rlp() {
        this(WRAP, WRAP);
    }

    public Rlp(int param) {
        this(param, param);
    }

    // Use WRAP or MATCH, else size by md
    public Rlp(int param1, int param2) {
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

    public Rlp rule(int verb, int subject) {
        super.addRule(verb, subject);
        return this;
    }

    public Rlp center() {
        addRule(RelativeLayout.CENTER_IN_PARENT);
        return this;
    }

    public Rlp centerVertical() {
        addRule(RelativeLayout.CENTER_VERTICAL);
        return this;
    }

    public Rlp centerHorizontal() {
        addRule(RelativeLayout.CENTER_HORIZONTAL);
        return this;
    }

    public Rlp top() {
        addRule(RelativeLayout.ALIGN_PARENT_TOP);
        return this;
    }

    public Rlp bottom() {
        addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        return this;
    }

    public Rlp right() {
        addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        return this;
    }

    public Rlp left() {
        addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        return this;
    }
}
