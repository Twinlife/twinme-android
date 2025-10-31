/*
 *  Copyright (c) 2020-2024 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.services;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ExportedImageId;
import org.twinlife.twinlife.ImageId;
import org.twinlife.twinlife.ImageService;
import org.twinlife.twinme.TwinmeContext;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.spaces.CustomAppearance;

import java.io.File;
import java.util.UUID;

public class SpaceAppearanceService extends AbstractTwinmeService {
    private static final String LOG_TAG = "SpaceAppearanceService";
    private static final boolean DEBUG = false;

    private static final int GET_SPACE = 1;
    private static final int GET_SPACE_DONE = 1 << 1;
    private static final int UPDATE_SPACE = 1 << 2;
    private static final int UPDATE_SPACE_DONE = 1 << 3;
    private static final int CREATE_IMAGE = 1 << 4;
    private static final int CREATE_IMAGE_DONE = 1 << 5;
    private static final int DELETE_IMAGE = 1 << 6;
    private static final int DELETE_IMAGE_DONE = 1 << 7;
    private static final int UPDATE_DEFAULT_SPACE_SETTINGS = 1 << 8;
    private static final int UPDATE_DEFAULT_SPACE_SETTINGS_DONE = 1 << 9;

    public interface Observer extends AbstractTwinmeService.Observer, SpaceObserver {

        void onUpdateSpace(@NonNull Space space);

        void onGetConversationImage(@NonNull ExportedImageId imageId, @NonNull Bitmap bitmap);

        void onUpdateDefaultSpaceSettings(SpaceSettings spaceSettings);
    }

    private class TwinmeContextObserver extends AbstractTwinmeService.TwinmeContextObserver {

        @Override
        public void onUpdateSpace(long requestId, @NonNull Space space) {
            if (DEBUG) {
                Log.d(LOG_TAG, "TwinmeContextObserver.onUpdateSpace: requestId=" + requestId + " space=" + space);
            }

            Integer operationId = getOperation(requestId);
            if (operationId != null) {
                SpaceAppearanceService.this.onUpdateSpace(space);
            }
        }
    }

    @Nullable
    private Observer mObserver;
    private int mState = 0;
    private int mWork = 0;
    private Space mSpace;
    private SpaceSettings mSpaceSettings;
    private Bitmap mConversationBackgroundLightBitmap;
    private File mConversationBackgroundLightFile;
    private Bitmap mConversationBackgroundDarkBitmap;
    private File mConversationBackgroundDarkFile;
    private ExportedImageId mRemoveImageId;
    private boolean mCleanBackgroundImage = false;
    private boolean mIsConversationBackgroundLightImage = false;
    private boolean mUpdateConversationBackgroundLightColor = false;
    private boolean mUpdateConversationBackgroundDarkColor = false;

    public SpaceAppearanceService(@NonNull AbstractTwinmeActivity activity, @NonNull TwinmeContext twinmeContext, @NonNull Observer observer) {
        super(LOG_TAG, activity, twinmeContext, observer);
        if (DEBUG) {
            Log.d(LOG_TAG, "SpaceService: activity=" + activity + " twinmeContext=" + twinmeContext + " observer=" + observer);
        }

        mObserver = observer;
        mTwinmeContextObserver = new TwinmeContextObserver();
        mTwinmeContext.setObserver(mTwinmeContextObserver);
    }

    public void dispose() {
        if (DEBUG) {
            Log.d(LOG_TAG, "dispose");
        }

        mObserver = null;
        super.dispose();
    }

    public void getSpace(UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSpace: spaceId= " + spaceId);
        }

        showProgressIndicator();

        mTwinmeContext.getSpace(spaceId, (ErrorCode errorCode, Space space) -> {
            mSpace = space;
            runOnGetSpace(mObserver, space, null);
            mState |= GET_SPACE_DONE;
            onOperation();
        });
    }

    public void getConversationImage(@NonNull UUID imageId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "getConversationImage: imageId=" + imageId);
        }

        ImageService imageService = mTwinmeContext.getImageService();
        ExportedImageId exportedImageId = imageService.getImageId(imageId);

        Bitmap cachedImage = imageService.getCachedImage(exportedImageId, ImageService.Kind.NORMAL);

        if (cachedImage != null && mObserver != null) {
            mObserver.onGetConversationImage(exportedImageId, cachedImage);
        } else {
            mTwinmeContext.executeImage(() -> {
                Bitmap image = imageService.getImage(exportedImageId, ImageService.Kind.NORMAL);
                if (image != null) {
                    runOnUiThread(() -> {
                        if (mObserver != null) {
                            mObserver.onGetConversationImage(exportedImageId, image);
                        }
                    });
                }
            });
        }
    }

    public void updateSpace(SpaceSettings spaceSettings) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSpace: spaceSettings=" + spaceSettings);
        }

        mSpaceSettings = spaceSettings;

        mWork |= UPDATE_SPACE;
        mState &= ~(UPDATE_SPACE | UPDATE_SPACE_DONE);

        showProgressIndicator();

        startOperation();
    }

    public void updateSpace(SpaceSettings spaceSettings, @Nullable Bitmap conversationBackgroundLightBitmap, @Nullable File conversationBackgroundLightFile, @Nullable Bitmap conversationBackgroundDarkBitmap, @Nullable File conversationBackgroundDarkFile, boolean updateConversationBackgroundLightColor, boolean updateConversationBackgroundDarkColor) {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSpace: spaceSettings=" + spaceSettings);
        }

        mSpaceSettings = spaceSettings;
        mConversationBackgroundLightBitmap = conversationBackgroundLightBitmap;
        mConversationBackgroundLightFile = conversationBackgroundLightFile;
        mConversationBackgroundDarkBitmap = conversationBackgroundDarkBitmap;
        mConversationBackgroundDarkFile = conversationBackgroundDarkFile;
        mUpdateConversationBackgroundLightColor = updateConversationBackgroundLightColor;
        mUpdateConversationBackgroundDarkColor = updateConversationBackgroundDarkColor;

        if (mConversationBackgroundLightBitmap != null) {
            mIsConversationBackgroundLightImage = true;
            mWork |= CREATE_IMAGE;
            mState &= ~(CREATE_IMAGE | CREATE_IMAGE_DONE);
        } else if (mConversationBackgroundDarkBitmap != null) {
            mWork |= CREATE_IMAGE;
            mState &= ~(CREATE_IMAGE | CREATE_IMAGE_DONE);
        } else if (mSpace != null) {
            mWork |= UPDATE_SPACE;
            mState &= ~(UPDATE_SPACE | UPDATE_SPACE_DONE);
        } else {
            mWork |= UPDATE_DEFAULT_SPACE_SETTINGS;
            mState &= ~(UPDATE_DEFAULT_SPACE_SETTINGS | UPDATE_DEFAULT_SPACE_SETTINGS_DONE);
        }

        showProgressIndicator();

        startOperation();
    }

    private void deleteImage(@Nullable UUID imageId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "deleteImage: imageId=" + imageId);
        }

        if (imageId == null) {
            return;
        }
        mRemoveImageId = mTwinmeContext.getImageService().getImageId(imageId);
        if (mRemoveImageId == null) {
            return;
        }

        mWork |= DELETE_IMAGE;
        mState &= ~(DELETE_IMAGE | DELETE_IMAGE_DONE);
        startOperation();
    }

    private void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        mState |= UPDATE_SPACE_DONE;

        mSpaceSettings = space.getSpaceSettings();

        if (mUpdateConversationBackgroundLightColor) {
            UUID lightImageId = mSpaceSettings.getUUID(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_IMAGE);
            mSpaceSettings.remove(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_IMAGE);
            if (lightImageId != null) {
                mCleanBackgroundImage = true;
                deleteImage(lightImageId);
                return;
            }
        }

        if (mUpdateConversationBackgroundDarkColor) {
            UUID darkImageId = mSpaceSettings.getUUID(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE);
            mSpaceSettings.remove(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE);
            if (darkImageId != null) {
                mCleanBackgroundImage = true;
                deleteImage(darkImageId);
                return;
            }
        }

        if (mObserver != null) {
            mObserver.onUpdateSpace(space);
        }

        onOperation();
    }

    private void onUpdateDefaultSpaceSettings(@NonNull SpaceSettings spaceSettings) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateDefaultSpaceSettings: spaceSettings=" + spaceSettings);
        }

        mState |= UPDATE_DEFAULT_SPACE_SETTINGS_DONE;

        mSpaceSettings = spaceSettings;

        if (mUpdateConversationBackgroundLightColor) {
            UUID lightImageId = mSpaceSettings.getUUID(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_IMAGE);
            mSpaceSettings.remove(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_IMAGE);
            if (lightImageId != null) {
                mCleanBackgroundImage = true;
                deleteImage(lightImageId);
                return;
            }
        }

        if (mUpdateConversationBackgroundDarkColor) {
            UUID darkImageId = mSpaceSettings.getUUID(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE);
            mSpaceSettings.remove(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE);
            if (darkImageId != null) {
                mCleanBackgroundImage = true;
                deleteImage(darkImageId);
                return;
            }
        }

        if (mObserver != null) {
            mObserver.onUpdateDefaultSpaceSettings(mSpaceSettings);
        }

        onOperation();
    }

    private void onCreateImage(@NonNull ExportedImageId imageId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateImage: imageId=" + imageId);
        }

        mState |= CREATE_IMAGE_DONE;

        if (mIsConversationBackgroundLightImage) {
            UUID removeImageId = mSpaceSettings.getUUID(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_IMAGE);
            mSpaceSettings.remove(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_COLOR);
            mSpaceSettings.setUUID(CustomAppearance.PROPERTY_CONVERSATION_BACKGROUND_IMAGE, imageId.getExportedId());

            if (removeImageId != null) {
                deleteImage(removeImageId);
                return;
            }

            if (mConversationBackgroundDarkBitmap != null) {
                mIsConversationBackgroundLightImage = false;
                mWork |= CREATE_IMAGE;
                mState &= ~(CREATE_IMAGE | CREATE_IMAGE_DONE);
            } else if (mSpace != null) {
                mWork |= UPDATE_SPACE;
                mState &= ~(UPDATE_SPACE | UPDATE_SPACE_DONE);
            } else {
                mWork |= UPDATE_DEFAULT_SPACE_SETTINGS;
                mState &= ~(UPDATE_DEFAULT_SPACE_SETTINGS | UPDATE_DEFAULT_SPACE_SETTINGS_DONE);
            }
        } else {
            UUID removeImageId = mSpaceSettings.getUUID(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE);
            mSpaceSettings.remove(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_COLOR);
            mSpaceSettings.setUUID(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE, imageId.getExportedId());

            if (removeImageId != null) {
                deleteImage(removeImageId);
                return;
            }

            if (mSpace != null) {
                mWork |= UPDATE_SPACE;
                mState &= ~(UPDATE_SPACE | UPDATE_SPACE_DONE);
            } else {
                mWork |= UPDATE_DEFAULT_SPACE_SETTINGS;
                mState &= ~(UPDATE_DEFAULT_SPACE_SETTINGS | UPDATE_DEFAULT_SPACE_SETTINGS_DONE);
            }
        }

        onOperation();
    }

    private void onDeleteImage(@NonNull ImageId imageId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteImage: imageId=" + imageId);
        }

        mState |= DELETE_IMAGE_DONE;

        if (mCleanBackgroundImage && mUpdateConversationBackgroundDarkColor) {
            UUID darkImageId = mSpaceSettings.getUUID(CustomAppearance.PROPERTY_DARK_CONVERSATION_BACKGROUND_IMAGE);
            if (darkImageId != null) {
                mCleanBackgroundImage = false;
                deleteImage(darkImageId);
                return;
            }
        } else if (mIsConversationBackgroundLightImage) {
            if (mConversationBackgroundDarkBitmap != null) {
                mIsConversationBackgroundLightImage = false;
                mWork |= CREATE_IMAGE;
                mState &= ~(CREATE_IMAGE | CREATE_IMAGE_DONE);
                onOperation();
                return;
            }
        }

        if (mSpace != null) {
            mWork |= UPDATE_SPACE;
            mState &= ~(UPDATE_SPACE | UPDATE_SPACE_DONE);
        } else {
            mWork |= UPDATE_DEFAULT_SPACE_SETTINGS;
            mState &= ~(UPDATE_DEFAULT_SPACE_SETTINGS | UPDATE_DEFAULT_SPACE_SETTINGS_DONE);
        }

        onOperation();
    }

    protected void onOperation() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onOperation");
        }

        if (!mIsTwinlifeReady) {
            return;
        }

        // We must update space settings
        if ((mWork & UPDATE_SPACE) != 0) {
            if ((mState & UPDATE_SPACE) == 0) {
                mState |= UPDATE_SPACE;
                long requestId = newOperation(UPDATE_SPACE);
                mTwinmeContext.updateSpace(requestId, mSpace, mSpaceSettings, null, null);
                return;
            }
            if ((mState & UPDATE_SPACE_DONE) == 0) {
                return;
            }
        }

        if ((mWork & CREATE_IMAGE) != 0) {
            if ((mState & CREATE_IMAGE) == 0) {
                mState |= CREATE_IMAGE;

                Bitmap bitmap = mConversationBackgroundLightBitmap;
                File file = mConversationBackgroundLightFile;

                if (!mIsConversationBackgroundLightImage) {
                    bitmap = mConversationBackgroundDarkBitmap;
                    file = mConversationBackgroundDarkFile;
                }

                mTwinmeContext.getImageService().createLocalImage(file, bitmap, (ErrorCode errorCode, ExportedImageId imageId) -> {
                    mState |= CREATE_IMAGE_DONE;
                    if (errorCode == ErrorCode.SUCCESS && imageId != null) {
                        onCreateImage(imageId);
                    }
                    onOperation();
                });
                return;
            }

            if ((mState & CREATE_IMAGE_DONE) == 0) {
                return;
            }
        }

        if ((mWork & DELETE_IMAGE) != 0) {
            if ((mState & DELETE_IMAGE) == 0) {
                mState |= DELETE_IMAGE;

                mTwinmeContext.getImageService().deleteImage(mRemoveImageId, (ErrorCode errorCode, ImageId imageId) -> {
                    mState |= DELETE_IMAGE_DONE;
                    if (errorCode == ErrorCode.SUCCESS && imageId != null) {
                        onDeleteImage(imageId);
                    }
                    onOperation();
                });
                return;
            }

            if ((mState & DELETE_IMAGE_DONE) == 0) {
                return;
            }
        }

        if ((mWork & UPDATE_DEFAULT_SPACE_SETTINGS) != 0) {
            if ((mState & UPDATE_DEFAULT_SPACE_SETTINGS) == 0) {
                mState |= UPDATE_DEFAULT_SPACE_SETTINGS;

                long requestId = newOperation(UPDATE_DEFAULT_SPACE_SETTINGS);
                if (DEBUG) {
                    Log.d(LOG_TAG, "updateSpace: requestId=" + requestId + " spaceSettings= " + mSpaceSettings);
                }

                mTwinmeContext.saveDefaultSpaceSettings(mSpaceSettings, (ErrorCode errorCode, SpaceSettings settings) -> {
                    if (errorCode == ErrorCode.SUCCESS && settings != null) {
                        onUpdateDefaultSpaceSettings(settings);
                    }
                });
                return;
            }

            if ((mState & UPDATE_DEFAULT_SPACE_SETTINGS_DONE) == 0) {
                return;
            }
        }

        // Nothing more to do, we can hide the progress indicator.
        hideProgressIndicator();
    }

    protected void onError(int operationId, ErrorCode errorCode, @Nullable String errorParameter) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onError: operationId=" + operationId + " errorCode=" + errorCode + " errorParameter=" + errorParameter);
        }

        // Wait for reconnection
        if (errorCode == ErrorCode.TWINLIFE_OFFLINE) {
            mRestarted = true;

            return;
        }

        if (operationId == GET_SPACE && errorCode == ErrorCode.ITEM_NOT_FOUND) {

            mState |= GET_SPACE_DONE;
            runOnGetSpace(mObserver, null, null);
            return;
        }

        super.onError(operationId, errorCode, errorParameter);
    }
}