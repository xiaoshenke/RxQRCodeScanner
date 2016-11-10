package wuxian.me.rxqrcodescanner.camera;

import android.graphics.Point;

/**
 * Created by wuxian on 20/10/2016.
 */

public class PreviewData {
    public RxCamera rxCamera;
    public Point resolution;
    public byte[] data;

    public PreviewData(RxCamera rxCamera, Point resolution, byte[] data) {
        this.rxCamera = rxCamera;
        this.resolution = resolution;
        this.data = data;
    }
}
