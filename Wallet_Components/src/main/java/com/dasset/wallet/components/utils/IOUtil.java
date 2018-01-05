package com.dasset.wallet.components.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.dasset.wallet.components.constant.Regex;
import com.google.common.collect.Lists;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;

public class IOUtil {

    private static IOUtil ioUtil;

    private IOUtil() {
    }

    public static synchronized IOUtil getInstance() {
        if (ioUtil == null) {
            ioUtil = new IOUtil();
        }
        return ioUtil;
    }

    private File getFilePath(Context ctx) {
        File path;
        if (isSDCardExsist()) {
//            Cache = getExternalPath(ctx, Regex.LOG.getRegext());
            path = ctx.getExternalFilesDir(Regex.LOG.getRegext());

        } else {
            path = new File(ctx.getFilesDir(), Regex.LOG.getRegext());
//            Cache = ctx.getFilesDir();
        }
        if (path != null) {
            if (path.exists()) {
                return path;
            } else {
                path.mkdirs();
            }
        }
        return path;
    }

    public List<File> getFiles(Context ctx) {
        File path = getFilePath(ctx);
        LogUtil.getInstance().print("Cache:" + path.getAbsolutePath());
        LogUtil.getInstance().print("listFiles:" + path.listFiles());
        LogUtil.getInstance().print("exists:" + path.exists());
        List<File> files = Lists.newArrayList();
        if (path.listFiles() != null && path.exists()) {
            for (File file : path.listFiles()) {
                LogUtil.getInstance().print("file:" + file.getAbsolutePath());
                if (file.getName().endsWith(Regex.LOG.getRegext()) && file.getName().contains(DeviceUtil.getInstance().getDeviceId(ctx))) {
                    files.add(file);
                }
            }
            return files;
        } else {
            return null;
        }
    }

    public void deleteFile(final Context ctx) {
        for (File file : getFilePath(ctx).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
//                return pathname.isFile() && pathname.getName().endsWith(Regex.LOG_TYPE.getRegext());
                return pathname.isFile()
                        && pathname.getName().endsWith(Regex.LOG_TYPE.getRegext())
                        && pathname.getName().contains(DeviceUtil.getInstance().getDeviceId(ctx));
            }
        })) {
            file.delete();
        }
    }

    public void deleteFile(String path) {
        if (isSDCardExsist()) {
            File folder = new File(path);
            File[] files = folder.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

    public void deleteFile(File folder) {
        if (isSDCardExsist()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
    }

    public void deleteFile(String path, String fileName) {
        if (isSDCardExsist()) {
            File folder = new File(path);
            File[] files = folder.listFiles();
            for (File file : files) {
                if (file.getName().split("\\.")[0].equals(fileName)) {
                    file.delete();
                }
            }
        }
    }

    public File forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            LogUtil.getInstance().print(String.format("%s is exists", directory.getAbsolutePath()));
            if (!directory.isDirectory()) {
                LogUtil.getInstance().print(String.format("%s exists and is not a directory. Unable to create directory.", directory.getAbsolutePath()));
            }
        } else {
            LogUtil.getInstance().print(String.format("%s is not exists", directory.getAbsolutePath()));
            if (!directory.mkdirs()) {
                if (!directory.isDirectory()) {
                    LogUtil.getInstance().print(String.format("Unable to create directory %s", directory.getAbsolutePath()));
                    return null;
                }
            }
        }
        return directory;
    }

    public File forceMkdir(String directoryPath) throws IOException {
        return forceMkdir(new File(directoryPath));
    }

    public boolean writeBytes(String path, byte[] data) {
        if (!TextUtils.isEmpty(path) && data != null) {
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = new FileOutputStream(new File(path));
                fileOutputStream.write(data);
                fileOutputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    public void writeObject(Object object, File file) {
        if (object != null && file != null) {
            ByteArrayOutputStream byteArrayOutputStream = null;
            ObjectOutputStream objectOutputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                byteArrayOutputStream = new ByteArrayOutputStream();
                objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
                objectOutputStream.writeObject(object);
                objectOutputStream.flush();
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(byteArrayOutputStream.toByteArray());
                fileOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (byteArrayOutputStream != null) {
                        byteArrayOutputStream.close();
                    }
                    if (objectOutputStream != null) {
                        objectOutputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void writeObject(Object object, String filePath) {
        writeObject(object, new File(filePath));
    }

    public byte[] readBytes(String path) {
        byte[] bytes = null;
        if (!TextUtils.isEmpty(path)) {
            FileInputStream fileInputStream;
            try {
                File file = new File(path);
                fileInputStream = new FileInputStream(file);
                bytes = readBytes(fileInputStream);
                return bytes;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public byte[] readBytes(InputStream inputStream) {
        // this dynamically extends to take the bytes you read
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // this is storage overwritten on each iteration with bytes
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        // we need to know how may bytes were read to write them to the byteBuffer
        int length;
        try {
            while ((length = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (inputStream != null) try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // and then we can return your byte array.
        return byteArrayOutputStream.toByteArray();
    }

    public Object readObject(File file) {
        if (file.exists()) {
            Object object = null;
            FileInputStream fileInputStream = null;
            ObjectInputStream objectInputStream = null;
            try {
                fileInputStream = new FileInputStream(file);
                objectInputStream = new ObjectInputStream(fileInputStream);
                object = objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (objectInputStream != null) {
                        objectInputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return object;
        } else {
            return null;
        }
    }

    public Object readObject(String filePath) {
        return readObject(new File(filePath));
    }

    public boolean writeFile(Context ctx, String message, boolean isAppend) {
        File path = getFilePath(ctx);
        if (path != null) {
            try {
                File file = new File(path, DeviceUtil.getInstance().getDeviceId(ctx) + Regex.LOG_TYPE.getRegext());
                FileOutputStream stream = new FileOutputStream(file, isAppend);
                stream.write(message.getBytes(Regex.UTF_8.getRegext()));
                stream.flush();
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean isSDCardExsist() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    public File getExternalStorageDirectory(String directoryName) throws IOException {
        if (isSDCardExsist()) {
            return forceMkdir(new File(Environment.getExternalStorageDirectory(), directoryName));
        }
        return null;
    }

    public String readByte(Context ctx) {
        File dir = getFilePath(ctx);
        String[] files = dir.list();
        StringBuilder builder = new StringBuilder();
        for (String data : files) {
            File file = new File(getFilePath(ctx) + File.separator + data);
            LogUtil.getInstance().print("file_name:" + getFilePath(ctx) + File.separator + data);
            if (file.getName().endsWith(Regex.LOG_TYPE.getRegext())) {
                BufferedInputStream bufferedInputStream = null;
                try {
                    bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
                    byte[] buffer = new byte[1024];
                    int length;
                    showAvailableBytes(bufferedInputStream);
                    while ((length = bufferedInputStream.read(buffer)) != -1) {
                        // System.out.write(buffer, 0, length);
                        builder.append(new String(buffer, 0, length, Regex.UTF_8.getRegext()));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (bufferedInputStream != null) {
                        try {
                            bufferedInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                return null;
            }
        }
        return builder.toString();
    }

    public String readLine(Context ctx) {
        String[] files = getFilePath(ctx).list();
        StringBuilder builder = new StringBuilder();
        for (String data : files) {
            File file = new File(getFilePath(ctx) + File.separator + data);
            LogUtil.getInstance().print("file_name:" + getFilePath(ctx) + File.separator + data);
            if (file.getName().endsWith(Regex.LOG_TYPE.getRegext()) && file.getName().contains(DeviceUtil.getInstance().getDeviceId(ctx))) {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Regex.UTF_8.getRegext()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line).append(System.getProperty(Regex.LINE_SEPARATOR.getRegext()));
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                return null;
            }
        }
        return builder.toString();
    }

    private void showAvailableBytes(InputStream inputStream) {
        try {
            LogUtil.getInstance().print("当前输入流中的字节数为：" + inputStream.available());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(InputStream in, File file) {
        OutputStream out = null;
        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getMimeType(Context ctx, Uri uri) {
        String extension;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(ctx.getContentResolver().getType(uri));
        } else {
            extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(new File(uri.getPath())).toString());
        }
        return extension;
    }

    public void copyFile(String originalFilePath, String targetFilePath) {
        LogUtil.getInstance().print(String.format("originalFilePath:%s", originalFilePath));
        LogUtil.getInstance().print(String.format("targetFilePath:%s", targetFilePath));
        if (!TextUtils.isEmpty(originalFilePath) && !TextUtils.isEmpty(targetFilePath)) {
            File file = new File(originalFilePath);
            if (file.exists()) {
                FileInputStream fileInputStream = null;
                FileOutputStream fileOutputStream = null;
                try {
                    fileInputStream = new FileInputStream(originalFilePath);
                    fileOutputStream = new FileOutputStream(targetFilePath);
                    byte[] buffer = new byte[fileInputStream.available()];
                    fileInputStream.read(buffer);
                    fileOutputStream.write(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                LogUtil.getInstance().print(String.format("%s is not exists", file.getName()));
            }
        }
    }
}
