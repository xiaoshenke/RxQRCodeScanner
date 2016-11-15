package wuxian.me.rxqrcodescannerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Toast;

import rx.functions.Action1;
import wuxian.me.rxqrcodescanner.RxQRCodeScanner;
import wuxian.me.rxqrcodescanner.view.IScanView;

public class MainActivity extends AppCompatActivity {

    private IScanView scanView;
    private SurfaceView mSurfaceView;
    private RxQRCodeScanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
        scanView = (IScanView) findViewById(R.id.scanview);
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
        scanner.start().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Toast.makeText(MainActivity.this, "qrcode is " + s, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (scanner != null) {
            scanner.quit();
            scanner = null;
        }
    }
}
