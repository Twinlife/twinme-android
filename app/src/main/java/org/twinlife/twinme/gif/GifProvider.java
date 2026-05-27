/*
 *  Copyright (c) 2026 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  GifProvider: abstraction over a remote GIF catalog (Tenor, Giphy, ...).
 *  Implementations fetch trending GIFs and search by keyword, and map the
 *  remote JSON onto the provider-agnostic GifItem model. Pagination is opaque:
 *  pass back the nextPosition received to load the next page.
 */

package org.twinlife.twinme.gif;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public interface GifProvider {

    interface Callback {
        void onResult(@NonNull List<GifItem> items, @Nullable String nextPosition);

        void onError(@NonNull Exception error);
    }

    @NonNull
    String getDisplayName();

    boolean isConfigured();

    void fetchTrending(int limit, @Nullable String position, @NonNull Callback callback);

    void search(@NonNull String query, int limit, @Nullable String position, @NonNull Callback callback);
}
