package wuxian.me.rxqrcodescanner;

import android.content.Context;
import android.view.SurfaceView;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import wuxian.me.rxqrcodescanner.camera.RxCamera;
import wuxian.me.rxqrcodescanner.rxoperaters.DecodeResultOperator;
import wuxian.me.rxqrcodescanner.rxoperaters.OneShot;
import wuxian.me.rxqrcodescanner.rxoperaters.PreviewToQRCode;


/**
 * Created by wuxian on 8/11/2016.
 */

public class RxQRCodeScanner {

    private SurfaceView surfaceView;
    private Context context;
    private RxCamera camera;

    private RxQRCodeScanner(Context context, SurfaceView surfaceView) {
        this.context = context;
        this.surfaceView = surfaceView;
    }

    public Observable<String> start() {
        if (camera == null) {
            camera = new RxCamera.Builder().context(context).surfaceView(surfaceView).build();
        }

        return camera.open()                         //open camera
                .lift(new OneShot())                 //take a picture
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

        public RxQRCodeScanner build() {
            return new RxQRCodeScanner(context, surfaceView);
        }
    }

}
