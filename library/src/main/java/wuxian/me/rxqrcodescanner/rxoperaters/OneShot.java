package wuxian.me.rxqrcodescanner.rxoperaters;

import android.hardware.Camera;
import android.os.Handler;

import rx.Observable;
import rx.Subscriber;
import wuxian.me.rxqrcodescanner.camera.PreviewData;
import wuxian.me.rxqrcodescanner.camera.RxCamera;

/**
 * Created by wuxian on 10/11/2016.
 * <p>
 * OneShot of RxCamera
 */

public class OneShot implements Observable.Operator<PreviewData, RxCamera> {

    @Override
    public Subscriber<? super RxCamera> call(Subscriber<? super PreviewData> subscriber) {
        OneShotSubscriber parent = new OneShotSubscriber(subscriber);
        subscriber.add(parent);
        return parent;
    }

    private class OneShotSubscriber extends Subscriber<RxCamera> implements Camera.PreviewCallback, Runnable {
        private RxCamera camera;
        Subscriber<? super PreviewData> child;
        private boolean oneshot = true;
        Handler handler = new Handler();
        private static final int DELAY_TIME = 1000;

        OneShotSubscriber(Subscriber<? super PreviewData> child) {
            super(child);
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
            run(); //can't say a good way ,....
        }

        @Override
        public synchronized void onPreviewFrame(byte[] bytes, Camera camera) {
            if (oneshot) {
                oneshot = false;
                camera.setPreviewCallback(null); //just one shot!

                if (!child.isUnsubscribed()) {
                    child.onNext(new PreviewData(this.camera, null, bytes));

                    handler.postDelayed(this, DELAY_TIME);
                }
            }
        }

        private void executeInternal() {
            oneshot = true;
            camera.setPreviewCallback(this);
        }

        @Override
        public void run() {
            if (child.isUnsubscribed()) {
                camera.quit(); //never forget this!
                return;
            }

            if (camera.isRequestAnotherShot()) {  //keep check this
                camera.setRequestAnotherShot(false);
                executeInternal();
            } else {
                handler.postDelayed(this, DELAY_TIME);
            }

        }
    }
}
