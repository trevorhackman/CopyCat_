package hackman.trevor.copycat.system.drawables;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;

import hackman.trevor.tlibrary.library.TColor;

// Custom drawable for the hint button
public class ExitButtonDrawable extends GradientDrawable {

    // Intentionally package-private
    ExitButtonDrawable() {
        super();
    }

    private int width;
    private int height;

    private Paint paint;
    private Path path;

    private boolean enabled = true;

    private boolean initialized;
    private void initialize() {
        // Get width and height
        width = getBounds().width();
        height = getBounds().height();

        paint = new Paint();
        if (enabled) paint.setColor(TColor.Black);
        else paint.setColor(TColor.Grey400);
        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(width * .1f);

        path = new Path();
        float length = width * .2f;
        float center = width/2f;
        path.moveTo(center - length, center - length);
        path.lineTo(center + length, center + length);
        path.moveTo(center + length, center - length);
        path.lineTo(center - length, center + length);
    }

    public void setEnabledGraphic() {
        enabled = true;
        paint.setColor(TColor.Black);
        invalidateSelf();
    }

    public void setDisabledGraphic() {
        enabled = false;
        paint.setColor(TColor.Grey400);
        invalidateSelf();
    }

    @Override
    public void draw(Canvas canvas) {
        if (!initialized) {
            initialize();
            initialized = true;
        }
        // Size of drawable is subject to change
        if (getBounds().width() != width || getBounds().height() != height) {
            initialize();
        }

        canvas.drawPath(path, paint);
    }
}
