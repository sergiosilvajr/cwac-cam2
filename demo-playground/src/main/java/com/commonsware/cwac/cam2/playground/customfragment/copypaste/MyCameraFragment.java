package com.commonsware.cwac.cam2.playground.customfragment.copypaste;

import android.app.ActionBar;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.commonsware.cwac.cam2.AbstractCameraActivity;
import com.commonsware.cwac.cam2.CameraController;
import com.commonsware.cwac.cam2.CameraEngine;
import com.commonsware.cwac.cam2.CameraView;
import com.commonsware.cwac.cam2.ErrorConstants;
import com.commonsware.cwac.cam2.PictureTransaction;
import com.commonsware.cwac.cam2.ZoomStyle;
import com.commonsware.cwac.cam2.playground.R;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;

/**
 * Created by sergiosilvajr on 14/03/17.
 */

public class MyCameraFragment extends Fragment {

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
    private CameraController ctlr;
    private ViewGroup previewStack;
    private View progress;
    private boolean mirrorPreview=false;
    private Button shotButton;
    private Button switchButton;

    public static MyCameraFragment newPictureInstance(Uri output,
                                                    boolean updateMediaStore,
                                                    int quality,
                                                    ZoomStyle zoomStyle,
                                                    boolean facingExactMatch,
                                                    boolean skipOrientationNormalization,
                                                    int timerDuration) {
        MyCameraFragment f=new MyCameraFragment();
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

    /**
     * Standard fragment entry point.
     *
     * @param savedInstanceState State of a previous instance
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    /**
     * Standard lifecycle method, passed along to the CameraController.
     */
    @Override
    public void onStart() {
        super.onStart();

        AbstractCameraActivity.BUS.register(this);

        if (ctlr!=null) {
            ctlr.start();
        }
    }

    @Override
    public void onHiddenChanged(boolean isHidden) {
        super.onHiddenChanged(isHidden);

        if (!isHidden) {
            ActionBar ab=getActivity().getActionBar();

            if (ab!=null) {
                ab.setBackgroundDrawable(getActivity()
                        .getResources()
                        .getDrawable(
                                com.commonsware.cwac.cam2.R.drawable.cwac_cam2_action_bar_bg_transparent));
                ab.setTitle("");

                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                    ab.setDisplayHomeAsUpEnabled(false);
                }
                else {
                    ab.setDisplayShowHomeEnabled(false);
                    ab.setHomeButtonEnabled(false);
                }
            }

            if (shotButton!=null) {
                shotButton.setEnabled(true);
                switchButton.setEnabled(canSwitchSources());
            }
        }
    }

    /**
     * Standard lifecycle method, for when the fragment moves into
     * the stopped state. Passed along to the CameraController.
     */
    @Override
    public void onStop() {
        if (ctlr!=null) {
            try {
                ctlr.stop();
            }
            catch (Exception e) {
                ctlr.postError(ErrorConstants.ERROR_STOPPING, e);
                Log.e(getClass().getSimpleName(),
                        "Exception stopping controller", e);
            }
        }

        AbstractCameraActivity.BUS.unregister(this);

        super.onStop();
    }

    /**
     * Standard lifecycle method, for when the fragment is utterly,
     * ruthlessly destroyed. Passed along to the CameraController,
     * because why should the fragment have all the fun?
     */
    @Override
    public void onDestroy() {
        if (ctlr!=null) {
            ctlr.destroy();
        }

        super.onDestroy();
    }

    /**
     * Standard callback method to create the UI managed by
     * this fragment.
     *
     * @param inflater Used to inflate layouts
     * @param container Parent of the fragment's UI (eventually)
     * @param savedInstanceState State of a previous instance
     * @return the UI being managed by this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View v=
                inflater.inflate(R.layout.fragment_custom_camera, container, false);

        previewStack=
                (ViewGroup)v.findViewById(R.id.cwac_cam2_preview_stack);

        progress=v.findViewById(R.id.cwac_cam2_progress);
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
                    ctlr.switchCamera();
                }
                catch (Exception e) {
                    ctlr.postError(ErrorConstants.ERROR_SWITCHING_CAMERAS, e);
                    Log.e(getClass().getSimpleName(),
                            "Exception switching camera", e);
                }
            }
        });

        onHiddenChanged(false); // hack, since this does not get
        // called on initial display

        shotButton.setEnabled(false);
        switchButton.setEnabled(false);

        if (ctlr!=null && ctlr.getNumberOfCameras()>0) {
            prepController();
        }

        return (v);
    }

    public void shutdown() {
        progress.setVisibility(View.VISIBLE);

        if (ctlr!=null) {
            try {
                ctlr.stop();
            }
            catch (Exception e) {
                ctlr.postError(ErrorConstants.ERROR_STOPPING, e);
                Log.e(getClass().getSimpleName(),
                        "Exception stopping controller", e);
            }
        }
    }

    /**
     * @return the CameraController this fragment delegates to
     */

    /**
     * Establishes the controller that this fragment delegates to
     *
     * @param ctlr the controller that this fragment delegates to
     */
    public void setController(CameraController ctlr) {
        int currentCamera=-1;

        if (this.ctlr!=null) {
            currentCamera=this.ctlr.getCurrentCamera();
        }

        this.ctlr=ctlr;
        ctlr.setQuality(getArguments().getInt(ARG_QUALITY, 1));

        if (currentCamera>-1) {
            ctlr.setCurrentCamera(currentCamera);
        }
    }

    /**
     * Indicates if we should mirror the preview or not. Defaults
     * to false.
     *
     * @param mirror true if we should horizontally mirror the
     *               preview, false otherwise
     */
    public void setMirrorPreview(boolean mirror) {
        this.mirrorPreview=mirror;
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode= ThreadMode.MAIN)
    public void onEventMainThread(
            CameraController.ControllerReadyEvent event) {
        if (event.isEventForController(ctlr)) {
            prepController();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode=ThreadMode.MAIN)
    public void onEventMainThread(CameraEngine.OpenedEvent event) {
        if (event.exception==null) {
            progress.setVisibility(View.GONE);
            switchButton.setEnabled(canSwitchSources());
            shotButton.setEnabled(true);
            previewStack.setOnTouchListener(null);
        }
        else {
            ctlr.postError(ErrorConstants.ERROR_OPEN_CAMERA,
                    event.exception);
            getActivity().finish();
        }
    }

    protected void performCameraAction() {
        takePicture();
    }

    private void takePicture() {
        Uri output=getArguments().getParcelable(ARG_OUTPUT);

        PictureTransaction.Builder b=new PictureTransaction.Builder();

        if (output!=null) {
            b.toUri(getActivity(), output,
                    getArguments().getBoolean(ARG_UPDATE_MEDIA_STORE, false),
                    getArguments().getBoolean(ARG_SKIP_ORIENTATION_NORMALIZATION,
                            false));
        }

        shotButton.setEnabled(false);
        switchButton.setEnabled(false);
        ctlr.takePicture(b.build());
    }


    private boolean canSwitchSources() {
        return (!getArguments().getBoolean(ARG_FACING_EXACT_MATCH,
                false));
    }

    private void prepController() {
        LinkedList<CameraView> cameraViews= new LinkedList<>();
        CameraView cv=(CameraView)previewStack.getChildAt(0);

        cv.setMirror(mirrorPreview);
        cameraViews.add(cv);

        for (int i=1; i<ctlr.getNumberOfCameras(); i++) {
            cv=new CameraView(getActivity());
            cv.setVisibility(View.INVISIBLE);
            cv.setMirror(mirrorPreview);
            previewStack.addView(cv);
            cameraViews.add(cv);
        }

        ctlr.setCameraViews(cameraViews);
    }

}
