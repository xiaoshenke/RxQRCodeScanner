package wuxian.me.rxqrcodescannerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Toast;

import rx.Subscriber;
import wuxian.me.rxqrcodescanner.RxQRCodeScanner;
import wuxian.me.rxqrcodescanner.view.IScanView;

public class MainActivity extends AppCompatActivity {

    private IScanView scanView;
    private SurfaceView mSurfaceView;
    private Subscriber<String> subscriber;
    private RxQRCodeScanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        scanView = (IScanView) findViewById(R.id.scanview);

        if (subscriber == null) {
            subscriber = new Subscriber<String>() {
                @Override
                public void onCompleted() {
                    //won't be called in these library...
                }

                @Override
                public void onError(Throwable e) {
                    Toast.makeText(MainActivity.this, "error message: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                @Override
                public void onNext(String s) {
                    Toast.makeText(MainActivity.this, "qrcode is " + s, Toast.LENGTH_LONG).show();
                }
            };
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanner == null) {
            scanner = new RxQRCodeScanner.Builder()
                    .context(this)
                    .surfaceView(mSurfaceView)
                    .scanView(scanView)
                    .build();
        }
        scanner.start().subscribe(subscriber);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (subscriber != null) {
            subscriber.unsubscribe();
        }

        if (scanner != null) {
            scanner.quit();
        }
    }
}
