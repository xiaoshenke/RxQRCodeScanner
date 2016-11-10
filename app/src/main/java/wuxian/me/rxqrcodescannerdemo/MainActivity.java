package wuxian.me.rxqrcodescannerdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.Toast;

import rx.functions.Action1;
import wuxian.me.rxqrcodescanner.RxQRCodeScanner;

public class MainActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface);
    }

    @Override
    protected void onResume() {
        super.onResume();

        new RxQRCodeScanner.Builder().context(this).surfaceView(mSurfaceView).build()
                .start()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(MainActivity.this, "qrcode is " + s, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
