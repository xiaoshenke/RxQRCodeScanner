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

    private static long shotTime = -1;

    @Override
    public Subscriber<? super RxCamera> call(Subscriber<? super PreviewData> subscriber) {
        OneShotSubscriber parent = new OneShotSubscriber(subscriber);
        subscriber.add(parent);
        return parent;
    }

    private class OneShotSubscriber extends Subscriber<RxCamera> implements Camera.PreviewCallback {
        private static final String TAG = "OneShotSubscriber";
        private RxCamera camera;
        Subscriber<? super PreviewData> child;
        private static final long SHOT_INTERVAL = 800; //时间间隔太频繁的话 decode所需的内存吃不消

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
            camera.setPreviewCallback(this);
        }

        @Override
        public synchronized void onPreviewFrame(byte[] bytes, Camera camera) {
            if (!child.isUnsubscribed() && bytes != null) {
                boolean shot = false;
                if (shotTime == -1) {
                    shotTime = System.currentTimeMillis();
                    shot = true;
                } else {
                    if (System.currentTimeMillis() - shotTime > SHOT_INTERVAL) {
                        shotTime = System.currentTimeMillis();
                        shot = true;
                    }
                }
                if (shot) {
                    child.onNext(new PreviewData(this.camera, null, bytes));
                }

            }
        }
    }
}
