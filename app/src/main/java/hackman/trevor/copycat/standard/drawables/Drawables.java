package hackman.trevor.copycat.standard.drawables;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.content.ContextCompat;

import hackman.trevor.copycat.R;
import hackman.trevor.tlibrary.library.TColor;
import hackman.trevor.tlibrary.library.TDimensions;


public enum Drawables {;
    public static Drawable modesButton(float radius, float strokeWidth) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(TColor.Grey900);
        gd.setCornerRadius(radius);
        gd.setStroke((int)strokeWidth, TColor.Black);
        return gd;
    }

    public static LayerDrawable exitButton() {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(TColor.White);
        gd.setCornerRadius(TDimensions.dpToPixel(10));
        gd.setStroke((int)TDimensions.dpToPixel(3), TColor.Grey500);

        ExitButtonDrawable x = new ExitButtonDrawable();

        Drawable[] layers = {gd, x};
        return new LayerDrawable(layers);
    }

    public static LayerDrawable explanationButton(Context context) {

        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(TColor.Grey500);
        gd.setStroke((int)TDimensions.dpToPixel(1), TColor.Grey700);

        Drawable v = ContextCompat.getDrawable(context, R.drawable.question_mark);

        Drawable[] layers = {gd, v};
        return new LayerDrawable(layers);
    }
}
