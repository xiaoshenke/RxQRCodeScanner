package wuxian.me.rxqrcodescanner.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import wuxian.me.rxqrcodescanner.RxQRCodeScanner;
import wuxian.me.rxqrcodescanner.util.Preconditions;
import wuxian.me.rxqrcodescanner.view.IScanView;

/**
 * Created by wuxian on 8/11/2016.
 * RxCamera actual contains an Android.hardware.camera and provide some basic function.
 */

public class RxCamera {
    private Context context;

    private Camera camera;
    private CameraConfig cameraConfig;

    private IScanView scanView;
    private SurfaceView surfaceView;

    private boolean isPreviewing = false;
    private boolean cameraOpen = false;

    private boolean alreadyInit = false;

    private boolean requestAnotherShot = true;

    private RxCamera(@NonNull Context context, @NonNull SurfaceView surfaceView,
                     @NonNull CameraConfig config, @Nullable IScanView scanView) {
        this.context = context;
        this.surfaceView = surfaceView;
        this.cameraConfig = config;
        this.scanView = scanView;
    }

    public Camera getNativeCamera() {
        return camera;
    }

    public void setRequestAnotherShot(boolean request) {
        requestAnotherShot = request;
    }

    public boolean isRequestAnotherShot() {
        return requestAnotherShot;
    }

    private boolean openCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                cameraOpen = true;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public void stopPreview() {
        if (camera != null) {
            camera.stopPreview();
        }
    }

    public void setPreviewCallback(Camera.PreviewCallback callback) {
        if (camera != null) {
            camera.setPreviewCallback(callback);
        }
    }

    public RxCamera startScan() {
        if (scanView != null) {
            scanView.drawScanFrame();
        }
        return this;
    }

    public RxCamera stopScan() {
        if (scanView != null) {
            scanView.stopDrawScanFrame();
        }
        return this;
    }

    /**
     * return a rxcamera which is open and startpreview state
     */
    public Observable<RxCamera> open() {
        return Observable.create(new RxCameraOnSubscribe());
    }

    public synchronized void quit() {
        if (camera != null) {
            if (isPreviewing()) {
                camera.stopPreview();
            }

            camera.setPreviewCallback(null);
            try {
                camera.release();
            } catch (Exception e) {
                ;
            } finally {
                isPreviewing = false;
                alreadyInit = false;
                cameraOpen = false;
                camera = null;
            }

        }
    }

    private boolean isOpen() {
        return camera != null && cameraOpen;
    }

    private boolean isAlreadyInit() {
        return alreadyInit;
    }

    private boolean isPreviewing() {
        return isPreviewing;
    }

    private void setIsPreviewing(boolean isPreviewing) {
        this.isPreviewing = isPreviewing;
    }

    private SurfaceView getSurfaceView() {
        return surfaceView;
    }

    private void startPreview() {
        if (camera != null) {
            camera.startPreview();
        }
        setIsPreviewing(false);
    }

    private boolean setPreviewDisplay(SurfaceHolder holder) {
        if (camera != null) {
            try {
                camera.setPreviewDisplay(holder);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private boolean autoFocus = false;
    private Handler handler = new Handler();

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (autoFocus) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        autoFocus(autoFocus);
                    }
                }, 1000);
            }
        }
    };

    public void autoFocus(boolean auto) {
        autoFocus = auto;
        if (camera != null && autoFocus) {
            camera.autoFocus(autoFocusCallback);
        }
    }

    private boolean initCameraWithConfig() {
        Camera.Parameters parameters = null;
        try {
            parameters = camera.getParameters();
        } catch (Exception e) {
            return false;
        }

        // set fps
        if (cameraConfig.minPreferPreviewFrameRate != -1 && cameraConfig.maxPreferPreviewFrameRate != -1) {
            try {
                int[] range = CameraUtil.findClosestFpsRange(camera, cameraConfig.minPreferPreviewFrameRate, cameraConfig.maxPreferPreviewFrameRate);
                parameters.setPreviewFpsRange(range[0], range[1]);
            } catch (Exception e) {
                return false;
            }
        }

        // set preview size;
        if (cameraConfig.preferPreviewSize != null) {  //don't care you
            try {
                //check wether squared preview is accepted or not.
                if (cameraConfig.acceptSquarePreview) {
                    Point point = CameraUtil.getCameraResolution(context, camera);
                    parameters.setPreviewSize(point.x, point.y);
                    //Camera.Size previewSize = CameraUtil.findClosestPreviewSize(camera, cameraConfig.preferPreviewSize);
                    //parameters.setPreviewSize(previewSize.width, previewSize.height);
                } else {
                    Point point = CameraUtil.getCameraResolution(context, camera);
                    parameters.setPreviewSize(point.x, point.y);
                    //Camera.Size previewSize = CameraUtil.findClosestNonSquarePreviewSize(camera, cameraConfig.preferPreviewSize);
                    //parameters.setPreviewSize(previewSize.width, previewSize.height);
                }
            } catch (Exception e) {
                return false;
            }
        }

        // set format
        if (cameraConfig.previewFormat != -1) {
            try {
                parameters.setPreviewFormat(cameraConfig.previewFormat);
                parameters.setPictureFormat(ImageFormat.JPEG);
            } catch (Exception e) {
                return false;
            }
        }

        // set auto focus
        if (cameraConfig.isAutoFocus) {
            try {
                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                }
            } catch (Exception e) {
                return false;
            }
        }

        // set enableShutterSound (only supported for API 17 and newer)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && cameraConfig.muteShutterSound) {
            if (CameraUtil.canDisableShutter(cameraConfig.currentCameraId)) {
                camera.enableShutterSound(false);
            }
        }

        // set all parameters
        try {
            camera.setParameters(parameters);
        } catch (Exception e) {
            return false;
        }

        // set display orientation
        int displayOrientation = cameraConfig.displayOrientation;
        if (displayOrientation == -1) {
            displayOrientation = CameraUtil.getPortraitCameraDisplayOrientation(context, cameraConfig.currentCameraId, cameraConfig.isFaceCamera);
        }
        try {
            camera.setDisplayOrientation(displayOrientation);
        } catch (Exception e) {
            return false;
        }

        alreadyInit = true;
        return true;
    }

    public class RxCameraProducer implements Producer {
        private RxCamera rxcamera;
        private Subscriber<? super RxCamera> subscriber;

        public RxCameraProducer(Subscriber<? super RxCamera> subscriber, RxCamera camera) {
            this.rxcamera = camera;
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            executeInternal();
        }

        private void executeInternal() {
            if (subscriber.isUnsubscribed()) {
                rxcamera.quit(); //never forget this!
                return;
            }

            Preconditions.checkUiThread();
            if (!rxcamera.isOpen()) {
                boolean open = rxcamera.openCamera();
                if (!open) {
                    subscriber.onError(new Exception("open rxcamera error"));
                    return;
                }
            }

            if (!rxcamera.isAlreadyInit()) {
                boolean doinit = rxcamera.initCameraWithConfig();
                if (!doinit) {
                    subscriber.onError(new Exception("init camera with cameraConfig error"));
                    return;
                }
            }

            if (rxcamera.isPreviewing()) {
                subscriber.onNext(rxcamera);
            } else {
                SurfaceView sf = rxcamera.getSurfaceView();
                if (sf == null || sf.getHolder() == null) {
                    subscriber.onError(new Exception("surfaceview is invalid"));
                    return;
                }
                sf.getHolder().addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                        boolean setHolder = rxcamera.setPreviewDisplay(surfaceHolder);
                        if (!setHolder) {
                            subscriber.onError(new Exception("setpreviewdisplay error"));
                            return;
                        }

                        rxcamera.startPreview();
                        rxcamera.setIsPreviewing(true);
                        rxcamera.autoFocus(true);

                        if (subscriber.isUnsubscribed()) {
                            rxcamera.quit();
                            return;
                        }
                        subscriber.onNext(rxcamera);
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                        rxcamera.setIsPreviewing(false);
                    }
                });
            }
        }
    }

    public class RxCameraOnSubscribe implements Observable.OnSubscribe<RxCamera> {
        @Override
        public void call(Subscriber<? super RxCamera> subscriber) {
            subscriber.setProducer(new RxCameraProducer(subscriber, RxCamera.this));
        }
    }


    public static class Builder {
        private Context context;
        private SurfaceView surfaceView;
        private IScanView scanView;
        private CameraConfig config;

        public Builder() {
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder surfaceView(SurfaceView surfaceView) {
            this.surfaceView = surfaceView;
            return this;
        }

        public Builder scanView(IScanView scanView) {
            this.scanView = scanView;
            return this;
        }

        public Builder cameraConfig(CameraConfig config) {
            this.config = config;
            return this;
        }

        private CameraConfig buildDefaultConfig() {
            CameraConfig config = new CameraConfig.Builder()
                    .useBackCamera()
                    .setAutoFocus(true)
                    .setPreferPreviewFrameRate(15, 30)
                    .setPreferPreviewSize(new Point(640, 480), false)
                    .setHandleSurfaceEvent(true)
                    .build();
            return config;
        }

        public RxCamera build() {
            Preconditions.checkNotNull(context, "context is null");
            Preconditions.checkNotNull(surfaceView, "surfaceview is null");
            if (config == null) {
                config = buildDefaultConfig();
            }
            return new RxCamera(context, surfaceView, config, scanView);
        }
    }

}
