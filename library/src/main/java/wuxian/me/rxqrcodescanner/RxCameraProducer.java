package wuxian.me.rxqrcodescanner;

import rx.Producer;
import rx.Subscriber;
import wuxian.me.rxqrcodescanner.camera.RxCamera;

/**
 * Created by wuxian on 10/11/2016.
 */

public class RxCameraProducer implements Producer {
    private RxCamera camera;
    private Subscriber<? super RxCamera> subscriber;

    public RxCameraProducer(Subscriber<? super RxCamera> subscriber, RxCamera camera) {
        this.camera = camera;
        this.subscriber = subscriber;
    }

    @Override
    public void request(long n) {

        subscriber.onNext(camera);

        //we never call onComplete for now...
    }
}
