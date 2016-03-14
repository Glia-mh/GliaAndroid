package com.team.roots;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by adityaaggarwal on 2/7/16.
 */
public class MyClearEditText extends EditText {
    public MyClearEditText(Context context) {
        super(context);
    }

    public MyClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyClearEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


  //  @Override
   /* public boolean onKeyPreIme(int keyCode, KeyEvent event)
    {
    if(keyCode == KeyEvent.KEYCODE_BACK)
    {
        clearFocus();
    }
    return super.onKeyPreIme(keyCode, event);
}*/
}
