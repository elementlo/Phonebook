package com.weikuo.elemenzhang.phonebookwk.view;

import android.content.Context;

/**
 * Created by elemenzhang on 2017/6/13.
 */

public interface IprogressBar {
    void showProgressbar();
    void dismissProgressbar();
    void shoProgressdialog(Context context);
    void dismissProgressdialog();
}
