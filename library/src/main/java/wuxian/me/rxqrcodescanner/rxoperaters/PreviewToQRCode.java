package wuxian.me.rxqrcodescanner.rxoperaters;

import android.content.Context;
import android.util.Log;

import rx.functions.Func1;
import wuxian.me.rxqrcodescanner.camera.PreviewData;
import wuxian.me.rxqrcodescanner.decode.DecodeException;
import wuxian.me.rxqrcodescanner.decode.DecodeManager;
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
        try {
            String code = DecodeManager.getQrcodeFromPreviewData(context, data);
            return new DecodeResult(data.rxCamera, Result.success((Object) code));
        } catch (DecodeException e) {
            return new DecodeResult(data.rxCamera, Result.failure());
        }

    }
}


