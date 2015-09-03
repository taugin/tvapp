package com.android.tvapp.view;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import com.android.tvapp.R;
import com.android.tvapp.util.Log;
import com.android.tvapp.util.Utils;

public class CustomViewFlipper extends ViewFlipper {

    private ArrayList<Animation> mInAnimations;
    private ArrayList<Animation> mOutAnimations;

    public CustomViewFlipper(Context context) {
        super(context);
        init(context);
    }

    public CustomViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mInAnimations = new ArrayList<Animation>();
        mOutAnimations = new ArrayList<Animation>();
        Animation animation = null;
        animation = AnimationUtils.loadAnimation(context, R.anim.push_up_in);
        mInAnimations.add(animation);
        animation = AnimationUtils.loadAnimation(context, R.anim.push_up_out);
        mOutAnimations.add(animation);
        animation = AnimationUtils.loadAnimation(context, R.anim.push_left_in);
        mInAnimations.add(animation);
        animation = AnimationUtils.loadAnimation(context, R.anim.push_left_out);
        mOutAnimations.add(animation);

        animation = AnimationUtils.loadAnimation(context, R.anim.alpha_in);
        mInAnimations.add(animation);
        animation = AnimationUtils.loadAnimation(context, R.anim.alpha_out);
        mOutAnimations.add(animation);

        animation = AnimationUtils.loadAnimation(context, R.anim.rotate_in);
        mInAnimations.add(animation);
        animation = AnimationUtils.loadAnimation(context, R.anim.rotate_out);
        mOutAnimations.add(animation);
    }

    @Override
    public void showNext() {
        if (Utils.USE_TEST_MODE) {
            long time = System.currentTimeMillis();
            int index1 = new Random(time).nextInt(mInAnimations.size());
            int index2 = new Random(time).nextInt(mOutAnimations.size());
            setInAnimation(mInAnimations.get(index1));
            setOutAnimation(mOutAnimations.get(index2));
        }
        super.showNext();
    }
}
