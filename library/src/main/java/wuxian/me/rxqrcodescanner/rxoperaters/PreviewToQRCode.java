package wuxian.me.rxqrcodescanner.rxoperaters;

import android.content.Context;
import android.util.Log;

import rx.functions.Func1;
import wuxian.me.rxqrcodescanner.camera.PreviewData;
import wuxian.me.rxqrcodescanner.util.Result;

/**
 * Created by wuxian on 10/11/2016.
 */

public class PreviewToQRCode implements Func1<PreviewData, Result<String>> {
    private static final String TAG = "NewPreviewFunc";
    private Context context;

    public PreviewToQRCode(Context context) {
        this.context = context;
    }

    @Override
    public Result call(PreviewData data) {
        Log.e(TAG, "in call data is " + data);
        return Result.failure();  //test

        //return DecodeManager.getQrcodeFromPreviewData(context, data);

    }
}


