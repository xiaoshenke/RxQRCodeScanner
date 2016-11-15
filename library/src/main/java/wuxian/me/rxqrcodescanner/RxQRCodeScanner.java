package wuxian.me.rxqrcodescanner;

import android.content.Context;
import android.view.SurfaceView;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import wuxian.me.rxqrcodescanner.camera.PreviewData;
import wuxian.me.rxqrcodescanner.camera.RxCamera;
import wuxian.me.rxqrcodescanner.rxoperaters.DecodeResultOperator;
import wuxian.me.rxqrcodescanner.rxoperaters.PreviewFilter;
import wuxian.me.rxqrcodescanner.rxoperaters.Shot;
import wuxian.me.rxqrcodescanner.rxoperaters.PreviewToQRCode;
import wuxian.me.rxqrcodescanner.view.IScanView;


/**
 * Created by wuxian on 8/11/2016.
 */

public class RxQRCodeScanner {

    private SurfaceView surfaceView;
    private IScanView scanView;
    private Context context;
    private RxCamera camera;

    private RxQRCodeScanner(Context context, SurfaceView surfaceView, IScanView scanView) {
        this.context = context;
        this.surfaceView = surfaceView;
        this.scanView = scanView;
    }

    public Observable<String> start() {
        if (camera == null) {
            camera = new RxCamera.Builder()
                    .context(context)
                    .surfaceView(surfaceView)
                    .scanView(scanView)
                    .build();
        }

        return camera.open()                         //open camera
                .map(new Func1<RxCamera, RxCamera>() {
                    @Override
                    public RxCamera call(RxCamera rxCamera) {
                        return rxCamera.startScan(); //draw scanview
                    }
                })
                .lift(new Shot())                    //take  pictures
                .filter(new PreviewFilter())         //only pass one preview downstream
                .observeOn(Schedulers.computation())
                .map(new PreviewToQRCode(context))   //decode photo data
                .observeOn(AndroidSchedulers.mainThread())
                .lift(new DecodeResultOperator());   //if fail take another shot,otherwise push data downstream
    }

    public void quit() {
        if (camera != null) {
            camera.quit();
        }
    }

    public static class Builder {
        private SurfaceView surfaceView;
        private Context context;
        private IScanView scanView;

        public Builder() {
            ;
        }

        public Builder surfaceView(SurfaceView surfaceView) {
            this.surfaceView = surfaceView;
            return this;
        }

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder scanView(IScanView scanView) {
            this.scanView = scanView;
            return this;
        }

        public RxQRCodeScanner build() {
            return new RxQRCodeScanner(context, surfaceView, scanView);
        }
    }

}
