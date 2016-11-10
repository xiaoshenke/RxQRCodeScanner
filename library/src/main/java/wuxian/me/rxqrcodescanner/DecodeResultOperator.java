package wuxian.me.rxqrcodescanner;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by wuxian on 10/11/2016.
 * <p>
 * if we get correct decoderesult ,we pass it downstream
 * else we request another @RxCamera.oneshot
 */

public class DecodeResultOperator implements Observable.Operator<String, String> {
    @Override
    public Subscriber<? super String> call(Subscriber<? super String> subscriber) {
        return null;
    }

    private class DecodeResultSubscriber extends Subscriber<String> {
        private Subscriber<? super String> child;

        public DecodeResultSubscriber(Subscriber<? super String> child) {
            this.child = child;
        }

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(String s) {
            //string is valid
            child.onNext(s);
            //string is not valid
            request(1);
        }
    }
}
