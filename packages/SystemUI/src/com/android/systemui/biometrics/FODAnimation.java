/**
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.biometrics;
import android.provider.Settings;
import android.content.Context;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.content.Intent;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.tuner.TunerService;
public class FODAnimation extends ImageView implements TunerService.Tunable {

    private final String FOD_RECOGNIZING_ANIMATION = "system:" + Settings.System.FOD_RECOGNIZING_ANIMATION;
    private final WindowManager.LayoutParams mAnimParams = new WindowManager.LayoutParams();

    private boolean mShowing = false;
    private Context mContext;
    private int mAnimationSize;
    private int mAnimationPositionY;
    private int mIsRecognizingAnim;

    private AnimationDrawable recognizingAnim;
    private WindowManager mWindowManager;

    private String[] ANIMATION_STYLES_NAMES = {
        "fod_miui_normal_recognizing_anim",
        "fod_miui_normal_recognizing_anim",
        "fod_miui_aod_recognizing_anim",
        "fod_miui_light_recognizing_anim",
        "fod_miui_pop_recognizing_anim",
        "fod_miui_pulse_recognizing_anim",
        "fod_miui_pulse_recognizing_white_anim",
        "fod_miui_rhythm_recognizing_anim",
        "fod_op_cosmos_recognizing_anim",
        "fod_op_mclaren_recognizing_anim",
        "fod_op_stripe_recognizing_anim",
        "fod_op_wave_recognizing_anim",
        "fod_pureview_dna_recognizing_anim",
        "fod_pureview_future_recognizing_anim",
        "fod_pureview_halo_ring_recognizing_anim",
        "fod_pureview_molecular_recognizing_anim",
        "fod_miui_aurora_recognizing_anim",
        "fod_op_energy_recognizing_anim",
        "fod_op_ripple_recognizing_anim",
        "fod_blue_firework_recognizing_anim",
        "fod_coloros7_1_recognizing_anim",
        "fod_coloros7_2_recognizing_anim"
    };

    private final String FOD_ANIMATIONS_PACKAGE = "com.pixelish.fod.animations";

    private static final boolean DEBUG = true;
    private static final String LOG_TAG = "FODAnimations";


    public FODAnimation(Context context, int mPositionX, int mPositionY) {
        super(context);

        mContext = context;
        mWindowManager = mContext.getSystemService(WindowManager.class);

        mAnimationSize = mContext.getResources().getDimensionPixelSize(R.dimen.fod_animation_size);
        mAnimParams.height = mAnimationSize;
        mAnimParams.width = mAnimationSize;

        mAnimParams.format = PixelFormat.TRANSLUCENT;
        mAnimParams.type = WindowManager.LayoutParams.TYPE_VOLUME_OVERLAY; // it must be behind FOD icon
        mAnimParams.flags =  WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mAnimParams.gravity = Gravity.TOP | Gravity.CENTER;
        mAnimParams.y = mPositionY - (mAnimationSize / 2);

        setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        Dependency.get(TunerService.class).addTunable(this, FOD_RECOGNIZING_ANIMATION);

    }

    private void updateAnimationStyle(String drawableName) {
    if (DEBUG) Log.i(LOG_TAG, "Updating animation style to:" + drawableName);
    int resId = 0;
    try {
        PackageManager pm = mContext.getPackageManager();
        Resources mApkResources = pm.getResourcesForApplication(FOD_ANIMATIONS_PACKAGE);
        resId = mApkResources.getIdentifier(drawableName, "drawable", FOD_ANIMATIONS_PACKAGE);
        if (DEBUG) Log.i(LOG_TAG, "Got resource id: "+ resId +" from package" );
        setBackgroundDrawable(mApkResources.getDrawable(resId));
        recognizingAnim = (AnimationDrawable) getBackground();
    }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onTuningChanged(String key, String newValue) {
        mIsRecognizingAnim = newValue == null ? 0 : Integer.valueOf(newValue);
        updateAnimationStyle(ANIMATION_STYLES_NAMES[mIsRecognizingAnim]);
    }

    public void updateParams(int mDreamingOffsetY) {
        mAnimParams.y = mDreamingOffsetY - (mAnimationSize / 2);
    }

    public void showFODanimation() {
        if (mIsRecognizingAnim != 0 && getParent()  == null) {
            mWindowManager.addView(this, mAnimParams);
            mWindowManager.updateViewLayout(this, mAnimParams);
            recognizingAnim.start();
        }
    }

    public void hideFODanimation() {
        if (getWindowToken() != null) {
            clearAnimation();
            recognizingAnim.stop();
            recognizingAnim.selectDrawable(0);
            mWindowManager.removeView(this);
        }
    }
}
