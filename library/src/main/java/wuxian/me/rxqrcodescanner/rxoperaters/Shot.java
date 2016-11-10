package wuxian.me.rxqrcodescanner.rxoperaters;

import android.hardware.Camera;

import rx.Observable;
import rx.Subscriber;
import wuxian.me.rxqrcodescanner.camera.PreviewData;
import wuxian.me.rxqrcodescanner.camera.RxCamera;

/**
 * Created by wuxian on 10/11/2016.
 * <p>
 * Shot of RxCamera
 */

public class Shot implements Observable.Operator<PreviewData, RxCamera> {

    @Override
    public Subscriber<? super RxCamera> call(Subscriber<? super PreviewData> subscriber) {
        OneShotSubscriber parent = new OneShotSubscriber(subscriber);
        subscriber.add(parent);
        return parent;
    }

    private class OneShotSubscriber extends Subscriber<RxCamera> implements Camera.PreviewCallback {
        private RxCamera camera;
        Subscriber<? super PreviewData> child;
        private boolean oneshot = true;

        OneShotSubscriber(Subscriber<? super PreviewData> child) {
            this.child = child;
        }

        @Override
        public void onCompleted() {
            child.onCompleted();
        }

        @Override
        public void onError(Throwable e) {
            child.onError(e);
        }

        @Override
        public void onNext(RxCamera rxCamera) {
            this.camera = rxCamera;
            oneshot = true;
            camera.setPreviewCallback(this);

        }

        @Override
        public synchronized void onPreviewFrame(byte[] bytes, Camera camera) {
            if (oneshot) {
                oneshot = false;
                camera.setPreviewCallback(null); //just one shot!
                child.onNext(new PreviewData(this.camera, null, bytes));
            }
        }
    }
}
