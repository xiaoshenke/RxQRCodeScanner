package wuxian.me.rxqrcodescanner;

import android.content.Context;
import android.util.Log;

import rx.functions.Func1;

/**
 * Created by wuxian on 10/11/2016.
 * TODO: to be finished
 */

public class PreviewToStringFunc implements Func1<PreviewData, String> {
    private static final String TAG = "NewPreviewFunc";
    private Context context;

    public PreviewToStringFunc(Context context) {
        this.context = context;
    }

    @Override
    public String call(PreviewData data) {
        Log.e(TAG, "in call data is " + data);
        return "";

        /**
         try {
         return DecodeManager.getQrcodeFromPreviewData(context, data);
         } catch (DecodeException e) {
         return null;
         }
         **/

    }
}


