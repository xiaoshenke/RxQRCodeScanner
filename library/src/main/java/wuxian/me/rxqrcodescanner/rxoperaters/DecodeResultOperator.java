package wuxian.me.rxqrcodescanner.rxoperaters;

import rx.Observable;
import rx.Subscriber;
import wuxian.me.rxqrcodescanner.decode.DecodeResult;

/**
 * Created by wuxian on 10/11/2016.
 * <p>
 * if we get correct decoderesult ,we pass it downstream
 * else we request another @RxCamera.oneshot
 */

public class DecodeResultOperator implements Observable.Operator<String, DecodeResult> {
    @Override
    public Subscriber<DecodeResult> call(Subscriber<? super String> child) {
        DecodeResultSubscriber parent = new DecodeResultSubscriber(child);
        child.add(parent);
        return parent;
    }

    private class DecodeResultSubscriber extends Subscriber<DecodeResult> {
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
        public void onNext(DecodeResult result) {
            if (result.result.failed()) {  // if we fail,we request another oneshot
                result.rxCamera.setRequestAnotherShot(true); //can'f find a better solution
            } else {
                if (child.isUnsubscribed()) {
                    //onCompleted(); ?????
                    return;
                }
                try {
                    child.onNext(result.result.get());
                } catch (Exception e) {
                    onError(e);
                }

            }

        }
    }

}
