package hackman.trevor.copycat.ui;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import hackman.trevor.copycat.MainActivity;
import hackman.trevor.copycat.R;
import hackman.trevor.copycat.logic.Game;
import hackman.trevor.copycat.standard.AndroidSound;
import hackman.trevor.tlibrary.library.TColor;
import hackman.trevor.tlibrary.library.TDimensions;
import hackman.trevor.tlibrary.library.ui.GoodTextView;
import hackman.trevor.tlibrary.library.ui.Llp;
import hackman.trevor.tlibrary.library.ui.Rlp;

@SuppressLint("ViewConstructor")
public class ModeView extends RelativeLayout implements View.OnClickListener {
    private MainActivity main;
    private Game.GameMode mode;
    private ModesMenu modesMenu;

    private GoodTextView displayName;
    private GoodTextView bestText;

    private boolean selected;
    private GradientDrawable selectedDrawable;
    private GradientDrawable normalDrawable;

    private int height;
    private int width;

    private boolean layout;

    public ModeView(MainActivity main, Game.GameMode mode, ModesMenu modesMenu, boolean layout) {
        super(main);

        this.main = main;
        this.mode = mode;
        this.modesMenu = modesMenu;
        this.layout = layout;

        setOnClickListener(this);

        normalDrawable = new GradientDrawable();
        normalDrawable.setColor(TColor.Grey100);
        normalDrawable.setStroke(TDimensions.mdToPixels(2.5), TColor.Grey400);

        selectedDrawable = new GradientDrawable();
        selectedDrawable.setColor(TColor.White);
        selectedDrawable.setStroke(TDimensions.mdToPixels(2.5), TColor.valueScale(selectColor, .3));

        // Display name
        displayName = new GoodTextView(main);
        displayName.setText(mode.displayName());
        displayName.setTextColor(TColor.Black);
        displayName.setOutlineColor(selectColor);
        displayName.enableBold();
        displayName.setGravity(Gravity.CENTER);

        // Best text
        bestText = new GoodTextView(main);
        bestText.setTextColor(TColor.Black);
        bestText.setOutlineColor(selectColor);
        bestText.enableBold();
        bestText.setGravity(Gravity.CENTER);
        updateBest();


        if (layout) {
            selectedTextSize = .6f;

            Rlp displayNameLp = new Rlp();
            displayNameLp.centerVertical();
            displayName.setLayoutParams(displayNameLp);

            Rlp bestTextLp = new Rlp();
            bestTextLp.bottom();
            bestTextLp.bottomMargin = TDimensions.dpToPixel(1);
            bestText.setLayoutParams(bestTextLp);

            addView(displayName);
            addView(bestText);
        }
        else {
            selectedTextSize = .45f;

            LinearLayout vertical = new LinearLayout(main);
            vertical.setOrientation(LinearLayout.VERTICAL);
            vertical.setLayoutParams(new Rlp(Rlp.MATCH, Rlp.WRAP));
            vertical.addView(displayName);
            vertical.addView(bestText);
            addView(vertical);

            displayName.setLayoutParams(new Llp(Llp.MATCH, Llp.WRAP));
            bestText.setLayoutParams(new Llp(Llp.MATCH, Llp.WRAP));
        }
    }

    public void updateBest() {
        String bestString = main.getString(R.string.Best) + " " + main.tPreferences.getInt(mode.name() + "Best", 0);
        bestText.setText(bestString);
    }

    @Override
    public void onClick(View v) {
        if (modesMenu.isModesMenuUp && !selected) {
            main.tPreferences.putString("gameMode", mode.name());
            modesMenu.modeSelected(this);

            // Play button sound
            AndroidSound.click.play(main);
        }
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);

        // Don't know height until this point
        height = params.height;

        // Flex margins and sizes
        if (layout) {
            ((Rlp) displayName.getLayoutParams()).leftMargin = (int) (height * 0.2f);
        }
        bestText.setPixelTextSize(height * 0.28f);
    }

    @Override
    // Performing modifications to modeView in onLayout doesn't trigger requestLayout(), may result in bounds clipping
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (layout) {
            width = getWidth();
            bestText.setX(width * 0.70f);
        }
    }

    private float normalTextSize = .4f;
    private float selectedTextSize;
    private final float normalAlpha = 0.8f;
    public void setNormalGraphic() {
        selected = false;
        displayName.setAlpha(normalAlpha);
        displayName.setPixelTextSize(height * normalTextSize);
        displayName.setTextColor(TColor.Black);
        displayName.outline(0);

        bestText.setAlpha(normalAlpha);
        bestText.setTextColor(TColor.Black);
        bestText.outline(0);

        setBackground(normalDrawable);
    }

    private final int selectColor = 0xFF24b35f;
    public void setSelectedGraphic() {
        selected = true;
        //displayName.outline(.09, .09);
        displayName.setAlpha(1.0f);
        displayName.setPixelTextSize(height * selectedTextSize);
        displayName.setTextColor(selectColor);
        displayName.outline(0.06);

        bestText.setAlpha(1.0f);
        bestText.setTextColor(selectColor);
        bestText.outline(0.04);

        setBackground(selectedDrawable);
    }

    public Game.GameMode getMode() {
        return mode;
    }
}
