package wuxian.me.rxqrcodescanner.rxoperaters;

import android.content.Context;
import android.util.Log;

import rx.functions.Func1;
import wuxian.me.rxqrcodescanner.camera.PreviewData;
import wuxian.me.rxqrcodescanner.decode.DecodeFormatManager;
import wuxian.me.rxqrcodescanner.decode.DecodeResult;
import wuxian.me.rxqrcodescanner.util.Result;

/**
 * Created by wuxian on 10/11/2016.
 */

public class PreviewToQRCode implements Func1<PreviewData, DecodeResult> {
    private static final String TAG = "NewPreviewFunc";
    private Context context;

    public PreviewToQRCode(Context context) {
        this.context = context;
    }

    @Override
    public DecodeResult call(PreviewData data) {
        Log.e(TAG, "in call data is " + data);
        //DecodeFormatManager.getQrcodeFromPreviewData(context, data);
        return new DecodeResult(data.rxCamera, Result.<String>failure());  //test
        //return DecodeManager.getQrcodeFromPreviewData(context, data);
    }
}


