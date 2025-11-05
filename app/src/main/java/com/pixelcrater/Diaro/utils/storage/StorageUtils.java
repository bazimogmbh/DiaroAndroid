package com.pixelcrater.Diaro.utils.storage;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.utils.AppLog;
import com.pixelcrater.Diaro.utils.Static;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class StorageUtils {

    public static String getDefaultExternalStorage() {
//        AppLog.d("getExternalCacheDir(): " + MyApp.getInstance().getExternalCacheDir());
//        AppLog.d("getFilesDir(): " + MyApp.getInstance().getFilesDir());
//        AppLog.i("getFilesDir(): " + MyApp.getInstance().getExternalFilesDir(null));

        File defaultExternalStorage ;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.P){
            defaultExternalStorage = Environment.getExternalStorageDirectory();
        } else{
            defaultExternalStorage = MyApp.getInstance().getExternalFilesDir(null);
        }

        return defaultExternalStorage.getAbsolutePath();
    }

    public static String getLogDirPath() {
        return MyApp.getInstance().getExternalCacheDir() + "/log";
    }

    /**
     * Deletes file/directory recursively
     */
    public static boolean deleteFileOrDirectory(File file) {
        // AppLog.d("file: " + file);
        return FileUtils.deleteQuietly(file);
    }

    /**
     * Raturns all available SD-Cards in the system (include emulated)
     * <p>
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standard way to get it.
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static String[] getStorageDirectoriesForJellyBean() {
        final Pattern DIR_SEPARATOR = Pattern.compile("/");
        // Final set of paths
        final Set<String> rv = new HashSet<>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        AppLog.d("rawExternalStorage: " + rawExternalStorage);
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        AppLog.d("rawSecondaryStoragesStr: " + rawSecondaryStoragesStr);
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        AppLog.d("rawEmulatedStorageTarget: " + rawEmulatedStorageTarget);

        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            final String[] folders = DIR_SEPARATOR.split(path);
            final String lastFolder = folders[folders.length - 1];
            boolean isDigit = false;
            try {
                Integer.valueOf(lastFolder);
                isDigit = true;
            } catch (NumberFormatException ignored) {
            }
            rawUserId = isDigit ? lastFolder : "";
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splitted into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        return rv.toArray(new String[rv.size()]);
    }

    public static String[] getExternalStorageDirectories() {

        final Set<String> hashSet = new HashSet<>();

        File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(MyApp.getInstance(), null);
        if (externalFilesDirs != null) {
            AppLog.d("externalFilesDirs.length: " + externalFilesDirs.length);

            for (final File externalFilesDir : externalFilesDirs) {
                AppLog.d("externalFilesDir: " + externalFilesDir);
                if (externalFilesDir == null) {
                    continue;
                }
                AppLog.d("externalFilesDir.getAbsolutePath(): " + externalFilesDir.getAbsolutePath());

                hashSet.add(externalFilesDir.getParentFile().getParentFile().getParentFile()
                        .getParentFile().getAbsolutePath());
            }
        }

        AppLog.d("hashSet: " + hashSet);
        return hashSet.toArray(new String[hashSet.size()]);
    }

    public static long getAvailableSpaceInBytes(File directory) {
        return directory.getUsableSpace();
    }

    public static String getAvailableSpaceWithUnits(File directory) {
        return getSizeWithUnits(getAvailableSpaceInBytes(directory), null);
    }

    public static long getTotalSpaceInBytes(File directory) {
        return directory.getTotalSpace();
    }

    public static String getTotalSpaceWithUnits(File directory) {
        return getSizeWithUnits(getTotalSpaceInBytes(directory), null);
    }

    public static String getSizeWithUnits(long bytes, String units) {
        final long SIZE_KB = 1024L;
        final long SIZE_MB = SIZE_KB * SIZE_KB;
        final long SIZE_GB = SIZE_MB * SIZE_KB;
        DecimalFormat precision = new DecimalFormat("0.00");

        if (units == null) {
            if (bytes < SIZE_MB) {
                return precision.format((double) bytes / SIZE_KB) + " KB";
            } else if (bytes < SIZE_GB) {
                return precision.format((double) bytes / SIZE_MB) + " MB";
            } else {
                return precision.format((double) bytes / SIZE_GB) + " GB";
            }
        } else {
            switch (units) {
                case "KB":
                    return precision.format((double) bytes / SIZE_KB) + " KB";
                case "MB":
                    return precision.format((double) bytes / SIZE_MB) + " MB";
                case "GB":
                    return precision.format((double) bytes / SIZE_GB) + " GB";
                default:
                    return "0.00 B";
            }
        }
    }

    private static long getUsedSizeInBytes(File directory, long blockSize) {
        File[] files = directory.listFiles();
        if (files != null) {
            // space used by directory itself
            long size = directory.length();

            for (File file : files) {
                if (file.isDirectory()) {
                    // space used by subdirectory
                    size += getUsedSizeInBytes(file, blockSize);
                } else {
                    // file size need to rounded up to full block sizes
                    // (not a perfect function, it adds additional block to 0 sized files
                    // and file who perfectly fill their blocks)
                    size += (file.length() / blockSize + 1) * blockSize;
                }
            }
            return size;
        } else {
            return 0;
        }
    }

    public static long getUsedSizeInBytes(File directory) {
        if (!directory.exists()) {
            return 0;
        }

        StatFs statFs = new StatFs(directory.getAbsolutePath());
        long blockSize;
        blockSize = statFs.getBlockSizeLong();

        return getUsedSizeInBytes(directory, blockSize);
    }

    public static String getFilenameFromUri(Uri uri){
        String fileName = "";
        String[] filePathColumn = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = MyApp.getInstance().getContentResolver().query(uri, filePathColumn, null, null, null);
        if (cursor.moveToFirst()) {
            int fileNameIndex = cursor.getColumnIndex(filePathColumn[0]);
            fileName = cursor.getString(fileNameIndex);
          //  AppLog.i(  fileName );
        }
        cursor.close();
        return fileName;
    }

    public static void writeImageUriToFile(Uri uri, File file) {
        try (final InputStream input = MyApp.getInstance().getContentResolver().openInputStream(uri); OutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void compressPhoto(String path , int maxImageWH, int quality) throws IOException {
        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        int actualWidth = options.outWidth;
        int actualHeight = options.outHeight;
        String imageType = options.outMimeType;

        // do not compress gifs
        if(!StringUtils.isEmpty(imageType)) {
            if (imageType.equals("image/gif"))
                return;
        }
        // sometimes options.outMimeType is null , ??
        String extension = FileUtil.getExtension(path);
        if(!StringUtils.isEmpty(extension)) {
            if(extension.equals("gif"))
                return;
        }

        Bitmap newBitmap = null;
        Bitmap tmpBitmap = null;

        //  Bitmap (5494x5839) needs to be resized to (1926x2048)
        //  inSampleSize = 2,  Sampled bitmap dimensions: 2747x2920
        if (actualWidth > maxImageWH || actualHeight > maxImageWH) {
            float ratio;
            if (actualWidth > actualHeight) {
                ratio = actualWidth / (float) maxImageWH;
            } else {
                ratio = actualHeight / (float) maxImageWH;
            }

            int reqWidth = (int) (actualWidth / ratio);
            int reqHeight = (int) (actualHeight / ratio);

            // Do not load 5494x5839 bitmap in image, load the 2747x2920
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;

            tmpBitmap = BitmapFactory.decodeFile(path, options);
            // now scale it to max 2048 wh
            newBitmap = Bitmap.createScaledBitmap(tmpBitmap, reqWidth, reqHeight, true);

            AppLog.d(String.format(Locale.US, "Bitmap (%dx%d) needs to be resized to (%dx%d)", actualWidth, actualHeight, newBitmap.getWidth(), newBitmap.getHeight()));
            // AppLog.e(String.format(Locale.US, "Sampled bitmap loading dimensions: %dx%d", bmp.getWidth(), bmp.getHeight()));

        } else {
            options.inJustDecodeBounds = false;
            newBitmap = BitmapFactory.decodeFile(path, options);
         //   AppLog.e(String.format(Locale.US, "Bitmap of size %dx%d doesn't need resize", actualWidth, actualHeight));
        }

        ExifInterface oldExif = null;
        int rotation = 0;
        try {
            oldExif = new ExifInterface(path);
            int orientation = oldExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            rotation = getExifRotation(orientation);

        } catch (Exception e) {
            AppLog.e("no exif data found");
        }

        if(newBitmap == null ||  newBitmap.isRecycled() )
            return;

        // Rotate
        if (rotation != 0) {
            AppLog.d("Rotating to degrees ->" + rotation);
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedBitmap = Bitmap.createBitmap(newBitmap, 0, 0, newBitmap.getWidth(), newBitmap.getHeight(), matrix, true);
            FileOutputStream out = new FileOutputStream(path);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            rotatedBitmap.recycle();
            out.close();
        } else {
            //compress image to 90% of its quality
            FileOutputStream out = new FileOutputStream(path);
            if(newBitmap.hasAlpha()){
                newBitmap.compress(Bitmap.CompressFormat.PNG, quality, out);
            }
            else{
                newBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            }
            out.close();
        }

        if (newBitmap != null) {
            newBitmap.recycle();
        }

        if (tmpBitmap!=null) {
            tmpBitmap.recycle();
        }

        // copy exif data , resetting its orientation
        copyExif(oldExif, path);
    }

    private static int getExifRotation(int orientation) {
        int rotate = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }
        return rotate;
    }

    // Calculate the largest inSampleSize value that is a power of 2 and keeps both
    // height and width larger than the requested height and width.
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        AppLog.d("inSampleSize = " + inSampleSize);
        return inSampleSize;
    }

    static String[] attributes = new String[]{
            ExifInterface.TAG_F_NUMBER,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_ORIGINAL,
            //  ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_EXPOSURE_TIME,
            ExifInterface.TAG_FLASH,
            ExifInterface.TAG_FOCAL_LENGTH,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            //  ExifInterface.TAG_ORIENTATION,
            //     ExifInterface.TAG_SUBSEC_TIME,
            //     ExifInterface.TAG_SUBSEC_TIME_DIG,
            //      ExifInterface.TAG_SUBSEC_TIME_ORIG,
            ExifInterface.TAG_WHITE_BALANCE
    };


    public static void copyExif(ExifInterface originalExif, String imageOutputPath) {
        if (originalExif == null) {
            AppLog.e("originalExif is null!");
            return;
        }

        try {
            ExifInterface newExif = new ExifInterface(imageOutputPath);
            String value;
            for (String attribute : attributes) {
                value = originalExif.getAttribute(attribute);
                if (value != null) {
                    if (!TextUtils.isEmpty(value)) {
                        newExif.setAttribute(attribute, value);
                    }
                }
            }

            newExif.resetOrientation();
            newExif.saveAttributes();
          //  AppLog.e("exif copied!");

        } catch (IOException e) {
            AppLog.e("copy exif error-> " + Arrays.toString(e.getStackTrace()));
        }
    }

    public static void copyAsset(Context ctx, String assetInPath, String fileOutPath) {
        File fileIn = new File(assetInPath);
        if (!fileIn.exists()) {
            try {
                final InputStream is = ctx.getAssets().open(assetInPath);
                final FileOutputStream out = new FileOutputStream(fileOutPath);
                final byte[] buffer = new byte[4096];
                int size = 0;
                while ((size = is.read(buffer)) != -1) {
                    out.write(buffer, 0, size);
                }
                out.flush();
                out.close();
                is.close();
                AppLog.d(assetInPath + " copied successfully");
            } catch (final IOException e) {
                AppLog.d(assetInPath + " copy failed");
                e.printStackTrace();
            }
        } else
            AppLog.d(fileOutPath + " exists");
    }

    public static String getStringFromAsset(Context ctx, String assetFile) {
        String template = "";
        try {
            InputStream stream = ctx.getAssets().open(assetFile);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();
            template = new String(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return template;
    }

    public static void writeToFile(String content, Uri uri, Activity activityCompat) {
        if (ContextCompat.checkSelfPermission(activityCompat, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activityCompat, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        } else {
            OutputStream outputStream;
            try {
                outputStream = activityCompat.getContentResolver().openOutputStream(uri);
                PrintStream ps = new PrintStream(Objects.requireNonNull(outputStream));
                ps.print(content);
                ps.flush();
                ps.close();

                outputStream.flush();
                outputStream.close();

                Static.showToastSuccess("File exported!");
            } catch (IOException e) {
                e.printStackTrace();
                AppLog.e("Exception writing to " + uri);
                Static.showToastError(e.getMessage() + "  " + uri.getPath());
            }

        }
    }

}