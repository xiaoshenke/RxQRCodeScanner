package wuxian.me.rxqrcodescanner.rxoperaters;

import android.util.Log;

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
    private static final String TAG = "DecodeResult";
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
            if (result.result.failed()) {
                result.rxCamera.setRequestAnotherShot(true);
            } else {
                if (child.isUnsubscribed()) {
                    return;
                }
                try {
                    result.rxCamera.stopPreview();
                    result.rxCamera.autoFocus(false);
                    result.rxCamera.stopScan();
                    child.onNext((String) result.result.get());
                } catch (Exception e) {
                    onError(e);
                }
            }
        }
    }

}
