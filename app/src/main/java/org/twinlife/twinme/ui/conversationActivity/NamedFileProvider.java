/*
 *  Copyright (c) 2018 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Yannis Le Gal (Yannis.LeGal@twin.life)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 */

package org.twinlife.twinme.ui.conversationActivity;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class NamedFileProvider extends ContentProvider {

    private static final String[] COLUMNS = {
            OpenableColumns.DISPLAY_NAME,
            OpenableColumns.SIZE
    };

    private static NamedFileProvider mInstance;

    private String mAuthority;
    private final List<Pair<String, String>> mFiles = new ArrayList<>();

    public static NamedFileProvider getInstance() {

        return mInstance;
    }

    private static int modeToMode(String mode) {

        int modeBits;
        switch (mode) {
            case "r":
                modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
                break;

            case "w":
            case "wt":
                modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE;
                break;

            case "wa":
                modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_APPEND;
                break;

            case "rw":
                modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE;
                break;

            case "rwt":
                modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE | ParcelFileDescriptor.MODE_TRUNCATE;
                break;

            default:
                throw new IllegalArgumentException("Invalid mode: " + mode);
        }

        return modeBits;
    }

    public Uri getUriForFile(@SuppressWarnings("unused") @NonNull Context context, @NonNull File file, @NonNull String name) {

        mFiles.add(new Pair<>(name, file.getPath()));

        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.scheme("content");
        uriBuilder.authority(mAuthority);
        uriBuilder.path(Integer.toString(mFiles.size()));

        return uriBuilder.build();
    }

    @Override
    public void attachInfo(@NonNull Context context, @NonNull ProviderInfo info) {
        super.attachInfo(context, info);

        // Sanity check our security
        if (info.exported) {
            throw new SecurityException("Provider must not be exported");
        }
        if (!info.grantUriPermissions) {
            throw new SecurityException("Provider must grant uri permissions");
        }

        mAuthority = info.authority;
    }

    @Override
    public boolean onCreate() {
        mInstance = this;
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        File file = getFileForUri(uri);
        String displayName = getNameForUri(uri);

        if (projection == null) {
            projection = COLUMNS;
        }

        String[] columns = new String[projection.length];
        Object[] values = new Object[projection.length];
        int i = 0;
        for (String column : projection) {
            if (OpenableColumns.DISPLAY_NAME.equals(column)) {
                columns[i] = OpenableColumns.DISPLAY_NAME;
                values[i++] = displayName;
            } else if (OpenableColumns.SIZE.equals(column)) {
                columns[i] = OpenableColumns.SIZE;
                values[i++] = file.length();
            }
        }

        columns = copyOf(columns, i);
        values = copyOf(values, i);
        final MatrixCursor cursor = new MatrixCursor(columns, 1);
        cursor.addRow(values);

        return cursor;
    }

    @Override
    @Nullable
    public String getType(@NonNull Uri uri) {

        File file = getFileForUri(uri);
        int lastDot = file.getName().lastIndexOf('.');
        if (lastDot >= 0) {
            String extension = file.getName().substring(lastDot + 1);
            String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mime != null) {

                return mime;
            }
        }

        return "application/octet-stream";
    }

    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {

        return ParcelFileDescriptor.open(getFileForUri(uri), modeToMode(mode));
    }

    @Override
    @Nullable
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {

        throw new UnsupportedOperationException();
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

        throw new UnsupportedOperationException();
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        throw new UnsupportedOperationException();
    }

    //
    // Private methods
    //

    private File getFileForUri(Uri uri) {

        return new File(mFiles.get(getIndexForUri(uri)).second);
    }

    private String getNameForUri(Uri uri) {

        return mFiles.get(getIndexForUri(uri)).first;
    }

    private int getIndexForUri(Uri uri) {

        List<String> segments = uri.getPathSegments();

        return Integer.parseInt(segments.get(segments.size() - 1)) - 1;
    }

    private String[] copyOf(String[] source, int length) {

        final String[] result = new String[length];
        System.arraycopy(source, 0, result, 0, length);

        return result;
    }

    private Object[] copyOf(Object[] source, int length) {

        final Object[] result = new Object[length];
        System.arraycopy(source, 0, result, 0, length);

        return result;
    }
}