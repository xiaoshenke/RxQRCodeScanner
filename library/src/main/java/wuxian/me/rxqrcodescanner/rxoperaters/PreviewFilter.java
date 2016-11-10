package wuxian.me.rxqrcodescanner.rxoperaters;

import rx.functions.Func1;
import wuxian.me.rxqrcodescanner.camera.PreviewData;

/**
 * Created by wuxian on 10/11/2016.
 */

public class PreviewFilter implements Func1<PreviewData, Boolean> {
    @Override
    public synchronized Boolean call(PreviewData previewData) {
        if (previewData.rxCamera.isRequestAnotherShot()) {
            previewData.rxCamera.setRequestAnotherShot(false);

            previewData.data = null; //clear the data
            return true;
        }
        return false;
    }
}
