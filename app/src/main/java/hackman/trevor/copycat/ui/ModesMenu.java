package hackman.trevor.copycat.ui;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import hackman.trevor.copycat.MainActivity;
import hackman.trevor.copycat.R;
import hackman.trevor.copycat.logic.Game;
import hackman.trevor.copycat.standard.AndroidSound;
import hackman.trevor.tlibrary.library.TDimensions;
import hackman.trevor.tlibrary.library.ui.GoodTextView;
import hackman.trevor.tlibrary.library.ui.Llp;
import hackman.trevor.tlibrary.library.ui.Rlp;

@SuppressLint("ViewConstructor")
public class ModesMenu extends RelativeLayout {
    private MainActivity main;

    private LinearLayout wrapper;

    private ImageView divider1;
    private ImageView divider2;

    private GoodTextView headText;
    private GoodTextView descriptionText;

    private LinearLayout buttonLayout;
    private ModeView[] modeViews;
    private Button closeButton;


    // Animation durations
    private final int fadeInDuration = 500;
    private final int fadeOutDuration = 300;

    // Animation listeners
    private Runnable onOpenEnd;
    private Runnable onCloseEnd;

    public boolean isModesMenuUp; // Keeps track of whether modes menu is open/opening or not
    private boolean isModesMenuCompletelyUp; // Keeps track of whether menu is completely open (fade in animation over)

    private final int edgeMargin = TDimensions.dpToPixel(8);

    public ModesMenu(final MainActivity main) {
        super(main);
        this.main = main;

        // Properties
        setAlpha(0.0f); // Be invisible initially for fade-in animation
        setClickable(true); // Or else it can be clicked through
        setBackground(ContextCompat.getDrawable(main, R.drawable.bordered_rectangle));
        Rlp rlp = new Rlp().center();
        setLayoutParams(rlp);

        // Bring to front
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslationZ(-1);
        }

        wrapper = new LinearLayout(main);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setGravity(Gravity.CENTER_HORIZONTAL);
        Rlp wrapperLp = new Rlp(Rlp.MATCH, Rlp.WRAP);
        wrapper.setLayoutParams(wrapperLp.center());

        // Head text
        headText = new GoodTextView(main);
        headText.setText(main.getString(R.string.Select_Mode));
        headText.enableBold();
        headText.setId(R.id.modesMenuHeadText);
        headText.setLayoutParams(new Llp());

        // Dividers
        divider1 = new ImageView(main);
        divider1.setBackground(ContextCompat.getDrawable(main, R.drawable.block));
        divider1.setId(R.id.modesMenuDivider1);

        divider2 = new ImageView(main);
        divider2.setBackground(ContextCompat.getDrawable(main, R.drawable.block));
        divider2.setId(R.id.modesMenuDivider2);

        Llp dividerLp1 = new Llp(Llp.MATCH, 0);
        dividerLp1.height = TDimensions.dpToPixel(1);
        dividerLp1.leftMargin = TDimensions.dpToPixel(5);
        dividerLp1.rightMargin = TDimensions.dpToPixel(5);
        divider1.setLayoutParams(dividerLp1);

        Llp dividerLp2 = new Llp(Llp.MATCH, 0);
        dividerLp2.height = TDimensions.dpToPixel(1);
        dividerLp2.leftMargin = TDimensions.dpToPixel(5);
        dividerLp2.rightMargin = TDimensions.dpToPixel(5);
        divider2.setLayoutParams(dividerLp2);

        // Button layout
        buttonLayout = new LinearLayout(main);

        // Description text
        descriptionText = new GoodTextView(main) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                onDescriptionTextMeasure();
            }
        };
        descriptionText.setId(R.id.modesMenuDescriptionText);
        updateDescriptionText();
        descriptionText.enableShadows();
        descriptionText.setGravity(Gravity.CENTER);
        placeHolder = new GoodTextView(main); // Use for measuring purposes

        Llp descriptionLp = new Llp(Llp.MATCH, Llp.WRAP);
        descriptionText.setLayoutParams(descriptionLp);

        // Close button
        closeButton = new Button(main);
        closeButton.setId(R.id.modesMenuCloseButton);
        closeButton.setTypeface(null, Typeface.BOLD);
        closeButton.setText(R.string.Close);
        closeButton.setPadding(0, 0, 0, 0);
        closeButton.setBackground(ContextCompat.getDrawable(main, R.drawable.bordered_rectangle_2));
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();

                // Play button sound
                AndroidSound.click.play(main);
            }
        });

        Llp closeButtonLp = new Llp(Llp.MATCH, Llp.WRAP);
        closeButtonLp.setPixelMargins(TDimensions.dpToPixel(8));
        closeButton.setLayoutParams(closeButtonLp);

        // Put everything together
        wrapper.addView(headText);
        wrapper.addView(divider1);
        wrapper.addView(divider2);
        wrapper.addView(descriptionText);
        wrapper.addView(closeButton);
        addView(wrapper);

        // Animation listeners
        onOpenEnd = new Runnable() {
            @Override
            public void run() {
                isModesMenuCompletelyUp = true;
                closeButton.setEnabled(true);
            }
        };

        onCloseEnd = new Runnable() {
            @Override
            public void run() {
                main.gameScreen.enableNonColorButtons();
                closeButton.setEnabled(true);

                // Manual move to back
                final ViewGroup parent = (ViewGroup) ModesMenu.this.getParent();
                parent.removeView(ModesMenu.this);
                parent.addView(ModesMenu.this, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ModesMenu.this.setTranslationZ(-1); // Brings to back
                }
            }
        };
    }

    public void flex(int height, int width) {
        ViewGroup.LayoutParams params = this.getLayoutParams();

        // Portrait
        if (width < height) params.width = LinearLayout.LayoutParams.MATCH_PARENT;
        else { // Landscape
            params.width = (height + width)/2;
        }

        // Determine if tall or short button layout needed
        boolean isTall = (width < height && TDimensions.pixelsToDp(height) > 420);

        // Button Layout
        if (isTall) buttonLayoutTall();
        else buttonLayoutShort();

        final float scale           = 1.4f;
        float headTextSize          = 30f;
        float descriptionTextSize   = 14f;
        float closeTextSize         = 13.886f;
        float modeSize              = 42;

        float minHeadSize           = TDimensions.dpToPixel(headTextSize);
        float minDescriptionSize    = TDimensions.dpToPixel(descriptionTextSize);
        float minCloseSize          = TDimensions.dpToPixel(closeTextSize);
        float minModeSize           = TDimensions.dpToPixel(modeSize);

        float calculatedHeadSize        = TDimensions.mdToPixels(headTextSize * scale);
        float calculatedDescriptionSize = TDimensions.mdToPixels(descriptionTextSize * scale);
        float calculatedCloseSize       = TDimensions.mdToPixels(closeTextSize * scale);
        float calculatedModeSize        = TDimensions.mdToPixels(modeSize * scale);

        headTextSize            = Math.max(minHeadSize, calculatedHeadSize);
        descriptionTextSize     = Math.max(minDescriptionSize, calculatedDescriptionSize);
        closeTextSize           = Math.max(minCloseSize, calculatedCloseSize);
        modeSize                = Math.max(minModeSize, calculatedModeSize);

        int modeMarginLR = TDimensions.mdToPixels(25);
        int modeMarginTB = TDimensions.mdToPixels(5);

        headText.setPixelTextSize(headTextSize);
        descriptionText.setPixelTextSize(descriptionTextSize);
        closeButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, closeTextSize);
        closeButton.getLayoutParams().height = (int)(2 * closeTextSize);

        if (isTall) {
            Llp modeLp = new Llp(Llp.MATCH, 0);
            modeLp.height = (int) modeSize;
            modeLp.setPixelMargins(modeMarginLR, modeMarginTB, modeMarginLR, modeMarginTB);

            for (ModeView modeView : modeViews) {
                modeView.setLayoutParams(modeLp);
            }
        }
        else {
            modeMarginLR = TDimensions.mdToPixels(20);
            modeMarginTB = TDimensions.mdToPixels(4);

            Llp leftLp = new Llp(Llp.MATCH, 0);
            leftLp.height = (int) modeSize;
            leftLp.setPixelMargins(modeMarginLR, modeMarginTB, modeMarginLR/2, modeMarginTB);

            Llp rightLp = new Llp(Llp.MATCH, 0);
            rightLp.height = (int) modeSize;
            rightLp.setPixelMargins(modeMarginLR/2, modeMarginTB, modeMarginLR, modeMarginTB);

            for (int i = 0; i < modeViews.length; ++i) {
                if (i%2==0) modeViews[i].setLayoutParams(leftLp);
                else modeViews[i].setLayoutParams(rightLp);
            }
        }

        for (ModeView modeView : modeViews) {
            if (modeView == selected) modeView.setSelectedGraphic();
            else modeView.setNormalGraphic();
        }

        //((Rlp)headText.getLayoutParams()).topMargin = TDimensions.hpToPixel(TDimensions.HP/8);
        ((Llp)descriptionText.getLayoutParams()).setPixelMargins(modeMarginLR, 0, modeMarginLR, modeMarginTB);

        initialAfterFlex = true;
    }

    // We need to measure the max height of descriptionText and give it a fixed height at that value to prevent shifting heights
    private boolean initialAfterFlex = false;
    private GoodTextView placeHolder;
    private void onDescriptionTextMeasure() {
        if (initialAfterFlex) {
            placeHolder.setPixelTextSize(descriptionText.getTextSize());
            placeHolder.setLayoutParams(descriptionText.getLayoutParams());
            placeHolder.setText(main.getString(R.string.description_Single)); // Set to longest description, will need to manually change this if longest changes
            int widthSpec = MeasureSpec.makeMeasureSpec(descriptionText.getMeasuredWidth(), MeasureSpec.EXACTLY);
            int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            placeHolder.measure(widthSpec, heightSpec);

            descriptionText.setHeight(placeHolder.getMeasuredHeight());
            initialAfterFlex = false;
        }
    }

    public void open() {
        isModesMenuUp = true;
        main.gameScreen.disableNonColorButtons();

        // Bring to front
        this.bringToFront();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            getParent().requestLayout();
            ((View) getParent()).invalidate();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.setTranslationZ(999); // Fix for bringToFront not completely working on newer APIs with relativeLayout
        }

        // Check for updates to best of each mode, slightly inefficient
        for (ModeView modeView : modeViews) {
            modeView.updateBest();
        }

        closeButton.setEnabled(false);

        animate().alpha(1.0f).setDuration(fadeInDuration).withEndAction(onOpenEnd);
    }

    public void close() {
        if (isModesMenuCompletelyUp) {
            isModesMenuUp = false;
            isModesMenuCompletelyUp = false;
            main.gameScreen.enableModesButton();

            closeButton.setEnabled(false);
            animate().alpha(0.0f).setDuration(fadeOutDuration).withEndAction(onCloseEnd);
            main.gameScreen.forMenuFadeIn();
        }
    }

    private ModeView selected;
    public void modeSelected(ModeView selected) {
        this.selected.setNormalGraphic();
        this.selected = selected;
        selected.setSelectedGraphic();
        updateDescriptionText();

        // Update the popUp and playSymbol
        main.gameScreen.popUpInstructions.modeUpdate();
        main.gameScreen.playSymbolSetUp();
    }

    private void updateDescriptionText() {
        Game.GameMode mode = Game.GameMode.valueOf(main.tPreferences.getString("gameMode", Game.GameMode.Classic.name()));

        String description = null;
        switch (mode) {
            case Classic:
                description = main.getString(R.string.description_Classic);
                break;
            case Reverse:
                description = main.getString(R.string.description_Reverse);
                break;
            case Chaos:
                description = main.getString(R.string.description_Chaos);
                break;
            case NoRepeat:
                description = main.getString(R.string.description_Single);
                break;
        }
        descriptionText.setText(description);
    }

    private void buttonLayoutTall() {
        if (buttonLayout.getParent() != null) {
            wrapper.removeView(buttonLayout);
            buttonLayout.removeAllViews();
        }

        // Button layout
        buttonLayout.setOrientation(LinearLayout.VERTICAL);

        Llp buttonLayoutLp = new Llp(Rlp.MATCH, Rlp.WRAP);
        buttonLayoutLp.setPixelMargins(TDimensions.dpToPixel(8));
        buttonLayout.setLayoutParams(buttonLayoutLp);

        Game.GameMode[] modes = Game.GameMode.values();
        modeViews = new ModeView[modes.length];
        Game.GameMode mode = Game.GameMode.valueOf(main.tPreferences.getString("gameMode", Game.GameMode.Classic.name()));
        for (int i = 0; i < modes.length; ++i) {
            modeViews[i] = new ModeView(main, modes[i], this, true);
            // Set selected
            if (modes[i] == mode) {
                selected = modeViews[i];
            }
        }

        for (ModeView modeView : modeViews) {
            buttonLayout.addView(modeView);
        }

        wrapper.addView(buttonLayout, wrapper.indexOfChild(divider1) + 1);
    }

    private void buttonLayoutShort() {
        if (buttonLayout.getParent() != null) {
            wrapper.removeView(buttonLayout);
            buttonLayout.removeAllViews();
        }

        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

        Llp buttonLayoutLp = new Llp(Rlp.MATCH, Rlp.WRAP);
        buttonLayoutLp.setPixelMargins(TDimensions.dpToPixel(8));
        buttonLayout.setLayoutParams(buttonLayoutLp);

        LinearLayout subButtonLayout1 = new LinearLayout(main);
        LinearLayout subButtonLayout2 = new LinearLayout(main);
        subButtonLayout1.setOrientation(LinearLayout.VERTICAL);
        subButtonLayout2.setOrientation(LinearLayout.VERTICAL);

        Llp subLp = new Llp(0, Llp.MATCH);
        subLp.weight = 1;
        subButtonLayout1.setLayoutParams(subLp);
        subButtonLayout2.setLayoutParams(subLp);

        buttonLayout.addView(subButtonLayout1);
        buttonLayout.addView(subButtonLayout2);

        Game.GameMode[] modes = Game.GameMode.values();
        modeViews = new ModeView[modes.length];
        Game.GameMode mode = Game.GameMode.valueOf(main.tPreferences.getString("gameMode", Game.GameMode.Classic.name()));
        for (int i = 0; i < modes.length; ++i) {
            modeViews[i] = new ModeView(main, modes[i], this, false);
            // Set selected
            if (modes[i] == mode) {
                selected = modeViews[i];
            }
        }

        for (int i = 0; i < modeViews.length; ++i) {
            if (i%2 == 0) subButtonLayout1.addView(modeViews[i]);
            else subButtonLayout2.addView(modeViews[i]);
        }

        wrapper.addView(buttonLayout, wrapper.indexOfChild(divider1) + 1);
    }
}
