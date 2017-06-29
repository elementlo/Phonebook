package com.weikuo.elemenzhang.phonebookwk.utils;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by elemenzhang on 2017/5/11.
 */

public class BaseTaskSwitch implements Application.ActivityLifecycleCallbacks {
    public int mCount = 0;
    private boolean isStoped = false;
    private OnTaskSwitchListener mOnTaskSwitchListener;
    private static BaseTaskSwitch mBaseLifecycle;

    public static BaseTaskSwitch init(Application application) {
        if (null == mBaseLifecycle) {
            mBaseLifecycle = new BaseTaskSwitch();
            application.registerActivityLifecycleCallbacks(mBaseLifecycle);
        }
        return mBaseLifecycle;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(Activity activity) {
        if (mCount++ == 0 && isStoped) {
            mOnTaskSwitchListener.onTaskSwitchToForeground();
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {}

    @Override
    public void onActivityPaused(Activity activity) {}

    @Override
    public void onActivityStopped(Activity activity) {
        if (--mCount == 0) {
            isStoped = true;
            mOnTaskSwitchListener.onTaskSwitchToBackground();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

    @Override
    public void onActivityDestroyed(Activity activity) {}

    public void setOnTaskSwitchListener(OnTaskSwitchListener listener) {
        mOnTaskSwitchListener = listener;
    }

    public interface OnTaskSwitchListener {

        void onTaskSwitchToForeground();

        void onTaskSwitchToBackground();
    }
}
