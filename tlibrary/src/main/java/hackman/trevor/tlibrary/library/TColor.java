package hackman.trevor.tlibrary.library;

import android.graphics.Color;

// Exclusively color-related algorithms
// Parameter int color always refers to an rgb value
public enum TColor {;
    // Common Colors - Mostly taken from Google Color Guide
    public static int Transparent =         0x0000;
    public static int Shade1 =              0x11000000;
    public static int Shade2 =              0x22000000;
    public static int Shade3 =              0x33000000;
    public static int Shade4 =              0x44000000;
    public static int Shade5 =              0x55000000;
    public static int Shade6 =              0x66000000;
    public static int Shade7 =              0x77000000;
    public static int Shade8 =              0x88000000;
    public static int Shade9 =              0x99000000;
    public static int Shade10 =             0xAA000000;
    public static int Shade11 =             0xBB000000;
    public static int Shade12 =             0xCC000000;
    public static int Shade13 =             0xDD000000;
    public static int Shade14 =             0xEE000000;
    public static int Shade15 =             0xFF000000;

    public static int Red50 =               0xFFFFEBEE;
    public static int Red100 =              0xFFFFCDD2;
    public static int Red200 =              0xFFEF9A9A;
    public static int Red300 =              0xFFE57373;
    public static int Red400 =              0xFFEF5350;
    public static int Red500 =              0xFFF44336;
    public static int Red600 =              0xFFE53935;
    public static int Red700 =              0xFFD32F2F;
    public static int Red800 =              0xFFC62828;
    public static int Red900 =              0xFFB71C1C;
    public static int RedAccent100 =        0xFFFF8A80;
    public static int RedAccent200 =        0xFFFF5252;
    public static int RedAccent400 =        0xFFFF1744;
    public static int RedAccent700 =        0xFFD50000;

    public static int Pink50 =              0xFFFCE4EC;
    public static int Pink100 =             0xFFF8BBD0;
    public static int Pink200 =             0xFFF48FB1;
    public static int Pink300 =             0xFFF06292;
    public static int Pink400 =             0xFFEC407A;
    public static int Pink500 =             0xFFE91E63;
    public static int Pink600 =             0xFFD81B60;
    public static int Pink700 =             0xFFC2185B;
    public static int Pink800 =             0xFFAD1457;
    public static int Pink900 =             0xFF880E4F;
    public static int PinkAccent100 =       0xFFFF80AB;
    public static int PinkAccent200 =       0xFFFF4081;
    public static int PinkAccent400 =       0xFFF50057;
    public static int PinkAccent700 =       0xFFC51162;

    public static int Purple50 =            0xFFF3E5F5;
    public static int Purple100 =           0xFFE1BEE7;
    public static int Purple200 =           0xFFCE93D8;
    public static int Purple300 =           0xFFBA68C8;
    public static int Purple400 =           0xFFAB47BC;
    public static int Purple500 =           0xFF9C27B0;
    public static int Purple600 =           0xFF8E24AA;
    public static int Purple700 =           0xFF7B1FA2;
    public static int Purple800 =           0xFF6A1B9A;
    public static int Purple900 =           0xFF4A148C;
    public static int PurpleAccent100 =     0xFFC51162;
    public static int PurpleAccent200 =     0xFFE040FB;
    public static int PurpleAccent400 =     0xFFD500F9;
    public static int PurpleAccent700 =     0xFFAA00FF;

    public static int DeepPurple50 =        0xFFEDE7F6;
    public static int DeepPurple100 =       0xFFD1C4E9;
    public static int DeepPurple200 =       0xFFB39DDB;
    public static int DeepPurple300 =       0xFF9575CD;
    public static int DeepPurple400 =       0xFF7E57C2;
    public static int DeepPurple500 =       0xFF673AB7;
    public static int DeepPurple600 =       0xFF5E35B1;
    public static int DeepPurple700 =       0xFF512DA8;
    public static int DeepPurple800 =       0xFF4527A0;
    public static int DeepPurple900 =       0xFF311B92;
    public static int DeepPurpleAccent100 = 0xFFB388FF;
    public static int DeepPurpleAccent200 = 0xFF7C4DFF;
    public static int DeepPurpleAccent400 = 0xFF651FFF;
    public static int DeepPurpleAccent700 = 0xFF6200EA;

    public static int Indigo50 =            0xFFE8EAF6;
    public static int Indigo100 =           0xFFC5CAE9;
    public static int Indigo200 =           0xFF9FA8DA;
    public static int Indigo300 =           0xFF7986CB;
    public static int Indigo400 =           0xFF5C6BC0;
    public static int Indigo500 =           0xFF3F51B5;
    public static int Indigo600 =           0xFF3949AB;
    public static int Indigo700 =           0xFF303F9F;
    public static int Indigo800 =           0xFF283593;
    public static int Indigo900 =           0xFF1A237E;
    public static int IndigoAccent100 =     0xFF8C9EFF;
    public static int IndigoAccent200 =     0xFF536DFE;
    public static int IndigoAccent300 =     0xFF3D5AFE;
    public static int IndigoAccent700 =     0xFF304FFE;

    public static int Blue50 =              0xFFE3F2FD;
    public static int Blue100 =             0xFFBBDEFB;
    public static int Blue200 =             0xFF90CAF9;
    public static int Blue300 =             0xFF64B5F6;
    public static int Blue400 =             0xFF42A5F5;
    public static int Blue500 =             0xFF2196F3;
    public static int Blue600 =             0xFF1E88E5;
    public static int Blue700 =             0xFF1976D2;
    public static int Blue800 =             0xFF1565C0;
    public static int Blue900 =             0xFF0D47A1;
    public static int BlueAccent100 =       0xFF82B1FF;
    public static int BlueAccent200 =       0xFF448AFF;
    public static int BlueAccent400 =       0xFF2979FF;
    public static int BlueAccent700 =       0xFF2962FF;

    public static int LightBlue50 =         0xFFE1F5FE;
    public static int LightBlue100 =        0xFFB3E5FC;
    public static int LightBlue200 =        0xFF81D4FA;
    public static int LightBlue300 =        0xFF4FC3F7;
    public static int LightBlue400 =        0xFF29B6F6;
    public static int LightBlue500 =        0xFF03A9F4;
    public static int LightBlue600 =        0xFF039BE5;
    public static int LightBlue700 =        0xFF0288D1;
    public static int LightBlue800 =        0xFF0277BD;
    public static int LightBlue900 =        0xFF01579B;
    public static int LightBlueAccent100 =  0xFF80D8FF;
    public static int LightBlueAccent200 =  0xFF40C4FF;
    public static int LightBlueAccent400 =  0xFF00B0FF;
    public static int LightBlueAccent700 =  0xFF0091EA;

    public static int Cyan50 =              0xFFE0F7FA;
    public static int Cyan100 =             0xFFB2EBF2;
    public static int Cyan200 =             0xFF80DEEA;
    public static int Cyan300 =             0xFF4DD0E1;
    public static int Cyan400 =             0xFF26C6DA;
    public static int Cyan500 =             0xFF00BCD4;
    public static int Cyan600 =             0xFF00ACC1;
    public static int Cyan700 =             0xFF0097A7;
    public static int Cyan800 =             0xFF00838F;
    public static int Cyan900 =             0xFF006064;
    public static int CyanAccent100 =       0xFF84FFFF;
    public static int CyanAccent200 =       0xFF18FFFF;
    public static int CyanAccent400 =       0xFF00E5FF;
    public static int CyanAccent700 =       0xFF00B8D4;

    public static int Teal50 =              0xFFE0F2F1;
    public static int Teal100 =             0xFFB2DFDB;
    public static int Teal200 =             0xFF80CBC4;
    public static int Teal300 =             0xFF4DB6AC;
    public static int Teal400 =             0xFF26A69A;
    public static int Teal500 =             0xFF009688;
    public static int Teal600 =             0xFF00897B;
    public static int Teal700 =             0xFF00796B;
    public static int Teal800 =             0xFF00695C;
    public static int Teal900 =             0xFF004D40;
    public static int TealAccent100 =       0xFFA7FFEB;
    public static int TealAccent200 =       0xFF64FFDA;
    public static int TealAccent400 =       0xFF1DE9B6;
    public static int TealAccent700 =       0xFF00BFA5;

    public static int Green50 =             0xFFE8F5E9;
    public static int Green100 =            0xFFC8E6C9;
    public static int Green200 =            0xFFA5D6A7;
    public static int Green300 =            0xFF81C784;
    public static int Green400 =            0xFF66BB6A;
    public static int Green500 =            0xFF4CAF50;
    public static int Green600 =            0xFF43A047;
    public static int Green700 =            0xFF388E3C;
    public static int Green800 =            0xFF2E7D32;
    public static int Green900 =            0xFF1B5E20;
    public static int GreenAccent100 =      0xFFB9F6CA;
    public static int GreenAccent200 =      0xFF69F0AE;
    public static int GreenAccent400 =      0xFF00E676;
    public static int GreenAccent700 =      0xFF00C853;

    public static int LightGreen50 =        0xFFF1F8E9;
    public static int LightGreen100 =       0xFFDCEDC8;
    public static int LightGreen200 =       0xFFC5E1A5;
    public static int LightGreen300 =       0xFFAED581;
    public static int LightGreen400 =       0xFF9CCC65;
    public static int LightGreen500 =       0xFF8BC34A;
    public static int LightGreen600 =       0xFF7CB342;
    public static int LightGreen700 =       0xFF689F38;
    public static int LightGreen800 =       0xFF558B2F;
    public static int LightGreen900 =       0xFF33691E;
    public static int LightGreenAccent100 = 0xFFCCFF90;
    public static int LightGreenAccent200 = 0xFFB2FF59;
    public static int LightGreenAccent400 = 0xFF76FF03;
    public static int LightGreenAccent700 = 0xFF64DD17;

    public static int Lime50 =              0xFFF9FBE7;
    public static int Lime100 =             0xFFF0F4C3;
    public static int Lime200 =             0xFFE6EE9C;
    public static int Lime300 =             0xFFDCE775;
    public static int Lime400 =             0xFFD4E157;
    public static int Lime500 =             0xFFCDDC39;
    public static int Lime600 =             0xFFC0CA33;
    public static int Lime700 =             0xFFAFB42B;
    public static int Lime800 =             0xFF9E9D24;
    public static int Lime900 =             0xFF827717;
    public static int LimeAccent100 =       0xFFF4FF81;
    public static int LimeAccent200 =       0xFFEEFF41;
    public static int LimeAccent400 =       0xFFC6FF00;
    public static int LimeAccent700 =       0xFFAEEA00;

    public static int Yellow50 =            0xFFFFFDE7;
    public static int Yellow100 =           0xFFFFF9C4;
    public static int Yellow200 =           0xFFFFF59D;
    public static int Yellow300 =           0xFFFFF176;
    public static int Yellow400 =           0xFFFFEE58;
    public static int Yellow500 =           0xFFFFEB3B;
    public static int Yellow600 =           0xFFFDD835;
    public static int Yellow700 =           0xFFFBC02D;
    public static int Yellow800 =           0xFFF9A825;
    public static int Yellow900 =           0xFFF57F17;
    public static int YellowAccent100 =     0xFFFFFF8D;
    public static int YellowAccent200 =     0xFFFFFF00;
    public static int YellowAccent400 =     0xFFFFEA00;
    public static int YellowAccent700 =     0xFFFFD600;

    public static int Amber50 =             0xFFFFF8E1;
    public static int Amber100 =            0xFFFFECB3;
    public static int Amber200 =            0xFFFFE082;
    public static int Amber300 =            0xFFffd54f;
    public static int Amber400 =            0xFFFFCA28;
    public static int Amber500 =            0xFFFFC107;
    public static int Amber600 =            0xFFFFB300;
    public static int Amber700 =            0xFFFFA000;
    public static int Amber800 =            0xFFFF8F00;
    public static int Amber900 =            0xFFFF6F00;
    public static int AmberAccent100 =      0xFFFFE57F;
    public static int AmberAccent200 =      0xFFFFD740;
    public static int AmberAccent400 =      0xFFFFC400;
    public static int AmberAccent700 =      0xFFFFAB00;

    public static int Orange50 =            0xFFFFF3E0;
    public static int Orange100 =           0xFFFFE0B2;
    public static int Orange200 =           0xFFFFCC80;
    public static int Orange300 =           0xFFFFB74D;
    public static int Orange400 =           0xFFFFA726;
    public static int Orange500 =           0xFFFF9800;
    public static int Orange600 =           0xFFFB8C00;
    public static int Orange700 =           0xFFF57C00;
    public static int Orange800 =           0xFFEF6C00;
    public static int Orange900 =           0xFFE65100;
    public static int OrangeAccent100 =     0xFFFFD180;
    public static int OrangeAccent200 =     0xFFFFAB40;
    public static int OrangeAccent400 =     0xFFFF9100;
    public static int OrangeAccent700 =     0xFFFF6D00;

    public static int DeepOrange50 =        0xFFFBE9E7;
    public static int DeepOrange100 =       0xFFFFCCBC;
    public static int DeepOrange200 =       0xFFFFAB91;
    public static int DeepOrange300 =       0xFFFF8A65;
    public static int DeepOrange400 =       0xFFFF7043;
    public static int DeepOrange500 =       0xFFFF5722;
    public static int DeepOrange600 =       0xFFF4511E;
    public static int DeepOrange700 =       0xFFE64A19;
    public static int DeepOrange800 =       0xFFD84315;
    public static int DeepOrange900 =       0xFFBF360C;
    public static int DeepOrangeAccent100 = 0xFFFF9E80;
    public static int DeepOrangeAccent200 = 0xFFFF6E40;
    public static int DeepOrangeAccent400 = 0xFFFF3D00;
    public static int DeepOrangeAccent700 = 0xFFDD2C00;

    public static int Brown50 =             0xFFEFEBE9;
    public static int Brown100 =            0xFFD7CCC8;
    public static int Brown200 =            0xFFBCAAA4;
    public static int Brown300 =            0xFFA1887F;
    public static int Brown400 =            0xFF8D6E63;
    public static int Brown500 =            0xFF795548;
    public static int Brown600 =            0xFF6D4C41;
    public static int Brown700 =            0xFF5D4037;
    public static int Brown800 =            0xFF4E342E;
    public static int Brown900 =            0xFF3E2723;

    public static int Grey50 =              0xFFFAFAFA;
    public static int Grey100 =             0xFFF5F5F5;
    public static int Grey200 =             0xFFEEEEEE;
    public static int Grey300 =             0xFFE0E0E0;
    public static int Grey400 =             0xFFBDBDBD;
    public static int Grey500 =             0xFF9E9E9E;
    public static int Grey600 =             0xFF757575;
    public static int Grey700 =             0xFF616161;
    public static int Grey800 =             0xFF424242;
    public static int Grey900 =             0xFF212121;

    public static int BlueGrey50 =          0xFFECEFF1;
    public static int BlueGrey100 =         0xFFCFD8DC;
    public static int BlueGrey200 =         0xFFB0BEC5;
    public static int BlueGrey300 =         0xFF90A4AE;
    public static int BlueGrey400 =         0xFF78909C;
    public static int BlueGrey500 =         0xFF607D8B;
    public static int BlueGrey600 =         0xFF546E7A;
    public static int BlueGrey700 =         0xFF455A64;
    public static int BlueGrey800 =         0xFF37474F;
    public static int BlueGrey900 =         0xFF263238;

    public static int Black = 0xff000000;
    public static int White = 0xffffffff;

    // Uses Color.HSVToColor to convert HSV color to RGB color
    // Convenience method
    public static int HSVToColor(float hue, float saturation, float value) {
        float[] hsv = {hue, saturation, value};
        return Color.HSVToColor(hsv);
    }

    // Shifts the hue of a color according to HSV
    // Hue is a value from 0 to 360 on the wheel that goes red=>yellow=>green=>cyan=>blue=>magenta=>red=>...
    public static int hueShift(int color, int shift) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[0] += shift;
        if (hsv[0] > 360) hsv[0] -= 360;

        return Color.HSVToColor(hsv);
    }

    // Shifts the saturation of a color according to HSV
    // Saturation is a value form 0=white to 1=color
    public static int saturationShift(int color, float shift) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] += shift;
        if (hsv[1] > 1) hsv[1] = 1;
        if (hsv[1] < 0) hsv[1] = 0;

        return Color.HSVToColor(hsv);
    }

    // Shifts the value of a color according to HSV
    // Value is a value from 0=black to 1=color
    public static int valueShift(int color, float shift) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] += shift;
        if (hsv[2] > 1) hsv[2] = 1;
        if (hsv[2] < 0) hsv[2] = 0;

        return Color.HSVToColor(hsv);
    }

    // Scales the saturation of a color according to HSV
    // Parameter float scale should be a value -1 to 1
    // Saturation is a value from 0=white to 1=color
    public static int saturationScale(int color, float scale) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (scale > 0) {
            hsv[1] += scale * (1 - hsv[1]);
        }
        else {
            hsv[1] += scale * hsv[1];
        }
        if (hsv[1] > 1) hsv[1] = 1;
        if (hsv[1] < 0) hsv[1] = 0;

        return Color.HSVToColor(hsv);
    }

    // Scales the value of a color according to HSV
    // Parameter float scale should be a value -1 to 1 for darker or less dark respectively
    // Value is a value from 0=black to 1=color
    public static int valueScale(int color, float scale) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if (scale > 0) {
            hsv[2] += scale * (1 - hsv[2]);
        }
        else {
            hsv[2] += scale * hsv[2];
        }
        if (hsv[2] > 1) hsv[2] = 1;
        if (hsv[2] < 0) hsv[2] = 0;

        return Color.HSVToColor(hsv);
    }

    // Opposite HSV color
    public static int complementaryColor(int color) {
        return hueShift(color, 180);
    }

    // Makes an rgb color darker by a given percentage
    // percentage must be a value in the range [0,1.0f]
    public static int darkenColor(int color, float percentage) {
        int a, r, g, b;
        a = Color.alpha(color);
        r = Color.red(color);
        g = Color.green(color);
        b = Color.blue(color);

        percentage = 1 - percentage;
        r *= percentage;
        g *= percentage;
        b *= percentage;

        return Color.argb(a, r, g, b);
    }

    // Makes an rgb color brighter by a given percentage
    // percentage must be a value in the range [0,1.0f]
    public static int brightenColor(int color, float percentage) {
        int a, r, g, b;
        a = Color.alpha(color);
        r = Color.red(color);
        g = Color.green(color);
        b = Color.blue(color);

        r += (int)((0xff - r) * percentage);
        g += (int)((0xff - g) * percentage);
        b += (int)((0xff - b) * percentage);

        return Color.argb(a, r, g, b);
    }

    // Overloads
    public static int saturationShift(int color, double shift) {
        return saturationShift(color, (float)shift);
    }

    public static int valueShift(int color, double shift) {
        return valueShift(color, (float)shift);
    }

    public static int saturationScale(int color, double scale) {
        return saturationScale(color, (float)scale);
    }

    public static int valueScale(int color, double scale) {
        return valueScale(color, (float)scale);
    }

    public static int darkenColor(int color, double percentage) {
        return darkenColor(color, (float)percentage);
    }

    public static int brightenColor(int color, double percentage) {
        return brightenColor(color, (float)percentage);
    }

    public static int HSVToColor(double hue, double saturation, double value) {
        return HSVToColor((float)hue, (float)saturation, (float)value);
    }
}
