package com.nickstephen.lighter;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by Nick Stephen on 3/02/14.
 */
public final class ScreenFrag extends Fragment implements SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {
    public static final String FRAG_TAG = "com.nickstephen.lighter.ScreenFrag";

    public static ScreenFrag newInstance() {
        return new ScreenFrag();
    }

    private int mBrightness = 200;
    private Window mWindow;
    private ContentResolver mResolver;
    private boolean mShouldChangeSystemBrightness = true;
    private boolean mKeepScreenOn = false;
    private boolean mShouldRememberBrightness;
    private TextView mBrightnessText;
    private CheckBox mScreenOnCheck;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mResolver = this.getActivity().getContentResolver();

        try {
            mBrightness = android.provider.Settings.System.getInt(mResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (android.provider.Settings.SettingNotFoundException e) {
            Util.logD(FRAG_TAG, "System brightness setting not found!");
        }

        if (mShouldRememberBrightness = SettingsAccessor.shouldRememberBrightness(this.getActivity())) {
            int val = SettingsAccessor.getBrightnessLevel(this.getActivity());
            if (val != -1) {
                mBrightness = val;
            }
        }
        mKeepScreenOn = SettingsAccessor.shouldKeepScreenOn(this.getActivity());

        mWindow = this.getActivity().getWindow();
    }

    @Override
    public void onResume() {
        super.onResume();

        mScreenOnCheck.setChecked(mKeepScreenOn);

        mShouldChangeSystemBrightness = SettingsAccessor.shouldManageSystemBrightness(this.getActivity());

        if (mShouldRememberBrightness) {
            WindowManager.LayoutParams params = mWindow.getAttributes();
            params.screenBrightness = mBrightness / (float) 255;
            mWindow.setAttributes(params);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mShouldChangeSystemBrightness) {
            android.provider.Settings.System.putInt(mResolver, Settings.System.SCREEN_BRIGHTNESS, mBrightness);
        }
        SettingsAccessor.saveBrightnessLevel(this.getActivity(), mBrightness);
    }

    @Override
    public void onStop() {
        super.onStop();

        SettingsAccessor.setShouldKeepScreenOn(this.getActivity(), mKeepScreenOn);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.screen_frag, null);

        mBrightnessText = (TextView) rootView.findViewById(R.id.brightness_text);

        SeekBar lightBar = (SeekBar) rootView.findViewById(R.id.light_bar);
        lightBar.setOnSeekBarChangeListener(this);
        lightBar.setProgress(mBrightness);

        mScreenOnCheck = (CheckBox) rootView.findViewById(R.id.checkbox_screen_on);
        mScreenOnCheck.setOnCheckedChangeListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setKeepScreenOn(mKeepScreenOn);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mBrightnessText = null;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress <= 10) {
            progress = 10;
        }

        WindowManager.LayoutParams params = mWindow.getAttributes();
        params.screenBrightness = progress / (float) 255;
        mWindow.setAttributes(params);

        mBrightness = progress;

        String text = Float.valueOf(100 * mBrightness / (float) 255).toString().split("\\.")[0];

        mBrightnessText.setText(text + "%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
        this.getView().setKeepScreenOn(mKeepScreenOn = checked);
    }
}
