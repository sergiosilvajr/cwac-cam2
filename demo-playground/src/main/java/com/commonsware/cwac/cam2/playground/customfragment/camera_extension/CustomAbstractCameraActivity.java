package com.commonsware.cwac.cam2.playground.customfragment.camera_extension;

import android.os.ResultReceiver;

import com.commonsware.cwac.cam2.AbstractCameraActivity;
import com.commonsware.cwac.cam2.CameraController;
import com.commonsware.cwac.cam2.CameraEngine;
import com.commonsware.cwac.cam2.CameraSelectionCriteria;
import com.commonsware.cwac.cam2.Facing;
import com.commonsware.cwac.cam2.FocusMode;

/**
 * Created by sergiosilvajr on 14/03/17.
 */

public abstract class CustomAbstractCameraActivity extends AbstractCameraActivity {
    @Override
    protected void init() {
        cameraFrag= (CustomCameraFragment) getFragmentManager().findFragmentByTag(TAG_CAMERA);

        boolean fragNeedsToBeAdded=false;

        if (cameraFrag==null) {
            cameraFrag=buildFragment();
            fragNeedsToBeAdded=true;
        }

        FocusMode focusMode=
                (FocusMode)getIntent().getSerializableExtra(EXTRA_FOCUS_MODE);
        boolean allowChangeFlashMode=
                getIntent().getBooleanExtra(EXTRA_ALLOW_SWITCH_FLASH_MODE, false);
        ResultReceiver onError=
                getIntent().getParcelableExtra(EXTRA_UNHANDLED_ERROR_RECEIVER);

        CameraController ctrl=
                new CameraController(focusMode, onError,
                        allowChangeFlashMode, isVideo());

        cameraFrag.setController(ctrl);
        cameraFrag
                .setMirrorPreview(getIntent()
                        .getBooleanExtra(EXTRA_MIRROR_PREVIEW, false));

        Facing facing=
                (Facing)getIntent().getSerializableExtra(EXTRA_FACING);

        if (facing==null) {
            facing= Facing.BACK;
        }

        boolean match=getIntent()
                .getBooleanExtra(EXTRA_FACING_EXACT_MATCH, false);
        CameraSelectionCriteria criteria=
                new CameraSelectionCriteria.Builder()
                        .facing(facing)
                        .facingExactMatch(match)
                        .build();
        CameraEngine.ID forcedEngineId=
                (CameraEngine.ID)getIntent().getSerializableExtra(EXTRA_FORCE_ENGINE);

        ctrl.setEngine(CameraEngine.buildInstance(this, CameraEngine.ID.CAMERA2), criteria);
        ctrl.getEngine().setDebug(getIntent().getBooleanExtra(EXTRA_DEBUG_ENABLED, false));
        configEngine(ctrl.getEngine());

        if (fragNeedsToBeAdded) {
            getFragmentManager()
                    .beginTransaction()
                    .add(getCameraIdLayer(), cameraFrag, TAG_CAMERA)
                    .commit();
        }
    }

    abstract protected int getCameraIdLayer();


}
