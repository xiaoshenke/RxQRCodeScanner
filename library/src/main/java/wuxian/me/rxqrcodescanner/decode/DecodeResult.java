package wuxian.me.rxqrcodescanner.decode;

import wuxian.me.rxqrcodescanner.camera.RxCamera;
import wuxian.me.rxqrcodescanner.util.Result;

/**
 * Created by wuxian on 10/11/2016.
 */

public class DecodeResult {
    public RxCamera rxCamera;
    public Result<String> result;

    public DecodeResult(RxCamera rxCamera, Result<String> result) {

        this.rxCamera = rxCamera;
        this.result = result;
    }
}
