package com.example.shubham.mycamera;

import android.content.Context;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Camera Fragment For capturing image and passing camera data to mPreviewFrameLayout fragment
 * <p>
 * created by shubham
 * date : 6/20/2017
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class CameraFragment extends Fragment implements View.OnClickListener, Camera.PictureCallback {
    //key for putting camera data in bundle
    public static final String EXTRA_CAMERA_DATA = "camera_data";
    public static final String CAMERA_ORIENTATION = "orientation";
    //tag for log
    private static final String TAG = CameraFragment.class.getSimpleName();
    // Native camera.
    private Camera mCamera;
    // View to display the camera output.
    private CameraPreview mPreview;
    //View for fragment
    private View mView;
    //TextView for Capturing Image
    private TextView mCaptureTextView;
    //ImageView for changing camera
    private ImageView mCameraChangeImageView;
    //Thread for opening Camera in new thread
    private CameraHandlerThread mThread = null;
    //frame layout on which surface view will be pasted
    private FrameLayout mPreviewFrameLayout;
    private int mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
    private RelativeLayout mOuterRelativeLayout;

    public CameraFragment() {
        Log.d(TAG, "In Constructor");
        ;
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "In onCreate");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d(TAG, "In onCreateView");
        mView = inflater.inflate(R.layout.fragment_camera, container, false);
//        mOuterRelativeLayout=(RelativeLayout)mView.findViewById(R.id.outer_relative_layout);
//        Display display = ((WindowManager) MyApplication.getAppContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
//        int height=display.getHeight();
//        int width=display.getWidth();
//        RelativeLayout.LayoutParams cameraPreviewLayoutParams = new RelativeLayout.LayoutParams(width, height);
//        mOuterRelativeLayout.setLayoutParams(cameraPreviewLayoutParams);
        mPreviewFrameLayout = (FrameLayout) mView.findViewById(R.id.camera_preview);
        openCamera(mCameraIndex);
        setPreview();
        mCaptureTextView = (TextView) mView.findViewById(R.id.capture_text_view);
        mCameraChangeImageView = (ImageView) mView.findViewById(R.id.change_camera_imv);


        int cameraCount = Camera.getNumberOfCameras();
        if (cameraCount > 1) {
            mCameraChangeImageView.setVisibility(View.VISIBLE);
            mCameraChangeImageView.setOnClickListener(this);
        } else {
            mCameraChangeImageView.setVisibility(View.INVISIBLE);
        }
        mCaptureTextView.setOnClickListener(this);

        return mView;
    }

    private void setPreview() {
        if (mCamera != null) {
            mPreview = new CameraPreview(getActivity().getBaseContext(), mCamera, mView);
            mPreviewFrameLayout.addView(mPreview);
            mPreview.startCameraPreview();
        } else {
            Toast.makeText(getContext(), "Error in opening Camera", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "In onAttach");

    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "In onDetach");

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "In onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "In onPause");
        mCamera.stopPreview();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "In onDestroy");
        mCamera.release();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.capture_text_view) {
            mCamera.takePicture(null, null, this);
            Toast.makeText(getContext(), "Captured Clicked", Toast.LENGTH_SHORT).show();

        } else if (view.getId() == R.id.change_camera_imv) {
            Log.d(TAG, "on Change Camera");

            mPreview.surfaceDestroyed(mPreview.getHolder());
            mPreview.getHolder().removeCallback(mPreview);
            mPreview.destroyDrawingCache();
            mPreviewFrameLayout.removeView(mPreview);
            releaseCameraAndPreview();

            if (mCameraIndex == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraIndex = Camera.CameraInfo.CAMERA_FACING_FRONT;
            } else {
                mCameraIndex = Camera.CameraInfo.CAMERA_FACING_BACK;
            }
            openCamera(mCameraIndex);
            setPreview();
        } else {
            //do nothing
        }
    }

    private void openCamera(int id) {
        if (mThread == null) {
            mThread = new CameraHandlerThread(TAG, id);
        }

        synchronized (mThread) {
            mThread.openCamera(id);
        }
    }

    private void safeCameraOpen(int id) {

        try {
            //releaseCameraAndPreview();
            mCamera = Camera.open(id);
            Log.d(TAG, mCamera.toString());
        } catch (Exception e) {
            Log.e(TAG, "failed to open Camera");
            e.printStackTrace();
        }
    }

    private void releaseCameraAndPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();

        }
    }

    /**
     * @param sizes  :List of sizes
     * @param width  :int
     * @param height : int
     * @return :camera sizes
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio;

        // Check whether is portrait or landscape
        if (mCameraIndex == 1)
            targetRatio = (double) height / width;
        else
            targetRatio = (double) width / height;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        return optimalSize;
    }


    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        Fragment previewFragment = new ActivationPreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(CAMERA_ORIENTATION, mCameraIndex);
        bundle.putByteArray(EXTRA_CAMERA_DATA, data);
        previewFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, previewFragment)
                .commit();
    }

    /**
     * Class for Handling opening of camera in app
     */
    private class CameraHandlerThread extends HandlerThread {
        Handler mHandler = null;
        int mId = -1;

        CameraHandlerThread(String name, int id) {
            super(name);
            mId = id;
            start();
            mHandler = new Handler(getLooper());
        }

        synchronized void notifyCameraOpened() {
            notify();
        }

        void openCamera(final int id) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    safeCameraOpen(id);
                    notifyCameraOpened();
                }
            });
            try {
                wait();
            } catch (InterruptedException e) {
                Log.w(TAG, "wait was interrupted");
                e.printStackTrace();
            }
        }
    }

    /**
     * Camera Preview Class for using Surface View in Frame layout
     */
    private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        // Our Camera.
        public Camera mCamera;
        // SurfaceHolder
        private SurfaceHolder mHolder;
        // Parent Context.
        private Context mContext;

        // Camera Sizing (For rotation, orientation changes)
        private Camera.Size mPreviewSize;

        // List of supported mPreviewFrameLayout sizes
        private List<Camera.Size> mSupportedPreviewSizes;

        // Flash modes supported by this camera
        private List<String> mSupportedFlashModes;

        // View holding this camera.
        private View mCameraView;

        public CameraPreview(Context context, Camera camera, View view) {
            super(context);
            mContext = context;
            mCameraView = view;
            setCamera(camera);
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setKeepScreenOn(true);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        }

        /**
         * Extract supported mPreviewFrameLayout and flash modes from the camera.
         *
         * @param camera :camera instance
         */
        private void setCamera(Camera camera) {
            Log.d("CustomCameraPreview", " SetCamera");
            mCamera = camera;
            mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
            mSupportedFlashModes = mCamera.getParameters().getSupportedFlashModes();

            // Set the camera to Auto Flash mode.
            if (mSupportedFlashModes != null && mSupportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                mCamera.setParameters(parameters);
            }

            requestLayout();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d("CustomCameraPreview", " SurfaceCreated");
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // If your mPreviewFrameLayout can change or rotate, take care of those events here.
            // Make sure to stop the mPreviewFrameLayout before resizing or reformatting it.
            if (holder.getSurface() == null) {
                // mPreviewFrameLayout surface does not exist
                //noinspection UnnecessaryReturnStatement
                return;
            }
//
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

                if (mPreviewSize != null) {
                    Camera.Size size = mPreviewSize;
                    parameters.setPreviewSize(size.width, size.height);
                }
                mCamera.setParameters(parameters);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            setMeasuredDimension(width, height);

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

            final int width;
            final int height;
            int previewWidth;
            int previewHeight;
            if (changed) {
                width = right - left;
                height = bottom - top;

                previewWidth = width;
                previewHeight = height;

                if (mPreviewSize != null) {
                    Display display = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                    switch (display.getRotation()) {
                        case Surface.ROTATION_0:
                            //noinspection SuspiciousNameCombination
                            previewWidth = mPreviewSize.height;
                            //noinspection SuspiciousNameCombination
                            previewHeight = mPreviewSize.width;
                            mCamera.setDisplayOrientation(90);
                            break;
                        case Surface.ROTATION_90:
                            previewWidth = mPreviewSize.width;
                            previewHeight = mPreviewSize.height;
                            break;
                        case Surface.ROTATION_180:
                            //noinspection SuspiciousNameCombination
                            previewWidth = mPreviewSize.height;
                            //noinspection SuspiciousNameCombination
                            previewHeight = mPreviewSize.width;
                            break;
                        case Surface.ROTATION_270:
                            previewWidth = mPreviewSize.width;
                            previewHeight = mPreviewSize.height;
                            mCamera.setDisplayOrientation(180);
                            break;
                    }
                }
                final int scaledChildHeight = previewHeight * width / previewWidth;
                mCameraView.layout(0, scaledChildHeight - height, width, height);
            }
        }

        /**
         * Begin the mPreviewFrameLayout of the camera input.
         */
        private void startCameraPreview() {
            Log.d("CustomCameraPreview", " startCameraPreview");
            try {
                mCamera.setPreviewDisplay(mHolder);
                if (mCameraIndex==Camera.CameraInfo.CAMERA_FACING_BACK)
                {
                    Log.d("MODEL NAME"," "+Build.DEVICE.toString()+" "+Build.MODEL.toString());
                    if (Build.MODEL.contentEquals("Nexus 5X")){
                        // rotate camera 180°
                    //    mCamera.setDisplayOrientation(180);
                    }
                }
                mCamera.setDisplayOrientation(90);
                mCamera.startPreview();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
