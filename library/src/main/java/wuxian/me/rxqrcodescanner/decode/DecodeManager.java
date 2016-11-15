package wuxian.me.rxqrcodescanner.decode;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.regex.Pattern;

import wuxian.me.rxqrcodescanner.camera.CameraConfig;
import wuxian.me.rxqrcodescanner.camera.CameraUtil;
import wuxian.me.rxqrcodescanner.camera.PreviewData;
import wuxian.me.rxqrcodescanner.view.ViewfinderResultPointCallback;

import static wuxian.me.rxqrcodescanner.camera.CameraUtil.getScreenResolution;

/**
 * Created by wuxian on 24/10/2016.
 * <p>
 */

public final class DecodeManager {
    private static MultiFormatReader reader;
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    private DecodeManager() {
        throw new NoSuchElementException("no instance");
    }

    public static String getQrcodeFromPreviewData(Context context, PreviewData input) throws DecodeException {
        Camera camera = input.rxCamera.getNativeCamera();
        if (camera == null) {
            throw new DecodeException("can't get camera parameters");
        }
        Point cameraResolution = CameraUtil.getCameraResolution(context, camera);
        if (reader == null) {
            init();
        }

        byte[] data = input.data;
        int height = cameraResolution.y;  //resolution --> cameraResulotion
        int width = cameraResolution.x;

        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;
        data = rotatedData;

        com.google.zxing.Result rawResult = null;
        PlanarYUVLuminanceSource source = getSourceFromPreviewData(context, new PreviewData(input.rxCamera, new Point(width, height), data));

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        try {
            rawResult = reader.decodeWithState(bitmap);
        } catch (ReaderException re) {
            throw new DecodeException("exception in reader");
        } finally {
            reader.reset();
        }

        if (rawResult != null) {
            final String code = rawResult.getText().trim();

            if (!TextUtils.isEmpty(code)) {
                return code;
            }
        }
        throw new DecodeException("no result");
    }

    private static Rect getFramingRect(Context context) {
        Point screenResolution = getScreenResolution(context);
        Rect framingRect;
        int width = screenResolution.x;
        int height = screenResolution.y;

        int leftOffset = (screenResolution.x - width) / 2;
        int topOffset = (screenResolution.y - height) / 3;
        framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
                topOffset + height);
        return framingRect;
    }

    private static Rect getFramingRectInPreview(Context context, Camera camera) {

        Rect frameRect = getFramingRect(context);
        if (frameRect == null) {
            return null;
        }
        Rect rect = new Rect(frameRect);
        Point cameraResolution = CameraUtil.getCameraResolution(context, camera);
        Point screenResolution = getScreenResolution(context);

        rect.left = rect.left * cameraResolution.y / screenResolution.x;
        rect.right = rect.right * cameraResolution.y / screenResolution.x;
        rect.top = rect.top * cameraResolution.x / screenResolution.y;
        rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;

        return rect;
    }

    private static PlanarYUVLuminanceSource getSourceFromPreviewData(Context context, PreviewData previewData) {
        CameraConfig cameraConfig = previewData.rxCamera.getCameraConfig();

        byte[] data = previewData.data;
        int width = previewData.resolution.x;
        int height = previewData.resolution.y;

        Camera camera = previewData.rxCamera.getNativeCamera();
        if (camera == null) {
            return null;
        }
        Rect rect = getFramingRectInPreview(context, camera);
        int previewFormat = cameraConfig.previewFormat;

        String previewFormatString = camera.getParameters().get("preview-format");
        ;
        switch (previewFormat) {
            // This is the standard Android format which all devices are REQUIRED to
            // support.
            // In theory, it's the only one we should ever care about.
            case PixelFormat.YCbCr_420_SP:
                // This format has never been seen in the wild, but is compatible as
                // we only care
                // about the Y channel, so allow it.
            case PixelFormat.YCbCr_422_SP:
                return new PlanarYUVLuminanceSource(data, width, height, rect.left,
                        rect.top, rect.width(), rect.height());
            default:
                // The Samsung Moment incorrectly uses this variant instead of the
                // 'sp' version.
                // Fortunately, it too has all the Y data up front, so we can read
                // it.
                if ("yuv420p".equals(previewFormatString)) {
                    return new PlanarYUVLuminanceSource(data, width, height,
                            rect.left, rect.top, rect.width(), rect.height());
                }
        }
        throw new IllegalArgumentException("Unsupported picture format: "
                + previewFormat + '/' + previewFormatString);
    }

    private static void init() {
        reader = new MultiFormatReader();
        reader.setHints(getDefaultHints());
    }

    static Hashtable<DecodeHintType, Object> getDefaultHints() {
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        Vector<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
        decodeFormats.addAll(DecodeFormatManager.ONE_D_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS);
        decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

        String characterSet = null;
        if (characterSet != null) {
            hints.put(DecodeHintType.CHARACTER_SET, characterSet);
        }

        hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK,
                new ViewfinderResultPointCallback(null));  //Todo: replace last parameter

        return hints;
    }
}
