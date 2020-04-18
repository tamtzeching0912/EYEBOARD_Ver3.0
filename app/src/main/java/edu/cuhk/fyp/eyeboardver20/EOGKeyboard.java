package edu.cuhk.fyp.eyeboardver20;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.InputConnection;

import static android.content.ContentValues.TAG;
import static java.lang.String.valueOf;

public class EOGKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {

    private static KeyboardView kv;
    private static Keyboard keyboard;
    private static EOGKeyboard instance;

    private boolean isCaps = false;
    private int quart = 0000;

    public static EOGKeyboard getInstance(){
        Log.d(TAG, "getInstance: get "+ instance);
        return instance;
    }

    @Override
    public KeyboardView onCreateInputView() {
        instance = this;
        Log.d(TAG, "onCreateInputView: create instance " + this);
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboard = new Keyboard(this, R.xml.qwerty, R.integer.keyboard_quart_0);
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        quart = 0000;
        Log.d(TAG, "onCreateInputView: init quart "+quart);
        return kv;
    }

    private void playClick(int i) {
        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(i){
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:
            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);

        }
    }

    @Override
    public void onPress(int primaryCode) {

    }

    @Override
    public void onRelease(int primaryCode) {

    }

    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        playClick(primaryCode);
        switch(primaryCode) {
            case 0000: quart = R.integer.keyboard_quart_0; break;
            case 1000: quart = R.integer.keyboard_quart_1; break;
            case 2000: quart = R.integer.keyboard_quart_2; break;
            case 3000: quart = R.integer.keyboard_quart_3; break;
            case 4000: quart = R.integer.keyboard_quart_4; break;
            case 5000: quart = R.integer.keyboard_quart_5; break;
            case 11000: quart = R.integer.keyboard_quart_1_1; break;
            case 12000: quart = R.integer.keyboard_quart_1_2; break;
            case 13000: quart = R.integer.keyboard_quart_1_3; break;
            case 14000: quart = R.integer.keyboard_quart_1_4; break;
            case 21000: quart = R.integer.keyboard_quart_2_1; break;
            case 22000: quart = R.integer.keyboard_quart_2_2; break;
            case 23000: quart = R.integer.keyboard_quart_2_3; break;
            case 31000: quart = R.integer.keyboard_quart_3_1; break;
            case 32000: quart = R.integer.keyboard_quart_3_2; break;
            case 33000: quart = R.integer.keyboard_quart_3_3; break;
            case 34000: quart = R.integer.keyboard_quart_3_4; break;
            case 41000: quart = R.integer.keyboard_quart_4_1; break;
            case 42000: quart = R.integer.keyboard_quart_4_2; break;
            case 43000: quart = R.integer.keyboard_quart_4_3; break;
            case 44000: quart = R.integer.keyboard_quart_4_4; break;
            case Keyboard.KEYCODE_DELETE:
                ic.deleteSurroundingText(1,0);
                break;
            case Keyboard.KEYCODE_SHIFT:
                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                kv.invalidateAllKeys();
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                char code = (char)primaryCode;
                if(Character.isLetter(code) && isCaps)
                    code = Character.toUpperCase(code);
                ic.commitText(valueOf(code), 1);
        }
        if(quart > 0) kv.setKeyboard(new Keyboard(this, R.xml.qwerty, quart));
    }

    /**
     * dir
     * 0 center, 1 top, 2 right, 3 bottom, 4 left
     */
    public void dirToQuart(int dir) {
        int primaryCode = 0000;
        int quarts[]    = { 5000, 2000,  3000,  4000,  1000 };
        int quarts1[]   = { 0000, 11000, 13000, 14000, 12000 };
        int quarts11[]  = { 1000, 97,    99,    100,   98 };
        int quarts12[]  = { 1000, 101,   103,   104,   102 };
        int quarts13[]  = { 1000, 105,   107,   108,   106 };
        int quarts14[]  = { 0000, 109,   111,   112,   110 };

        int quarts2[]   = { 0000, 21000, 23000, 0000,  22000 };
        int quarts21[]  = { 2000, 113,   115,   116,   114 };
        int quarts22[]  = { 2000, 117,   119,   120,   118 };
        int quarts23[]  = { 2000, 2000,  122,   2000,  121 };

        int quarts3[]   = { 0000, 31000, 33000, 34000, 32000 };
        int quarts31[]  = { 3000, 48,    50,    51,   49 };
        int quarts32[]  = { 3000, 52,    54,    55,   53 };
        int quarts33[]  = { 3000, 56,    43,    45,   57 };
        int quarts34[]  = { 3000, 47,    42,    37,   61 };

        int quarts4[]   = { 0000, 41000, 43000, 32, 42000 };
        int quarts41[]  = { 4000, 46,    63,    33,    44 };
        int quarts42[]  = { 4000, 39,    40,    41,    34 };
        int quarts43[]  = { 4000, 64,    95,    35,    38 };
        int quarts44[]  = { 4000, 32,    4000,  4000,  4000 };

        int quarts5[]   = { 0000, -5,    0000,  -4,    0000 };

        switch(quart) {
            case R.integer.keyboard_quart_0: primaryCode = quarts[dir];break;
            case R.integer.keyboard_quart_1: primaryCode = quarts1[dir]; break;
            case R.integer.keyboard_quart_2: primaryCode = quarts2[dir]; break;
            case R.integer.keyboard_quart_3: primaryCode = quarts3[dir]; break;
            case R.integer.keyboard_quart_4: primaryCode = quarts4[dir]; break;
            case R.integer.keyboard_quart_5: primaryCode = quarts5[dir]; break;
            case R.integer.keyboard_quart_1_1: primaryCode = quarts11[dir]; break;
            case R.integer.keyboard_quart_1_2: primaryCode = quarts12[dir]; break;
            case R.integer.keyboard_quart_1_3: primaryCode = quarts13[dir]; break;
            case R.integer.keyboard_quart_1_4: primaryCode = quarts14[dir]; break;
            case R.integer.keyboard_quart_2_1: primaryCode = quarts21[dir]; break;
            case R.integer.keyboard_quart_2_2: primaryCode = quarts22[dir]; break;
            case R.integer.keyboard_quart_2_3: primaryCode = quarts23[dir]; break;
            case R.integer.keyboard_quart_3_1: primaryCode = quarts31[dir]; break;
            case R.integer.keyboard_quart_3_2: primaryCode = quarts32[dir]; break;
            case R.integer.keyboard_quart_3_3: primaryCode = quarts33[dir]; break;
            case R.integer.keyboard_quart_3_4: primaryCode = quarts34[dir]; break;
            case R.integer.keyboard_quart_4_1: primaryCode = quarts41[dir]; break;
            case R.integer.keyboard_quart_4_2: primaryCode = quarts42[dir]; break;
            case R.integer.keyboard_quart_4_3: primaryCode = quarts43[dir]; break;
            case R.integer.keyboard_quart_4_4: primaryCode = quarts44[dir]; break;
        }
        onPress(primaryCode);
        onKey(primaryCode, null);
    }

    @Override
    public void onText(CharSequence text) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}
