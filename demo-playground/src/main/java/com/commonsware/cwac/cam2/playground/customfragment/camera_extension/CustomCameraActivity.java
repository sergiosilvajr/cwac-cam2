package com.commonsware.cwac.cam2.playground.customfragment.camera_extension;


import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;

import com.commonsware.cwac.cam2.CameraEngine;
import com.commonsware.cwac.cam2.CameraFragment;
import com.commonsware.cwac.cam2.FlashMode;
import com.commonsware.cwac.cam2.ZoomStyle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.commonsware.cwac.cam2.CameraActivity.EXTRA_CONFIRM;
import static com.commonsware.cwac.cam2.CameraActivity.EXTRA_CONFIRMATION_QUALITY;
import static com.commonsware.cwac.cam2.CameraActivity.EXTRA_DEBUG_SAVE_PREVIEW_FRAME;
import static com.commonsware.cwac.cam2.CameraActivity.EXTRA_SKIP_ORIENTATION_NORMALIZATION;
import static com.commonsware.cwac.cam2.CameraActivity.EXTRA_TIMER;

/**
 * Created by sergiosilvajr on 14/03/17.
 */

public class CustomCameraActivity extends CustomAbstractCameraActivity {
    @Override
    protected int getCameraIdLayer() {
        return android.R.id.content;
    }

    @Override
    protected boolean needsOverlay() {
        return(true);
    }

    @Override
    protected boolean needsActionBar() {
        return(true);
    }

    @Override
    protected boolean isVideo() {
        return false;
    }

    @Override
    protected CameraFragment buildFragment() {
        return(CustomCameraFragment.newPictureInstance(getOutputUri(),
                getIntent().getBooleanExtra(EXTRA_UPDATE_MEDIA_STORE, false),
                getIntent().getIntExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1),
                (ZoomStyle)getIntent().getSerializableExtra(EXTRA_ZOOM_STYLE),
                getIntent().getBooleanExtra(EXTRA_FACING_EXACT_MATCH, false),
                getIntent().getBooleanExtra(EXTRA_SKIP_ORIENTATION_NORMALIZATION, false),
                getIntent().getIntExtra(EXTRA_TIMER, 0)));
    }

    @Override
    protected String[] getNeededPermissions() {
        return new String[0];
    }

    @Override
    protected void configEngine(CameraEngine engine) {
        if (getIntent()
                .getBooleanExtra(EXTRA_DEBUG_SAVE_PREVIEW_FRAME, false)) {
            engine
                    .setDebugSavePreviewFile(new File(getExternalCacheDir(),
                            "cam2-preview.jpg"));
        }

        List<FlashMode> flashModes=
                (List<FlashMode>)getIntent().getSerializableExtra(EXTRA_FLASH_MODES);

        if (flashModes==null) {
            flashModes= new ArrayList<>();
        }

        if (flashModes!=null) {
            engine.setPreferredFlashModes(flashModes);
        }
    }

    public static class IntentBuilder
            extends CustomAbstractCameraActivity.IntentBuilder<IntentBuilder> {
        /**
         * Standard constructor. May throw a runtime exception
         * if the environment is not set up properly (see
         * validateEnvironment() on Utils).
         *
         * @param ctxt any Context will do
         */
        public IntentBuilder(Context ctxt) {
            super(ctxt, CustomCameraActivity.class);
        }

        @Override
        public Intent buildChooserBaseIntent() {
            return(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
        }

        /**
         * Call to skip the confirmation screen, so once the user
         * takes the picture, you get control back right away.
         *
         * @return the builder, for further configuration
         */
        public CustomCameraActivity.IntentBuilder skipConfirm() {
            result.putExtra(EXTRA_CONFIRM, false);

            return(this);
        }

        /**
         * Call to skip examining the picture for the EXIF orientation
         * tag and rotating the image if needed.
         *
         * @return the builder, for further configuration
         */
        public CustomCameraActivity.IntentBuilder skipOrientationNormalization() {
            result.putExtra(EXTRA_SKIP_ORIENTATION_NORMALIZATION, true);

            return(this);
        }

        public CustomCameraActivity.IntentBuilder debugSavePreviewFrame() {
            result.putExtra(EXTRA_DEBUG_SAVE_PREVIEW_FRAME, true);

            return(this);
        }

        /**
         * Call to set the quality factor for the confirmation screen.
         * Value should be greater than 0.0f and below 1.0f, and
         * represents the fraction of the app's heap size that we
         * should be willing to use for loading the confirmation
         * image. Defaults to not being used.
         *
         * @param quality something in (0.0f, 1.0f] range
         * @return the builder, for further configuration
         */
        public CustomCameraActivity.IntentBuilder confirmationQuality(float quality) {
            if (quality<=0.0f || quality>1.0f) {
                throw new IllegalArgumentException("Quality outside (0.0f, 1.0f] range!");
            }

            result.putExtra(EXTRA_CONFIRMATION_QUALITY, quality);

            return(this);
        }

        /**
         * Call to set a countdown timer for taking the picture. The picture will
         * be taken after the specified number of seconds automatically, unless
         * the activity is destroyed already (e.g., user already took a picture).
         *
         * @param duration time to wait before taking picture, in seconds
         * @return the builder, for further configuration
         */
        public CustomCameraActivity.IntentBuilder timer(int duration) {
            if (duration<=0) {
                throw new IllegalArgumentException("Timer duration must be positive");
            }

            result.putExtra(EXTRA_TIMER, duration);

            return(this);
        }
    }
}
