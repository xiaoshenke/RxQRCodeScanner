package wuxian.me.rxqrcodescanner.decode;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextUtils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.ReaderException;
import com.google.zxing.common.HybridBinarizer;

import java.util.Hashtable;
import java.util.Vector;

import wuxian.me.rxqrcodescanner.camera.PreviewData;
import wuxian.me.rxqrcodescanner.view.ViewfinderResultPointCallback;

/**
 * Created by wuxian on 24/10/2016.
 * <p>
 */

public final class DecodeManager {
    private static MultiFormatReader reader;

    private DecodeManager() {
    }

    public static String getQrcodeFromPreviewData(Context context, PreviewData input) throws DecodeException {
        if (reader == null) {
            init();
        }

        byte[] data = input.data;
        int height = input.resolution.y;
        int width = input.resolution.x;

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
        /*CameraConfigurationManager configManager = QRCodeCamera.getInstance(context).getConfigManager();
        if (configManager == null) {
            return null;
        }

        Point screenResolution = configManager.getScreenResolution();
        Rect framingRect;
        int width = screenResolution.x;
        int height = screenResolution.y;

        int leftOffset = (screenResolution.x - width) / 2;
        int topOffset = (screenResolution.y - height) / 3;
        framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
                topOffset + height);
        return framingRect;
        */
        return null;
    }

    private static Rect getFramingRectInPreview(Context context) {
        /*
        Rect frameRect = getFramingRect(context);
        if (frameRect == null) {
            return null;
        }

        CameraConfigurationManager configManager = QRCodeCamera.getInstance(context).getConfigManager();

        Rect rect = new Rect(frameRect);
        Point cameraResolution = configManager.getCameraResolution();
        Point screenResolution = configManager.getScreenResolution();
        rect.left = rect.left * cameraResolution.y / screenResolution.x;
        rect.right = rect.right * cameraResolution.y / screenResolution.x;
        rect.top = rect.top * cameraResolution.x / screenResolution.y;
        rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;

        return rect;
        */
        return null;
    }

    private static PlanarYUVLuminanceSource getSourceFromPreviewData(Context context, PreviewData previewData) {
        /*
        CameraConfigurationManager configManager = QRCodeCamera.getInstance(context).getConfigManager();
        if (configManager == null) {
            return null;
        }

        byte[] data = previewData.data;
        int width = previewData.resolution.x;
        int height = previewData.resolution.y;

        Rect rect = getFramingRectInPreview(context);
        int previewFormat = configManager.getPreviewFormat();
        String previewFormatString = configManager.getPreviewFormatString();
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
        */
        return null;

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
