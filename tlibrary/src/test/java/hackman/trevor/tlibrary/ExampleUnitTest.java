package hackman.trevor.tlibrary;

import org.junit.Test;

import hackman.trevor.tlibrary.library.TMath;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void test() throws Exception {
        System.out.print(TMath.complementaryColor(0x40FF00));
        System.out.print(TMath.complementaryColor(0xFF4000));
        System.out.print(TMath.complementaryColor(0xFFFF40));
        System.out.print(TMath.complementaryColor(0x2020FF));
    }
}