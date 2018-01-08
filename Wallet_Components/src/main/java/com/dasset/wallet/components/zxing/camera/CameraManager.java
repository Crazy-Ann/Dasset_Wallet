package com.dasset.wallet.components.zxing.camera;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.os.Handler;
import android.view.SurfaceHolder;

import com.dasset.wallet.components.utils.LogUtil;
import com.dasset.wallet.components.utils.ViewUtil;

import java.io.IOException;

public final class CameraManager {

    private static CameraManager cameraManager;

    static final int SDK_INT; // Later we can use Build.VERSION.SDK_INT

    static {
        int sdkInt;
        try {
            sdkInt = Integer.parseInt(Build.VERSION.SDK);
        } catch (NumberFormatException nfe) {
            // Just to be safe
            sdkInt = 10000;
        }
        SDK_INT = sdkInt;
    }

    private final  Context                    context;
    private final  CameraConfigurationManager configManager;
    private static Camera                     camera;
    private        Rect                       framingRect;
    private        Rect                       framingRectInPreview;
    private        boolean                    initialized;
    private        boolean                    previewing;
    private final  boolean                    useOneShotPreviewCallback;
    /**
     * Preview frames are delivered here, which we pass on to the registered
     * handler. Make sure to clear the handler so it will only receive one
     * message.
     */
    private final  PreviewCallback            previewCallback;
    /**
     * Autofocus callbacks arrive here, and are dispatched to the Handler which
     * requested them.
     */
    private final  AutoFocusCallback          autoFocusCallback;

    /**
     * Initializes this static object with the Context of the calling Activity.
     *
     * @param context
     *         The Activity which wants to use the camera.
     */
    public static void initialize(Context context) {
        if (cameraManager == null) {
            cameraManager = new CameraManager(context);
        }
    }

    public Camera getCamera() {
        return camera;
    }

    /**
     * Gets the CameraManager singleton instance.
     *
     * @return A reference to the CameraManager singleton.
     */
    public static CameraManager get() {
        return cameraManager;
    }

    private CameraManager(Context context) {
        this.context = context;
        this.configManager = new CameraConfigurationManager(context);
        useOneShotPreviewCallback = Integer.parseInt(Build.VERSION.SDK) > Build.VERSION_CODES.CUPCAKE;
        previewCallback = new PreviewCallback(configManager, useOneShotPreviewCallback);
        autoFocusCallback = new AutoFocusCallback();
    }

    /**
     * Opens the camera driver and initializes the hardware parameters.
     *
     * @param holder
     *         The surface object which the camera will draw preview frames
     *         into.
     *
     * @throws IOException
     *         Indicates the camera driver failed to open.
     */
    public void openDriver(SurfaceHolder holder) throws IOException {
        if (camera == null) {
            camera = Camera.open();
            if (camera == null) {
                throw new IOException();
            }
            camera.setPreviewDisplay(holder);
            if (!initialized) {
                initialized = true;
                configManager.initFromCameraParameters(camera);
            }
            configManager.setDesiredCameraParameters(camera);
        } else {
        }
    }

    public static void flash() {
        //camera.startPreview();
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (Camera.Parameters.FLASH_MODE_OFF.equals(parameters.getFlashMode())) {
                turnOn(parameters);
            } else if (Camera.Parameters.FLASH_MODE_TORCH.equals(parameters.getFlashMode())) {
                turnOff(parameters);
            }
        }
    }

    private static void turnOn(Camera.Parameters parameters) {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(parameters);
    }

    private static void turnOff(Camera.Parameters parameters) {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(parameters);
    }

    /**
     * Closes the camera driver if still in use.
     */
    public void closeDriver() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }

    /**
     * Asks the camera hardware to begin drawing preview frames to the screen.
     */
    public void startPreview() {
        if (camera != null && !previewing) {
            camera.startPreview();
            previewing = true;
        }
    }

    /**
     * Tells the camera to stop drawing preview frames.
     */
    public void stopPreview() {
        if (camera != null && previewing) {
            if (!useOneShotPreviewCallback) {
                camera.setPreviewCallback(null);
            }
            camera.stopPreview();
            previewCallback.setHandler(null, 0);
            autoFocusCallback.setHandler(null, 0);
            previewing = false;
        }
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data
     * will arrive as byte[] in the message.obj field, with width and height
     * encoded as message.arg1 and message.arg2, respectively.
     *
     * @param handler
     *         The handler to send the message to.
     * @param message
     *         The what field of the message to be sent.
     */
    public void requestPreviewFrame(Handler handler, int message) {
        if (camera != null && previewing) {
            previewCallback.setHandler(handler, message);
            if (useOneShotPreviewCallback) {
                camera.setOneShotPreviewCallback(previewCallback);
            } else {
                camera.setPreviewCallback(previewCallback);
            }
        }
    }

    /**
     * Asks the camera hardware to perform an autofocus.
     *
     * @param handler
     *         The Handler to notify when the autofocus completes.
     * @param message
     *         The message to deliver.
     */
    public void requestAutoFocus(Handler handler, int message) {
        if (camera != null && previewing) {
            autoFocusCallback.setHandler(handler, message);
            try {
                camera.autoFocus(autoFocusCallback);
            } catch (Exception e) {
                LogUtil.getInstance().e("camera", "autoFocus failed");
            }
        }
    }

    /**
     * Calculates the framing rect which the UI should draw to show the user
     * where to place the barcode. This target helps with alignment as well as
     * forces the user to hold the device far enough away to ensure the image
     * will be in focus.
     *
     * @return The rectangle to draw on screen in window coordinates.
     */
    public Rect getFramingRect() {
        Point screenResolution = configManager.getScreenResolution();
        if (framingRect == null) {
            if (camera == null) {
                return null;
            }
            int width  = screenResolution.x * 5 / 8;
            int height = screenResolution.x * 5 / 8;
            LogUtil.getInstance().print("scan_frame width:" + width);
            LogUtil.getInstance().print("scan_frame height:" + height);
            int leftOffset = (screenResolution.x - width) / 2;
            int topOffset  = (screenResolution.y - height) / 5 * 2 - ViewUtil.getInstance().px2dp(context,100);
            framingRect = new Rect(leftOffset, topOffset, leftOffset + width,
                                   topOffset + height);
            LogUtil.getInstance().print("scan_frame x:" + screenResolution.x);
            LogUtil.getInstance().print("scan_frame y:" + screenResolution.y);
            LogUtil.getInstance().print("scan_frame leftOffset:" + leftOffset);
            LogUtil.getInstance().print("scan_frame topOffset:" + topOffset);
            LogUtil.getInstance().print("scan_frame rightOffset:" + leftOffset + width);
            LogUtil.getInstance().print("scan_frame bottomOffset:" + topOffset + height);
        }
        return framingRect;
    }

    /**
     * Like {@link #getFramingRect} but coordinates are in terms of the preview
     * frame, not UI / screen.
     */
    private Rect getFramingRectInPreview() {
        if (framingRectInPreview == null) {
            Rect  rect             = new Rect(getFramingRect());
            Point cameraResolution = configManager.getCameraResolution();
            Point screenResolution = configManager.getScreenResolution();
            rect.left = rect.left * cameraResolution.y / screenResolution.x;
            rect.right = rect.right * cameraResolution.y / screenResolution.x;
            rect.top = rect.top * cameraResolution.x / screenResolution.y;
            rect.bottom = rect.bottom * cameraResolution.x / screenResolution.y;
            LogUtil.getInstance().print("rect.left:" + rect.left);
            LogUtil.getInstance().print("rect.right:" + rect.right);
            LogUtil.getInstance().print("rect.top:" + rect.top);
            LogUtil.getInstance().print("rect.bottom:" + rect.bottom);
            framingRectInPreview = rect;
        }
        return framingRectInPreview;
    }


    /**
     * A factory method to build the appropriate LuminanceSource object based on
     * the format of the preview buffers, as described by Camera.Parameters.
     *
     * @param data
     *         A preview frame.
     * @param width
     *         The width of the image.
     * @param height
     *         The height of the image.
     *
     * @return A PlanarYUVLuminanceSource instance.
     */
    public PlanarYUVLuminanceSource buildLuminanceSource(byte[] data, int width, int height) {
        Rect   rect                = getFramingRectInPreview();
        int    previewFormat       = configManager.getPreviewFormat();
        String previewFormatString = configManager.getPreviewFormatString();
        switch (previewFormat) {
            case PixelFormat.YCbCr_420_SP:
            case PixelFormat.YCbCr_422_SP:
                return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height());
            default:
                if ("yuv420p".equals(previewFormatString)) {
                    return new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height());
                }
        }
        throw new IllegalArgumentException("Unsupported picture format: " + previewFormat + '/' + previewFormatString);
    }

    public Context getContext() {
        return context;
    }
}
