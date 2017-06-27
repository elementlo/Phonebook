package com.weikuo.elemenzhang.phonebookwk.view.customview;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.weikuo.elemenzhang.phonebookwk.R;

/**
 * Created by elemenzhang on 2017/6/26.
 */

public class CustomDialog extends Dialog {
    private Context mContext;
    private Button btnDone;
    private TextView tvItemNum;

    public CustomDialog(@NonNull Context context) {
        super(context, R.style.quick_option_dialog);
        this.mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_custom, null);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.setContentView(view);
        Window win = getWindow();
        if (win != null) {
            WindowManager.LayoutParams lp = win.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            win.setAttributes(lp);
            win.setGravity(Gravity.BOTTOM);
        }
        btnDone = (Button) view.findViewById(R.id.btn_done);
        tvItemNum = (TextView) view.findViewById(R.id.tv_itemnum);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override
    public void show() {
        super.show();

    }

    public Button getDoneButton() {
        return btnDone;
    }

    public TextView getTvItemNum() {
        return tvItemNum;
    }
}
