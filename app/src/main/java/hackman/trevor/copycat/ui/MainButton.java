package hackman.trevor.copycat.ui;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

public class MainButton extends AppCompatButton {

    public MainButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void shrink() {
        animate().scaleX(0.90f).scaleY(0.90f)
                .setDuration(GameScreen.mainFadeDuration);
    }

    public void unshrink() {
        animate().scaleX(1.0f).scaleY(1.0f).setDuration(GameScreen.mainFadeDuration);
    }
}
