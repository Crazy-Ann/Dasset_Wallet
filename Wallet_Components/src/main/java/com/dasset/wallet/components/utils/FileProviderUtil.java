package com.dasset.wallet.components.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.dasset.wallet.components.constant.Constant;

import java.io.File;
import java.util.List;

public final class FileProviderUtil {

    private static FileProviderUtil fileProviderUtil;

    private FileProviderUtil() {
        // cannot be instantiated
    }

    public static synchronized FileProviderUtil getInstance() {
        if (fileProviderUtil == null) {
            fileProviderUtil = new FileProviderUtil();
        }
        return fileProviderUtil;
    }

    public static void releaseInstance() {
        if (fileProviderUtil != null) {
            fileProviderUtil = null;
        }
    }

    public Uri generateUri(Context context, Intent intent, File file) {
        Uri uri = generateUri(context, file);
        if (uri != null) {
            grantUriPermission(context, intent, uri);
            return uri;
        }
        return null;
    }

    public Uri generateUri(Context context, File file) {
        if (context != null && file != null) {
            if (file.getPath().startsWith("http") || file.getPath().startsWith("https")) {
                return Uri.parse(file.getPath());
            }
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, Constant.FILE_PROVIDER_AUTHORITY, file);
            } else {
                uri = Uri.fromFile(file);
            }
            return uri;
        }
        return null;
    }

    private void grantUriPermission(Context context, Intent intent, Uri uri) {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) {
            return;
        }
        if (context == null || intent == null || uri == null) {
            return;
        }
        if (uri.getScheme() != null && uri.getScheme().startsWith("http") || uri.getScheme().startsWith("https")) {
            return;
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;
            LogUtil.getInstance().print("resolveInfo:" + packageName);
            if (!TextUtils.isEmpty(packageName)) {
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
    }

    public static void revokeUriPermission(Context context, Intent intent, Uri uri) {
        if (!(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)) {
            return;
        }
        if (context == null || intent == null || uri == null) {
            return;
        }
        if (uri.getScheme() != null && uri.getScheme().startsWith("http") || uri.getScheme().startsWith("https")) {
            return;
        }
        context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }
}
