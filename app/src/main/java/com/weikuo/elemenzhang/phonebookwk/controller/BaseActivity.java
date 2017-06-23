package com.weikuo.elemenzhang.phonebookwk.controller;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.weikuo.elemenzhang.phonebookwk.view.IprogressBar;
import com.weikuo.elemenzhang.phonebookwk.view.customview.CommonProgressbar;

/**
 * Created by elemenzhang on 2017/6/13.
 */

public class BaseActivity extends AppCompatActivity implements IprogressBar{
    private CommonProgressbar progressbar;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    public void showProgressbar() {
        progressbar=new CommonProgressbar(this);
        progressbar.showProgress();
    }

    @Override
    public void dismissProgressbar() {
        progressbar.hideProgress();
    }

    @Override
    public void shoProgressdialog(Context context) {

    }

    @Override
    public void dismissProgressdialog() {

    }

}
