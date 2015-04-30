package com.tealium.kitchensink.fragment;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;

import com.tealium.kitchensink.R;
import com.tealium.kitchensink.helper.TMSHelper;
import com.tealium.kitchensink.model.Model;
import com.tealium.kitchensink.util.Constant;

/**
 * A placeholder fragment containing a simple view.
 */
public class TealiumFeaturesFragment extends StyleableFragment implements View.OnClickListener {

    private final BroadcastReceiver colorChangeReveiver;
    private final IntentFilter colorChangeIntentFilter;

    public TealiumFeaturesFragment() {
        this.colorChangeReveiver = createColorChangeBroadcastReceiver();
        this.colorChangeIntentFilter = new IntentFilter(Constant.LocalBroadcast.ACTION_COLOR_CHANGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_tealium_features, container, false);

        Model model = this.getModel(rootView.getContext());

        AutoCompleteTextView firstnameTextView = (AutoCompleteTextView) rootView.findViewById(R.id.tealium_features_firstname_actextview);
        firstnameTextView.setAdapter(model.getFirstNameAdapter());
        firstnameTextView.setText(model.getCurrentFirstName());

        rootView.findViewById(R.id.tealium_features_button_personalize)
                .setOnClickListener(this);

        rootView.findViewById(R.id.tealium_features_button_unlock)
                .setOnClickListener(this);


        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this.getActivity()).registerReceiver(
                this.colorChangeReveiver, this.colorChangeIntentFilter);
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(this.getActivity()).unregisterReceiver(
                this.colorChangeReveiver);
        super.onPause();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.tealium_features_button_personalize) {

            AutoCompleteTextView editText = (AutoCompleteTextView) getActivity()
                    .findViewById(R.id.tealium_features_firstname_actextview);

            final String name = editText.getText().toString();

            this.getModel(v.getContext()).setDefaultFirstName(name);

            TMSHelper.trackEvent("personalize", name);

        } else if (v.getId() == R.id.tealium_features_button_unlock) {
            TMSHelper.trackEvent("unlock", "true");
        }
    }

    private BroadcastReceiver createColorChangeBroadcastReceiver() {
        return new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {

                final View view = getView();
                if (view == null) {
                    return;
                }

                final int oldColor = intent.getIntExtra(
                        Constant.LocalBroadcast.EXTRA_OLD_COLOR, Color.WHITE);

                final int newColor = intent.getIntExtra(
                        Constant.LocalBroadcast.EXTRA_NEW_COLOR, Color.WHITE);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    view.setBackgroundColor(newColor);
                    return;
                }

                animateBackground(view, oldColor, newColor);
            }

            @TargetApi(11)
            private void animateBackground(final View v, int oldColor, int newColor) {
                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), oldColor, newColor);
                colorAnimation.setDuration(1000);
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        v.setBackgroundColor((Integer) animator.getAnimatedValue());
                    }
                });
                colorAnimation.start();
            }
        };
    }
}
