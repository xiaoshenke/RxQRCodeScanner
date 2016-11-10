package wuxian.me.rxqrcodescanner;

import rx.Observable;
import rx.Subscriber;
import wuxian.me.rxqrcodescanner.camera.RxCamera;

/**
 * Created by wuxian on 10/11/2016.
 */

public class OneShotOperator implements Observable.Operator<PreviewData, RxCamera> {

    @Override
    public Subscriber<? super RxCamera> call(Subscriber<? super PreviewData> subscriber) {
        OneShotSubscriber parent = new OneShotSubscriber();
        subscriber.add(parent);
        return parent;
    }

    private class OneShotSubscriber extends Subscriber<RxCamera> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(RxCamera rxCamera) {

        }
    }
}
