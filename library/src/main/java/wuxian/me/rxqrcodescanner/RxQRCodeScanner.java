package wuxian.me.rxqrcodescanner;

import android.content.Context;
import android.view.SurfaceView;

import rx.Observable;
import rx.schedulers.Schedulers;
import wuxian.me.rxqrcodescanner.camera.RxCamera;


/**
 * Created by wuxian on 8/11/2016.
 */

public class RxQRCodeScanner {
    private QRCodeScannerDFA dfa;

    private SurfaceView surfaceView;
    private Context context;
    private RxCamera camera;

    private RxQRCodeScanner(Context context, SurfaceView surfaceView) {
        this.context = context;
        this.surfaceView = surfaceView;
        dfa = buildDFA();
    }

    private QRCodeScannerDFA buildDFA() {
        return null;
    }

    /**
     * RxCamera.surfaceview().start().(每隔多少时间???)takeoneshot().observeon(Computation).map(decodefunction)
     * .map(是否成功 成功则返回 失败再次takeoneshot)
     * 像rxlifecycle那样有一个state的概念?
     *
     * @return
     */
    public Observable<String> start() {
        if (camera == null) {
            camera = new RxCamera.Builder().context(context).surfaceView(surfaceView).build();
        }
        return camera.oneshot()
                .observeOn(Schedulers.computation())
                .map(new PreviewToStringFunc(context));//TODO: success return fail request one shot..
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
