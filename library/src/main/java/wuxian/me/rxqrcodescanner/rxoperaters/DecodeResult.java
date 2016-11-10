package wuxian.me.rxqrcodescanner.rxoperaters;

import rx.Observable;
import rx.Subscriber;
import wuxian.me.rxqrcodescanner.util.Result;

/**
 * Created by wuxian on 10/11/2016.
 * <p>
 * if we get correct decoderesult ,we pass it downstream
 * else we request another @RxCamera.oneshot
 */

public class DecodeResult implements Observable.Operator<String, Result<String>> {
    @Override
    public Subscriber<Result<String>> call(Subscriber<? super String> child) {
        DecodeResultSubscriber parent = new DecodeResultSubscriber(child);
        child.add(parent);
        return parent;
    }

    private class DecodeResultSubscriber extends Subscriber<Result<String>> {
        private Subscriber<? super String> child;

        public DecodeResultSubscriber(Subscriber<? super String> child) {
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
        public void onNext(Result<String> result) {
            if (result.failed()) {  // if we fail,we request another oneshot
                request(1); //FIXME: how to request a data from the origin Observable?
            } else {
                if (child.isUnsubscribed()) {
                    return;
                }
                try {
                    child.onNext(result.get());
                } catch (Exception e) {
                    onError(e);
                }

            }

        }
    }
}
