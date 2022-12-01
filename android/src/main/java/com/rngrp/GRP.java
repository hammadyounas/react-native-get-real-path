package com.rngrp;

import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.util.Date;
import java.util.Random;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.database.Cursor;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

public class GRP extends ReactContextBaseJavaModule {

  public GRP(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @Override
  public String getName() {
    return "GRP";
  }

  private WritableMap makeErrorPayload(Exception ex) {
    WritableMap error = Arguments.createMap();
    error.putString("message", ex.getMessage());
    return error;
  }

  @ReactMethod
  public void getRealPathFromURI(String uriString, Callback callback) {
    Uri uri = Uri.parse(uriString);
    try {
      Context context = getReactApplicationContext();
      final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
       if ("content".equalsIgnoreCase(uri.getScheme())) {
        callback.invoke(null,getDataColumn(context, uri, null, null));
      }
      else if ("file".equalsIgnoreCase(uri.getScheme())) {
        callback.invoke(null, uri.getPath());
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      callback.invoke(makeErrorPayload(ex));
    }
  }

  public static String random() {
    Random generator = new Random();
    StringBuilder randomStringBuilder = new StringBuilder();
    int randomLength = generator.nextInt(10);
    char tempChar;
    for (int i = 0; i < randomLength; i++){
      tempChar = (char) (generator.nextInt(96) + 32);
      randomStringBuilder.append(tempChar);
    }
    return randomStringBuilder.toString();
  }

  public static boolean isMediaDocument(Uri uri) {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
  }

  public static boolean isDownloadsDocument(Uri uri) {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
  }
  public static boolean isExternalStorageDocument(Uri uri) {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
  }
  public static String getDataColumn(Context context, Uri uri, String selection,
                                     String[] selectionArgs) {
    // https://github.com/hiddentao/cordova-plugin-filepath/pull/6
    Cursor cursor = null;
    final String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};

    try {
      /* get `_data` */
      cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
      if (cursor != null && cursor.moveToFirst()) {
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        /* bingo! */
        final String filepath = cursor.getString(column_index);

        if (filepath != null) {
          return filepath;
        } else {
          column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
          final String displayName = cursor.getString(column_index);

          return writeFile(context, uri, displayName);
        }
      }
    } catch (Exception e) {
      if (cursor != null) {
        final int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME);
        final String displayName = cursor.getString(column_index);

        return writeFile(context, uri, displayName);
      }
    } finally {
      if (cursor != null)
        cursor.close();
    }
    return null;
  }
  public static String writeFile(Context context, Uri uri, String displayName) {
    InputStream input = null;
    try {
      input = context.getContentResolver().openInputStream(uri);
      /* save stream to temp file */
      try {
        File file = new File(context.getCacheDir(), displayName);
        OutputStream output = new FileOutputStream(file);
        try {
          byte[] buffer = new byte[4 * 1024]; // or other buffer size
          int read;

          while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
          }
          output.flush();

          final String outputPath = file.getAbsolutePath();
          return outputPath;

        } finally {
          output.close();
        }
      } catch (Exception e1a) {
        //
      } finally {
        try {
          input.close();
        } catch (IOException e1b) {
          //
        }
      }
    } catch (FileNotFoundException e2) {
      //
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e3) {
          //
        }
      }
    }

    return null;
  }
}
