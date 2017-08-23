package com.example.shubham.mycamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.shubham.mycamera.CameraFragment.CAMERA_ORIENTATION;
import static com.example.shubham.mycamera.CameraFragment.EXTRA_CAMERA_DATA;

/**
 * Preview Fragment For Showing the preview of Clicked picture from camera
 */
public class ActivationPreviewFragment extends Fragment implements View.OnClickListener {

    //Date Format
    private static final String DATE_FORMAT = "dd_mm_yyyy_hh_mm";
    private static final int IMAGE_QUALITY = 100;
    File mediaFile;
    private Bitmap mBitmap;
    private byte[] mData;

    public ActivationPreviewFragment() {
        // Required empty public constructor
    }

    /**
     * Used to return the camera File output.
     *
     * @return :file
     */
    private File getOutputMediaFile() {
        Log.d("CustomCameraFragment", " getOutputMediaFile");
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), BuildConfig.APPLICATION_ID);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("Custom Camera", "Required media storage does not exist");
                return null;
            }
        }

        // Create a media file name
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat(DATE_FORMAT).format(new Date());

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public static Bitmap makeSquare(byte[] data, int cameraID) {
        int width;
        int height;
        Matrix matrix = new Matrix();

        // Convert ByteArray to Bitmap
        Bitmap bitPic = BitmapFactory.decodeByteArray(data, 0, data.length);
        width = bitPic.getWidth();
        height = bitPic.getHeight();

        // Perform matrix rotations/mirrors depending on camera that took the photo
        if (cameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
            Matrix matrixMirrorY = new Matrix();
            matrixMirrorY.setValues(mirrorY);

            matrix.postConcat(matrixMirrorY);
        }

        matrix.postRotate(90);

        // Create new Bitmap out of the old one
        Bitmap bitPicFinal = Bitmap.createBitmap(bitPic, 0, 0, width, height, matrix, true);
        bitPic.recycle();
        int desWidth;
        int desHeight;
        desWidth = bitPicFinal.getWidth();
        desHeight = desWidth;
        Bitmap croppedBitmap = Bitmap.createBitmap(bitPicFinal, 0,bitPicFinal.getHeight() / 2 - bitPicFinal.getWidth() / 2,desWidth, desHeight);
        croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, 528, 528, true);
        return croppedBitmap;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activation_preview, container, false);
        ImageView previewImageView = (ImageView) view.findViewById(R.id.preview_image_view);
        TextView retakeTextView = (TextView) view.findViewById(R.id.retake_text_view);
        TextView saveTextView = (TextView) view.findViewById(R.id.save_text_view);

        //setting click listeners
        saveTextView.setOnClickListener(this);
        retakeTextView.setOnClickListener(this);

        //getting image from byte mData
        Bundle bundle = getArguments();
        if (!bundle.isEmpty()) {
            byte[] data = bundle.getByteArray(EXTRA_CAMERA_DATA);
            mData = data;
            int orientation = bundle.getInt(CAMERA_ORIENTATION);
            mBitmap = makeSquare(data, orientation);
            previewImageView.setImageBitmap(mBitmap);
        } else {
            Toast.makeText(MyApplication.getAppContext(), R.string.error_in_getting_image, Toast.LENGTH_SHORT).show();
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.retake_text_view) {
            Toast.makeText(MyApplication.getAppContext(), "Retake Image", Toast.LENGTH_SHORT).show();
            getActivity().getSupportFragmentManager().
                    beginTransaction()
                    .replace(R.id.fragment_container, new CameraFragment())
                    .commit();
        } else if (view.getId() == R.id.save_text_view) {
            Toast.makeText(MyApplication.getAppContext(), "Save Image", Toast.LENGTH_SHORT).show();

//            final File saveFile = getOutputMediaFile();
//            if (saveFile != null) {
//                FileOutputStream fos = null;
//                try {
//                    fos = new FileOutputStream(saveFile);
//                    // mImageBitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, fos);
//                    fos.write(mData);
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            } else {
//                Toast.makeText(MyApplication.getAppContext(), "storage permission denied", Toast.LENGTH_SHORT).show();
//                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//            }

               ImageSaving imageSaving=new ImageSaving(mBitmap);
                imageSaving.execute();

        }
    }

    private class ImageSaving extends AsyncTask<Void, Void, Boolean> {
        private Bitmap mImageBitmap;

        ImageSaving(Bitmap mBitmap) {
            this.mImageBitmap = mBitmap;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
//            mData = params[0];
            boolean result = false;
            final File saveFile = getOutputMediaFile();
            if (saveFile != null) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(saveFile);
                   result = mImageBitmap.compress(Bitmap.CompressFormat.PNG, IMAGE_QUALITY, fos);
                //fos.write(mData);
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(MyApplication.getAppContext(), "storage permission denied", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }

            return result;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {

            if (aBoolean)
            {
                Toast.makeText(MyApplication.getAppContext(), R.string.save_image_to + " " + mediaFile.getPath(), Toast.LENGTH_SHORT).show();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new CameraFragment())
                        .commit();
            }
            else
            {
                Toast.makeText(MyApplication.getAppContext(), "storage permission denied", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

            }
        }
    }
}
