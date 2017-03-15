package com.commonsware.cwac.cam2.playground.customfragment.camera_extension;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.commonsware.cwac.cam2.CameraController;
import com.commonsware.cwac.cam2.CameraEngine;
import com.commonsware.cwac.cam2.CameraFragment;
import com.commonsware.cwac.cam2.CameraView;
import com.commonsware.cwac.cam2.ErrorConstants;
import com.commonsware.cwac.cam2.ZoomStyle;
import com.commonsware.cwac.cam2.playground.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;

/**
 * Created by sergiosilvajr on 14/03/17.
 */

public class CustomCameraFragment extends CameraFragment {
    private Button shotButton;
    private Button switchButton;
    private View progress;
    private ViewGroup previewStack;
    private boolean mirrorPreview=false;

    private static final String ARG_OUTPUT="output";
    private static final String ARG_UPDATE_MEDIA_STORE=
            "updateMediaStore";
    private static final String ARG_SKIP_ORIENTATION_NORMALIZATION
            ="skipOrientationNormalization";
    private static final String ARG_IS_VIDEO="isVideo";
    private static final String ARG_QUALITY="quality";
    private static final String ARG_ZOOM_STYLE="zoomStyle";
    private static final String ARG_FACING_EXACT_MATCH=
            "facingExactMatch";
    private static final String ARG_TIMER_DURATION="timerDuration";

    public static CameraFragment newPictureInstance(Uri output,
                                                    boolean updateMediaStore,
                                                    int quality,
                                                    ZoomStyle zoomStyle,
                                                    boolean facingExactMatch,
                                                    boolean skipOrientationNormalization,
                                                    int timerDuration) {
        CameraFragment f=new CustomCameraFragment();
        Bundle args=new Bundle();

        args.putParcelable(ARG_OUTPUT, output);
        args.putBoolean(ARG_UPDATE_MEDIA_STORE, updateMediaStore);
        args.putBoolean(ARG_SKIP_ORIENTATION_NORMALIZATION,
                skipOrientationNormalization);
        args.putInt(ARG_QUALITY, quality);
        args.putBoolean(ARG_IS_VIDEO, false);
        args.putSerializable(ARG_ZOOM_STYLE, zoomStyle);
        args.putBoolean(ARG_FACING_EXACT_MATCH, facingExactMatch);
        args.putInt(ARG_TIMER_DURATION, timerDuration);
        f.setArguments(args);

        return (f);
    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View v=
                inflater.inflate(R.layout.fragment_custom_camera, container, false);
        previewStack = (ViewGroup) v.findViewById(R.id.cwac_cam2_preview_stack);
        progress=v.findViewById(R.id.progress_indicator);
        shotButton=
                (Button)v.findViewById(R.id.shot_button);
        switchButton=
                (Button)v.findViewById(R.id.switch_button);

        shotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCameraAction();
            }
        });
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setVisibility(View.VISIBLE);
                switchButton.setEnabled(false);

                try {
                    getController().switchCamera();
                }
                catch (Exception e) {
                    getController().postError(ErrorConstants.ERROR_SWITCHING_CAMERAS, e);
                    Log.e(getClass().getSimpleName(),
                            "Exception switching camera", e);
                }
            }
        });

        shotButton.setEnabled(false);
        switchButton.setEnabled(false);
        if ( getController()!=null &&  getController().getNumberOfCameras()>0) {
            prepController();
        }

        return (v);
    }

    private void prepController() {
        LinkedList<CameraView> cameraViews=new LinkedList<>();
        CameraView cv=(CameraView)previewStack.getChildAt(0);

        cv.setMirror(mirrorPreview);
        cameraViews.add(cv);

        for (int i=1; i<getController().getNumberOfCameras(); i++) {
            cv=new CameraView(getActivity());
            cv.setVisibility(View.INVISIBLE);
            cv.setMirror(mirrorPreview);
            previewStack.addView(cv);
            cameraViews.add(cv);
        }

        getController().setCameraViews(cameraViews);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (progress!= null) {
            progress.setVisibility(View.VISIBLE);
        }
    }

    private boolean canSwitchSources() {
        return (!getArguments().getBoolean(ARG_FACING_EXACT_MATCH,
                false));
    }
    @Override
    @SuppressWarnings("unused")
    @Subscribe(threadMode= ThreadMode.MAIN)
    public void onEventMainThread(CameraEngine.OpenedEvent event) {
        super.onEventMainThread(event);
        if (event.exception==null) {
            progress.setVisibility(View.GONE);
            switchButton.setEnabled(canSwitchSources());
            shotButton.setEnabled(true);
        }
    }

    @Override
    @SuppressWarnings("unused")
    @Subscribe(threadMode=ThreadMode.MAIN)
    public void onEventMainThread(
            CameraController.ControllerReadyEvent event) {
        if (event.isEventForController(getController())) {
            prepController();
        }
    }

}
