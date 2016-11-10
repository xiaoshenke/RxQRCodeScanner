package wuxian.me.rxqrcodescanner.camera;

import android.content.Context;
import android.view.SurfaceView;

import rx.Observable;
import rx.Subscriber;
import wuxian.me.rxqrcodescanner.PreviewData;
import wuxian.me.rxqrcodescanner.RxCameraProducer;

/**
 * Created by wuxian on 8/11/2016.
 */

public class RxCamera {

    private RxCamera(Context context, SurfaceView surfaceView, CameraConfig config) {
        ;
    }

    public Observable<RxCamera> open() {
        return Observable.create(new Observable.OnSubscribe<RxCamera>() {
            @Override
            public void call(Subscriber<? super RxCamera> subscriber) {

                subscriber.setProducer(new RxCameraProducer(subscriber, RxCamera.this));
            }
        });
    }


    public static class Builder {
        private Context context;
        private SurfaceView surfaceView;
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

        public Builder cameraConfig(CameraConfig config) {
            this.config = config;
            return this;
        }

        public RxCamera build() {
            return new RxCamera(context, surfaceView, config);
        }
    }

}
