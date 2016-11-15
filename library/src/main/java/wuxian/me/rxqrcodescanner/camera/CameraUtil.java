package wuxian.me.rxqrcodescanner.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by ragnarok on 15/11/1.
 * some utilities for retrieve system camera config
 */
public class CameraUtil {

    private static final String TAG = "RxCamera.CameraUtil";

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    private static int frontCameraId = -1;
    private static int backCameraId = -1;
    private static int cameraNumber = -1;

    public static int getCameraNumber() {
        if (cameraNumber == -1) {
            cameraNumber = Camera.getNumberOfCameras();
        }
        return cameraNumber;
    }

    public static int getFrontCameraId() {
        if (frontCameraId == -1) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < getCameraNumber(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    frontCameraId = i;
                    break;
                }
            }
        }
        return frontCameraId;
    }

    public static Camera.CameraInfo getCameraInfo(int id) {
        if (id >= 0 && id < getCameraNumber()) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(id, cameraInfo);
            return cameraInfo;
        }
        return null;
    }

    public static boolean hasFrontCamera() {
        return getFrontCameraId() != -1;
    }

    public static boolean hasBackCamera() {
        return getBackCameraId() != -1;
    }

    public static int getBackCameraId() {
        if (backCameraId == -1) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < getCameraNumber(); i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    backCameraId = i;
                    break;
                }
            }
        }
        return backCameraId;
    }

    public static int getPortraitCameraDisplayOrientation(Context context, int cameraId, boolean isFrontCamera) {
        if (cameraId < 0 || cameraId > getCameraNumber()) {
            return -1;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int rotation = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (isFrontCamera) {
            result = (cameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (cameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public static int[] findClosestFpsRange(Camera camera, int minFrameRate, int maxFrameRate) {
        minFrameRate *= 1000;
        maxFrameRate *= 1000;
        Camera.Parameters parameters = camera.getParameters();
        int minIndex = 0;
        int minDiff = Integer.MAX_VALUE;
        List<int[]> rangeList = parameters.getSupportedPreviewFpsRange();
        Log.d(TAG, "support preview fps range list: " + dumpFpsRangeList(rangeList));
        for (int i = 0; i < rangeList.size(); i++) {
            int[] fpsRange = rangeList.get(i);
            if (fpsRange.length != 2) {
                continue;
            }
            int minFps = fpsRange[0] / 1000;
            int maxFps = fpsRange[1] / 1000;
            int diff = Math.abs(minFps - minFrameRate) + Math.abs(maxFps - maxFrameRate);
            if (diff < minDiff) {
                minDiff = diff;
                minIndex = i;
            }
        }
        int[] result = rangeList.get(minIndex);
        return result;
    }

    private static String dumpFpsRangeList(List<int[]> rangeList) {
        String result = "";
        for (int[] range : rangeList) {
            if (range.length != 2) {
                continue;
            }
            result += "(" + range[0] + "," + range[1] + ") ";
        }
        return result;
    }

    private static Point sScreenResolution;
    private static Point sCameraResolution;

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

    public static Rect getScanviewRectInPreview(Context context, Camera camera) {
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

    public static Point getScreenResolution(Context context) {
        if (sScreenResolution == null) {
            WindowManager manager = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            Display display = manager.getDefaultDisplay();
            DisplayMetrics dm = new DisplayMetrics();
            display.getMetrics(dm);
            sScreenResolution = new Point(display.getWidth(), display.getHeight());
        }
        return sScreenResolution;
    }

    private static Point findBestPreviewSizeValue(
            CharSequence previewSizeValueString, Point screenResolution) {
        int bestX = 0;
        int bestY = 0;
        int diff = Integer.MAX_VALUE;
        for (String previewSize : COMMA_PATTERN.split(previewSizeValueString)) {

            previewSize = previewSize.trim();
            int dimPosition = previewSize.indexOf('x');
            if (dimPosition < 0) {
                continue;
            }

            int newX;
            int newY;
            try {
                newX = Integer.parseInt(previewSize.substring(0, dimPosition));
                newY = Integer.parseInt(previewSize.substring(dimPosition + 1));
            } catch (NumberFormatException nfe) {
                //Log.w(TAG, "Bad preview-size: " + previewSize);
                continue;
            }

            int newDiff = Math.abs(newX - screenResolution.x)
                    + Math.abs(newY - screenResolution.y);
            if (newDiff == 0) {
                bestX = newX;
                bestY = newY;
                break;
            } else if (newDiff < diff) {
                bestX = newX;
                bestY = newY;
                diff = newDiff;
            }

        }

        if (bestX > 0 && bestY > 0) {
            return new Point(bestX, bestY);
        }
        return null;
    }

    public static Point getCameraResolution(Context context, Camera camera) {
        if (sCameraResolution == null && camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            Point screenResolution = getScreenResolution(context);
            String previewSizeValueString = parameters.get("preview-size-values");
            // saw this on Xperia
            if (previewSizeValueString == null) {
                previewSizeValueString = parameters.get("preview-size-value");
            }

            Point cameraResolution = null;

            if (previewSizeValueString != null) {
                cameraResolution = findBestPreviewSizeValue(previewSizeValueString,
                        screenResolution);
            }

            if (cameraResolution == null) {
                // Ensure that the camera resolution is a multiple of 8, as the
                // screen may not be.
                cameraResolution = new Point((screenResolution.x >> 3) << 3,
                        (screenResolution.y >> 3) << 3);
            }
            sCameraResolution = cameraResolution;
        }
        return sCameraResolution;
    }

    public static Camera.Size findClosestPreviewSize(Camera camera, Point preferSize) {
        int preferX = preferSize.x;
        int preferY = preferSize.y;
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> allSupportSizes = parameters.getSupportedPreviewSizes();
        Log.d(TAG, "all support preview size: " + dumpPreviewSizeList(allSupportSizes));
        int minDiff = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < allSupportSizes.size(); i++) {
            Camera.Size size = allSupportSizes.get(i);
            int x = size.width;
            int y = size.height;

            int diff = Math.abs(x - preferX) + Math.abs(y - preferY);
            if (diff < minDiff) {
                minDiff = diff;
                index = i;
            }
        }

        Camera.Size size = allSupportSizes.get(index);
        return size;
    }

    public static Camera.Size findClosestNonSquarePreviewSize(Camera camera, Point preferSize) {
        int preferX = preferSize.x;
        int preferY = preferSize.y;
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> allSupportSizes = parameters.getSupportedPreviewSizes();
        Log.d(TAG, "all support preview size: " + dumpPreviewSizeList(allSupportSizes));
        int minDiff = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < allSupportSizes.size(); i++) {
            Camera.Size size = allSupportSizes.get(i);
            int x = size.width;
            int y = size.height;
            if (x != y) {
                int diff = Math.abs(x - preferX) + Math.abs(y - preferY);
                if (diff < minDiff) {
                    minDiff = diff;
                    index = i;
                }
            }
        }

        Camera.Size size = allSupportSizes.get(index);
        return size;
    }

    private static String dumpPreviewSizeList(List<Camera.Size> sizes) {
        String result = "";
        for (Camera.Size size : sizes) {
            result += "(" + size.width + "," + size.height + ") ";
        }
        return result;
    }

    public static Rect transferCameraAreaFromOuterSize(Point center, Point outerSize, int size) {
        int left = clampAreaCoord((int) (center.x / (float) (outerSize.x) * 2000 - 1000), size);
        int top = clampAreaCoord((int) (center.y / (float) (outerSize.y) * 2000 - 1000), size);

        return new Rect(left, top, left + size, top + size);
    }

    private static int clampAreaCoord(int center, int focusAreaSize) {
        int result;
        if (Math.abs(center) + focusAreaSize / 2 > 1000) {
            if (center > 0) {
                result = 1000 - focusAreaSize / 2;
            } else {
                result = -1000 + focusAreaSize / 2;
            }
        } else {
            result = center - focusAreaSize / 2;
        }
        return result;
    }

    public static boolean canDisableShutter(int id) {
        // cameraInfo.canDisableShutterSound is only available for API 17 and newer
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Camera.CameraInfo cameraInfo = getCameraInfo(id);
            return cameraInfo != null && cameraInfo.canDisableShutterSound;
        } else {
            Log.d(TAG, "SDK does not support disabling shutter sound");
            return false;
        }
    }
}
