package com.owambo.jvamcas.drivemehome.ui.camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ViewSwitcher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.appbar.MaterialToolbar;
import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.owambo.jvamcas.drivemehome.R;
import com.owambo.jvamcas.drivemehome.databinding.ActivityCameraCaptureBinding;
import com.owambo.jvamcas.drivemehome.utils.Const;
import com.owambo.jvamcas.drivemehome.utils.ParseUtil;

import java.io.File;

/***
 * Take a picture and save it at path IMAGES/PROFILE_PICTURE/_<viewId>_<todaydate>_.jpg
 * Caller must make sure to pass viewId in the intent
 * Caller will be sent back image name i.e _<viewId>_<todaydate>_.jpg
 */
public class CameraCaptureActivity extends AppCompatActivity {
    private ActivityCameraCaptureBinding captureBinding;
    private ViewSwitcher mSwitcher;
    private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        captureBinding = DataBindingUtil.
                setContentView(this, R.layout.activity_camera_capture);
        MaterialToolbar mToolBar = captureBinding.cameraToolbar;
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));

        mCameraView = captureBinding.cameraSurface;
        mCameraView.setLifecycleOwner(this);
        mSwitcher = captureBinding.mSwitcher;

        if (mSwitcher.getCurrentView().getId() != R.id.camera_surface)
            mSwitcher.showNext();
        showPreview();

        captureBinding.captureFrame.setOnClickListener(view -> {
            mCameraView.takePictureSnapshot();
        });

        captureBinding.discardFrame.setOnClickListener(view -> {
            if (mSwitcher.getCurrentView().getId() != R.id.camera_surface)
                mSwitcher.showNext();
            showPreview();
        });
        final PictureResult[] mResult = new PictureResult[1];

        captureBinding.saveFrame.setOnClickListener(view -> {
            String viewId = getIntent().getStringExtra(Const.ICON_PATH);//need to pass in viewId when request frame
            String filePath = ParseUtil.iconPath(viewId);

            File mFile = new File(getExternalFilesDir(null), filePath);

            Intent mIntent = new Intent().putExtra(Const.ICON_PATH, mFile.getAbsolutePath());
            setResult(Activity.RESULT_OK, mIntent);

            //deleteCurrentViewIconOnDevice(viewId);

            mResult[0].toFile(mFile, file -> finish());//save image to file
        });
        mCameraView.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken( PictureResult result) {
                super.onPictureTaken(result);
                mSwitcher.showNext();
                showPictureSaveLayout();
                mResult[0] = result;
                result.toBitmap(mCameraView.getWidth(), mCameraView.getHeight(), bitmap ->
                        captureBinding.captureFramePreview.setImageBitmap(bitmap));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void showPreview() {
        captureBinding.captureFrame.setVisibility(View.VISIBLE);
        captureBinding.discardFrame.setVisibility(View.GONE);
        captureBinding.saveFrame.setVisibility(View.GONE);
        getSupportActionBar().setTitle(R.string.take_picture);
    }

    private void showPictureSaveLayout() {
        captureBinding.captureFrame.setVisibility(View.GONE);
        captureBinding.discardFrame.setVisibility(View.VISIBLE);
        captureBinding.saveFrame.setVisibility(View.VISIBLE);
        getSupportActionBar().setTitle(R.string.save_picture);
    }

    private void deleteCurrentViewIconOnDevice(String viewId) {
        File mDir = new File(getExternalFilesDir(null), Const.IMAGE_ROOT_PATH);
        if (mDir.isDirectory()) {
            for (File mFile : mDir.listFiles()) {
                if (mFile.isFile() && mFile.getName().replaceAll(".*_(\\w+)_.*", "$1").equals(viewId))
                    mFile.delete();
            }
        }
    }
}
