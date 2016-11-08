package wuxian.me.rxqrcodescanner;

import android.content.Context;
import android.view.SurfaceView;

import rx.Observable;


/**
 * Created by wuxian on 8/11/2016.
 */

public class RxQRCodeScanner {
    private QRCodeScannerDFA dfa;

    private SurfaceView surfaceView;
    private Context context;

    private RxQRCodeScanner(Context context, SurfaceView surfaceView) {
        this.context = context;
        this.surfaceView = surfaceView;
        dfa = buildDFA();
    }

    private QRCodeScannerDFA buildDFA() {
        return null;
    }

    /**
     * RxCamera.surfaceview().(每隔多少时间???)takeoneshot().observeon(Computation).map(decodefunction)
     * .map(是否成功 成功则返回 失败再次takeoneshot)
     *
     * @return
     */
    public Observable<String> start() {
        return null;
    }

    public static class Builder {
        private SurfaceView surfaceView;
        private Context context;

        public Builder() {
            ;
        }

        public void surfaceView(SurfaceView surfaceView) {
            this.surfaceView = surfaceView;
        }

        public void context(Context context) {
            this.context = context;
        }

        public RxQRCodeScanner build() {
            return new RxQRCodeScanner(context, surfaceView);
        }
    }

}
