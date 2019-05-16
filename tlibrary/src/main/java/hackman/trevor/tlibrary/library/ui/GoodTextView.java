package hackman.trevor.tlibrary.library.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.TypedValue;

import hackman.trevor.tlibrary.library.TColor;

import static hackman.trevor.tlibrary.library.TDimensions.mdToPixels;


public class GoodTextView extends android.support.v7.widget.AppCompatTextView {
    private Paint p;
    private boolean bOutline1 = false; // If an outline is drawn
    private boolean bOutline2 = false; // If a double outline is drawn
    private int solid = TColor.White; // The default solid color
    private int outline1 = TColor.Black; // The default first outline color
    private int outline2 = TColor.White; // The default second outline color
    private float thickness1; // The thickness is in terms of a ratio of the text size
    private float thickness2; // ^^

    public GoodTextView(Context context) {
        super(context);
        p = getPaint();
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

    @Override // Sets textColor with solid
    public void setTextColor(int color) {
        solid = color;
    }

    // Enables outline and sets thickness
    public void outline(double thickness) {
        bOutline1 = true;
        this.thickness1 = (float)(getTextSize() * thickness);
        invalidate(); // Necessary or else outline isn't necessarily drawn if it's done post layout
    }

    public void outline() {
        outline(thickness1, thickness2);
    }

    // Overload to combine doubleOutline() and setThickness(int, int)
    public void outline(double thickness1, double thickness2) {
        bOutline1 = true;
        bOutline2 = true;
        this.thickness1 = (float)(getTextSize() * thickness1);
        this.thickness2 = (float)(getTextSize() * thickness2);
        invalidate(); // Necessary or else outline isn't necessarily drawn if it's done post layout
    }

    // Set single color for first outline
    public void setOutlineColor(int color) {
        outline1 = color;
    }

    // Set two colors for double outline
    public void setOutlineColor(int color1, int color2) {
        outline1 = color1;
        outline2 = color2;
    }

    public void enableBold() {
        setTypeface(getTypeface(), Typeface.BOLD);
    }

    public void enableShadows() {
        setShadowLayer(3, 2, 1, Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(bOutline1) {
            if (bOutline2) {
                // Double outline
                super.setTextColor(outline2);
                p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(thickness1 + thickness2);
                super.onDraw(canvas);
            }

            // Outline
            super.setTextColor(outline1);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(thickness1);
            // Draw outline
            super.onDraw(canvas);

            // Fill
            super.setTextColor(solid);
            p.setStyle(Paint.Style.FILL);
            super.onDraw(canvas);
        } else {
            super.setTextColor(solid);
            super.onDraw(canvas);
        }
    }
}
