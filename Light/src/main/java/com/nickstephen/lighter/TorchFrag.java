package com.nickstephen.lighter;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.List;

/**
 * Created by Nick Stephen on 3/02/14.
 */
public final class TorchFrag extends Fragment implements ToggleButton.OnCheckedChangeListener {
    public static final String FRAG_TAG = "com.nickstephen.lighter.TorchFrag";

    public static TorchFrag newInstance() {
        return new TorchFrag();
    }

    private Camera mCamera;
    private boolean mIsFlashOn = false;
    private boolean mHasFlash = false;
    private boolean mCameraLoaded = false;
    private CamLoader mCameraLoader;
    private CamStart mCameraStarter;
    private ToggleButton mTorchButton;
    private ContentLoadingProgressBar mProgressBar;
    private View mErrorImg;
    private TextView mBatteryLevelText;
    private BatteryHandler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRetainInstance(true);
        //noinspection ConstantConditions
        if (this.getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            mHasFlash = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.torch_frag, null);

        assert rootView != null;
        mTorchButton = (ToggleButton) rootView.findViewById(R.id.torch_button);
        mTorchButton.setOnCheckedChangeListener(this);

        mProgressBar = (ContentLoadingProgressBar) rootView.findViewById(R.id.progress_bar);

        mErrorImg = rootView.findViewById(R.id.error_img);

        if (!mHasFlash) {
            mProgressBar.hide();
            mErrorImg.setVisibility(View.VISIBLE);
        }

        mBatteryLevelText = (TextView) rootView.findViewById(R.id.battery_text);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mHandler = new BatteryHandler();
        mHandler.sendEmptyMessage(BatteryHandler.MSG_REFRESH);

        if (mHasFlash) {
            mCameraLoader = new CamLoader();
            mCameraLoader.execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mHandler.removeMessages(BatteryHandler.MSG_REFRESH);

        if (mCameraLoader != null) {
            mCameraLoader.cancel(true);
            mCameraLoader = null;
        }

        if (mCameraStarter != null) {
            while (!mCameraStarter.hasFinished()) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    Util.logD(FRAG_TAG, "InterruptedException whilst waiting: " + e.getMessage());
                }
            }
        }

        if (mCameraLoaded && mCamera != null) {
            boolean swap = mIsFlashOn;
            turnOffTorch();
            mCamera.release();
            mCamera = null;
            mCameraLoaded = false;
            mIsFlashOn = swap;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mTorchButton = null;
        mProgressBar = null;
        mErrorImg = null;
        mBatteryLevelText = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (mCameraLoaded && mCamera != null) {
            if (isChecked) {
                if (mCameraStarter != null) {
                    while (!mCameraStarter.hasFinished()) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Util.logD(FRAG_TAG, "InterruptedException whilst waiting: " + e.getMessage());
                        }
                    }
                }
                mCameraStarter = new CamStart();
                mCameraStarter.execute();
            } else {
                if (mCameraStarter != null) {
                    while (!mCameraStarter.hasFinished()) {
                        try {
                            Thread.sleep(20);
                        } catch (InterruptedException e) {
                            Util.logD(FRAG_TAG, "InterruptedException whilst waiting: " + e.getMessage());
                        }
                    }
                }
                turnOffTorch();
            }
        }
    }

    private void turnOnTorch() {
        if (!mIsFlashOn) {
            if (mCamera == null) {
                return;
            }

            try {
                Camera.Parameters cameraParams = mCamera.getParameters();
                cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(cameraParams);
                mCamera.startPreview();
                mIsFlashOn = true;
            } catch (RuntimeException e) {
                Util.logD(FRAG_TAG, "Error starting camera preview: " + e.getMessage());
            }
        }
    }

    private void turnOffTorch() {
        if (mIsFlashOn) {
            if (mCamera == null) {
                return;
            }

            try {
                Camera.Parameters cameraParams = mCamera.getParameters();
                cameraParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(cameraParams);
                mCamera.stopPreview();
                mIsFlashOn = false;
            } catch (RuntimeException e) {
                Util.logD(FRAG_TAG, "Error stopping preview: " + e.getMessage());
            }
        }
    }

    private class CamLoader extends AsyncTask<Void, Void, Camera> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mCameraLoaded = false;
            mProgressBar.show();
        }

        @Override
        protected Camera doInBackground(Void... voids) {
            try {
                Camera cam = Camera.open();

                if (isCancelled() && cam != null) {
                    cam.release();
                    return null;
                }

                return cam;
            } catch (RuntimeException e) {
                Util.logD(FRAG_TAG, "Error loading camera: " + e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Camera camera) {
            if (camera == null) {
                Util.logD("CamLoader", "Error loading camera!");
                if (mErrorImg != null) {
                    mErrorImg.setVisibility(View.VISIBLE);
                }
                return;
            }

            Camera.Parameters cameraParams = camera.getParameters();
            List<String> flashModes = cameraParams.getSupportedFlashModes();
            if (flashModes != null) {
                for (String mode : flashModes) {
                    if (Camera.Parameters.FLASH_MODE_TORCH.compareTo(mode) == 0) {
                        mCamera = camera;
                        mCameraLoaded = true;
                        if (mTorchButton != null) {
                            mTorchButton.setVisibility(View.VISIBLE);

                            if (mIsFlashOn) {
                                mIsFlashOn = false;
                                mTorchButton.setChecked(true);
                                mCameraStarter = new CamStart();
                                mCameraStarter.execute();
                            }
                        }
                        if (mProgressBar != null) {
                            mProgressBar.hide();
                        }
                        return;
                    }
                }
            }

            Util.logD(FRAG_TAG, "No torch flash mode found!");
            camera.release();
            if (mErrorImg != null) {
                mErrorImg.setVisibility(View.VISIBLE);
            }
        }
    }

    private class CamStart extends AsyncTask<Void, Void, Void> {
        private boolean mIsFinished = false;

        @Override
        protected Void doInBackground(Void... voids) {
            turnOnTorch();
            mIsFinished = true;
            return null;
        }

        public boolean hasFinished() {
            return mIsFinished;
        }
    }

    private class BatteryHandler extends Handler {
        private static final int MSG_REFRESH = 0x10;

        private final IntentFilter mIFilter;

        public BatteryHandler() {
            super();

            mIFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH:
                    Intent batteryStatus = TorchFrag.this.getActivity().registerReceiver(null, mIFilter);
                    if (batteryStatus != null) {
                        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

                        String battPct = Float.valueOf(100 * level / (float) scale).toString().split("\\.")[0];

                        if (mBatteryLevelText != null) {
                            mBatteryLevelText.setText(battPct + "%");
                        }
                    }
                    this.sendEmptyMessageDelayed(MSG_REFRESH, 2000);

                    break;
            }
        }
    }
}
