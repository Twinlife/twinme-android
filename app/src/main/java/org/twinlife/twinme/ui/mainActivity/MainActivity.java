/*
 *  Copyright (c) 2014-2025 twinlife SA.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Tristan Garaud (Tristan.Garaud@twinlife-systems.com)
 *   Denis Campredon (Denis.Campredon@twinlife-systems.com)
 *   Thibaud David (contact@thibauddavid.com)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Romain Kolb (romain.kolb@skyrock.com)
 */

package org.twinlife.twinme.ui.mainActivity;

import static org.twinlife.twinme.ui.Intents.INTENT_PROFILE_ID;
import static org.twinlife.twinme.ui.Intents.INTENT_SPACE_ID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.percentlayout.widget.PercentRelativeLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.installreferrer.api.InstallReferrerClient;
import com.android.installreferrer.api.InstallReferrerStateListener;
import com.android.installreferrer.api.ReferrerDetails;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.twinlife.twinlife.ProxyDescriptor;
import org.twinlife.twinlife.SNIProxyDescriptor;
import org.twinlife.device.android.twinme.BuildConfig;
import org.twinlife.device.android.twinme.R;
import org.twinlife.twinlife.AndroidDeviceInfo;
import org.twinlife.twinlife.BaseService.ErrorCode;
import org.twinlife.twinlife.ConnectivityService;
import org.twinlife.twinlife.ConversationService;
import org.twinlife.twinlife.NotificationService;
import org.twinlife.twinlife.TrustMethod;
import org.twinlife.twinlife.TwincodeURI;
import org.twinlife.twinlife.util.Utils;
import org.twinlife.twinme.models.CallReceiver;
import org.twinlife.twinme.models.Contact;
import org.twinlife.twinme.models.Profile;
import org.twinlife.twinme.models.Space;
import org.twinlife.twinme.models.SpaceSettings;
import org.twinlife.twinme.services.MainService;
import org.twinlife.twinme.skin.CircularImageDescriptor;
import org.twinlife.twinme.skin.Design;
import org.twinlife.twinme.skin.DisplayMode;
import org.twinlife.twinme.ui.AbstractTwinmeActivity;
import org.twinlife.twinme.ui.AcceptInvitationActivity;
import org.twinlife.twinme.ui.AddContactActivity;
import org.twinlife.twinme.ui.EditProfileActivity;
import org.twinlife.twinme.ui.FatalErrorActivity;
import org.twinlife.twinme.ui.Intents;
import org.twinlife.twinme.ui.Settings;
import org.twinlife.twinme.ui.ShowContactActivity;
import org.twinlife.twinme.ui.ShowProfileActivity;
import org.twinlife.twinme.ui.TwinmeApplication;
import org.twinlife.twinme.ui.accountActivity.AccountActivity;
import org.twinlife.twinme.ui.contacts.MenuAddContactView;
import org.twinlife.twinme.ui.contacts.SuccessAuthentifiedRelationView;
import org.twinlife.twinme.ui.conversationActivity.ConversationActivity;
import org.twinlife.twinme.ui.externalCallActivity.CreateExternalCallActivity;
import org.twinlife.twinme.ui.externalCallActivity.TransferCallActivity;
import org.twinlife.twinme.ui.groups.AcceptGroupInvitationActivity;
import org.twinlife.twinme.ui.inAppSubscriptionActivity.AcceptInvitationSubscriptionActivity;
import org.twinlife.twinme.ui.inAppSubscriptionActivity.BillingManager;
import org.twinlife.twinme.ui.inAppSubscriptionActivity.InAppSubscriptionActivity;
import org.twinlife.twinme.ui.mainActivity.skredBoard.SkredBoardFragment;
import org.twinlife.twinme.ui.mainActivity.skredBoard.SkredBoardFragmentDelegate;
import org.twinlife.twinme.ui.mainActivity.skredBoard.SkredboardGestureListener;
import org.twinlife.twinme.ui.groups.ShowGroupActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumServicesActivity;
import org.twinlife.twinme.ui.premiumServicesActivity.PremiumFeatureConfirmView;
import org.twinlife.twinme.ui.premiumServicesActivity.UIPremiumFeature;
import org.twinlife.twinme.ui.privacyActivity.PrivacyActivity;
import org.twinlife.twinme.ui.profiles.AddProfileActivity;
import org.twinlife.twinme.ui.settingsActivity.AboutActivity;
import org.twinlife.twinme.ui.settingsActivity.HelpActivity;
import org.twinlife.twinme.ui.settingsActivity.MessagesSettingsActivity;
import org.twinlife.twinme.ui.settingsActivity.PersonalizationActivity;
import org.twinlife.twinme.ui.settingsActivity.SettingsAdvancedActivity;
import org.twinlife.twinme.ui.settingsActivity.SoundsSettingsActivity;
import org.twinlife.twinme.ui.spaces.OnboardingSpaceActivity;
import org.twinlife.twinme.ui.spaces.SecretSpaceActivity;
import org.twinlife.twinme.ui.spaces.ShowSpaceActivity;
import org.twinlife.twinme.ui.spaces.SpaceSettingProperty;
import org.twinlife.twinme.ui.spaces.TemplateSpaceActivity;
import org.twinlife.twinme.ui.spaces.UISpace;
import org.twinlife.twinme.utils.AbstractConfirmView;
import org.twinlife.twinme.utils.AlertMessageView;
import org.twinlife.twinme.utils.CircularImageView;
import org.twinlife.twinme.utils.CommonUtils;
import org.twinlife.twinme.utils.DefaultConfirmView;
import org.twinlife.twinme.utils.OnboardingDetailView;
import org.twinlife.twinme.utils.RoundedView;
import org.twinlife.twinme.utils.UIMenuSelectAction;
import org.twinlife.twinme.utils.WhatsNewDialog;
import org.twinlife.twinme.utils.coachmark.CoachMark;
import org.twinlife.twinme.utils.coachmark.CoachMarkView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AbstractTwinmeActivity implements MainService.Observer, AbstractTwinmeActivity.Observer, SkredBoardFragmentDelegate, BillingManager.BillingManagerListener {
    private static final String LOG_TAG = "MainActivity";
    private static final boolean DEBUG = false;

    private static final String SKREDBOARD_FRAGMENT = "skredboard";

    private static final String CHECKED_REFERRER = "CHECKED_REFERRER";

    private static final String SPACES_FRAGMENT_TAG = "SPACES";
    private static final String CALLS_FRAGMENT_TAG = "CALLS";
    private static final String CONTACTS_FRAGMENT_TAG = "CONTACTS";
    private static final String CHAT_FRAGMENT_TAG = "CHAT";
    private static final String NOTIFICATIONS_FRAGMENT_TAG = "NOTIFICATIONS";
    private static final String SELECTED_BOTTOM_TAB = "selectedBottomTab";

    private static final int PROFILE_FRAGMENT_INDEX = 0;
    private static final int CALLS_FRAGMENT_INDEX = 1;
    private static final int CONTACTS_FRAGMENT_INDEX = 2;
    private static final int CHAT_FRAGMENT_INDEX = 3;
    private static final int NOTIFICATIONS_FRAGMENT_INDEX = 4;
    private static final int TAB_COUNT = 5;

    private Bundle mSavedInstanceState;

    private static final float DESIGN_SIDE_MENU_WIDTH = 680f;
    private static final float DESIGN_SIDE_MENU_SETTINGS_WIDTH = 520f;
    private static final float DESIGN_SIDE_SPACES_WIDTH = 160f;
    private static final float DESIGN_SIDE_SETTINGS_WIDTH = 520f;
    private static final float DESIGN_SPACE_ITEM_HEIGHT = 160f;
    private static final float DESIGN_CREATE_SPACE_ICON_SIZE = 70f;

    private static final int COACH_MARK_DELAY = 500;

    private BottomNavigationView mBottomNavigationView;
    private View mBottomNavigationShadowView;
    private FrameLayout mFragmentFrameLayout;
    private DrawerLayout mDrawerLayout;
    private View mDrawerContainer;
    private ListView mDrawerListView;
    private View mLeftMenuView;
    private RecyclerView mSpaceDrawerListView;
    private View mCreateSpaceView;
    private ImageView mCreateSpaceImageView;
    private LinearLayoutManager mUISpacesLinearLayoutManager;
    private SideMenuListAdapter mSideMenuListAdapter;
    private SpacesSideMenuListAdapter mSpaceSideMenuListAdapter;
    private CircularImageView mAvatarView;
    private RoundedView mNotificationView;
    private CircularImageView mCallsAvatarView;
    private RoundedView mCallsNotificationView;
    private CircularImageView mConversationsAvatarView;
    private RoundedView mConversationsNotificationView;
    private RelativeLayout mContentFrameLayout;
    private View mOverlayView;
    private CoachMarkView mCoachMarkView;

    private volatile boolean mInitNavigationItemOnResume = true;
    @Nullable
    private Profile mProfile;

    @Nullable
    private Space mSpace;
    private final List<UISpace> mUISpaces = new ArrayList<>();

    private BillingManager mBillingManager;

    private UUID mTransferCallId;

    private MainService mMainService;

    private SharedPreferences mSharedPreferences;
    private SkredBoardFragment mSkredBoardFragment;

    private boolean mIsOnPause = false;
    private boolean mMenuHiddenMode = true;
    private boolean mCreateLevel = false;
    private boolean mHasPendingNotification = false;
    private boolean mHasConversations = false;
    private int mNbContacts = 0;
    private boolean mOnlyGroups = false;
    private boolean mOnlyMissedCalls = false;
    private boolean mCheckReferer = true;
    private boolean mShowWhatsNew = false;
    private boolean mUpdateStatusColor = true;
    private int mTouchEventCount = 0;

    private WeakReference<TabbarFragment> mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreate: savedInstanceState=" + savedInstanceState);
        }

        super.onCreate(savedInstanceState);

        mSavedInstanceState = savedInstanceState;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

        mMainService = new MainService(this, getTwinmeContext(), this);
        mBillingManager = new BillingManager(this, this);

        initViews();

        parseIntent(getIntent());

        // Restore the splash screen flag at the end.
        if (savedInstanceState != null) {
            mBottomNavigationView.setSelectedItemId(savedInstanceState.getInt(SELECTED_BOTTOM_TAB));
            mInitNavigationItemOnResume = false;
        }
    }

    public Space getSpace() {

        return mSpace;
    }

    @Nullable
    public Profile getProfile() {

        return mProfile;
    }

    @SuppressWarnings("unused")
    public View getOverlayView() {

        return mOverlayView;
    }

    public boolean getOnlyGroups() {

        return mOnlyGroups;
    }

    public void setOnlyGroups(boolean onlyGroups) {

        mOnlyGroups = onlyGroups;
    }

    public boolean getOnlyMissedCalls() {

        return mOnlyMissedCalls;
    }

    public void setOnlyMissedCalls(boolean onlyMissedCalls) {

        mOnlyMissedCalls = onlyMissedCalls;
    }

    public void inviteContact() {
        if (DEBUG) {
            Log.d(LOG_TAG, "inviteContact");
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        if (mProfile != null) {
            MenuAddContactView menuAddContactView = new MenuAddContactView(this, null);
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            menuAddContactView.setLayoutParams(layoutParams);

            MenuAddContactView.Observer observer = new MenuAddContactView.Observer() {
                @Override
                public void onStartAddContactByScan() {

                    menuAddContactView.animationCloseMenu();

                    Intent intent = new Intent();
                    intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId());
                    intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.SCAN);
                    intent.setClass(getBaseContext(), AddContactActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onStartAddContactByInvite() {

                    menuAddContactView.animationCloseMenu();

                    Intent intent = new Intent();
                    intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId());
                    intent.putExtra(Intents.INTENT_INVITATION_MODE, AddContactActivity.InvitationMode.INVITE);
                    intent.setClass(getBaseContext(), AddContactActivity.class);
                    startActivity(intent);
                }

                @Override
                public void onCloseMenuSelectActionAnimationEnd() {

                    mDrawerLayout.removeView(menuAddContactView);
                    setStatusBarColor();
                }
            };

            menuAddContactView.setObserver(observer);

            mDrawerLayout.addView(menuAddContactView);

            List<UIMenuSelectAction> actions = new ArrayList<>();
            actions.add(new UIMenuSelectAction(getString(R.string.contacts_fragment_scan_contact_title), R.drawable.scan_code));
            actions.add(new UIMenuSelectAction(getString(R.string.contacts_fragment_invite_contact_title), R.drawable.qrcode));
            menuAddContactView.setActions(actions, this);
            menuAddContactView.openMenu(false);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        } else {
            Intent intent = new Intent();
            intent.setClass(this, AddProfileActivity.class);
            intent.putExtra(Intents.INTENT_FIRST_PROFILE, true);
            startActivity(intent);
        }
    }

    public void showProfile() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showProfile");
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        Intent intent = new Intent();
        if (mSpace == null) {
            intent.putExtra(Intents.INTENT_FIRST_PROFILE, true);
            intent.setClass(this, AddProfileActivity.class);
        } else if (mSpace.getProfile() == null) {
            intent.putExtra(INTENT_SPACE_ID, mSpace.getId().toString());
            intent.setClass(this, EditProfileActivity.class);
        } else {
            intent.putExtra(INTENT_PROFILE_ID, mSpace.getProfile().getId().toString());
            intent.setClass(this, ShowProfileActivity.class);
        }
        startActivity(intent);
    }

    public void openSideMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openSideMenu");
        }
        

        mDrawerLayout.openDrawer(GravityCompat.START);
        showCoachMark();
    }

    public void openDrawerLayout() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openDrawerLayout");
        }

        if (mSideMenuListAdapter.isFeatureSubscribed() != isFeatureSubscribed(org.twinlife.twinme.TwinmeApplication.Feature.GROUP_CALL)) {
            mDrawerListView.invalidateViews();
        }
        
        mUISpacesLinearLayoutManager.scrollToPositionWithOffset(0, (int) (-DESIGN_SPACE_ITEM_HEIGHT * Design.HEIGHT_RATIO));

        ViewGroup.LayoutParams layoutParams = mDrawerListView.getLayoutParams();
        if (!mUISpaces.isEmpty()) {
            layoutParams.width = (int) (DESIGN_SIDE_MENU_SETTINGS_WIDTH * Design.WIDTH_RATIO);
            mLeftMenuView.setVisibility(View.VISIBLE);
        } else {
            layoutParams.width = (int) (DESIGN_SIDE_MENU_WIDTH * Design.WIDTH_RATIO);
            mLeftMenuView.setVisibility(View.GONE);
        }

        updateSideMenu();

        mDrawerLayout.openDrawer(mDrawerContainer);

        showCoachMark();
    }

    public void openSkredBoard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "openSkredBoard");
        }

        mSkredBoardFragment.openSkredBoard();
    }

    public void closeSkredBoard() {
        if (DEBUG) {
            Log.d(LOG_TAG, "closeSkredBoard");
        }

        mSkredBoardFragment.closeSkredBoard();
    }

    private void checkReferrer() {
        if (DEBUG) {
            Log.d(LOG_TAG, "checkReferrer");
        }

        // Prevent multiple calls to checkReferrer until the callback has finished and we permanently save
        // the information in CHECKED_REFERRER preference.
        if (!mCheckReferer) {
            return;
        }
        mCheckReferer = false;

        InstallReferrerClient client = InstallReferrerClient.newBuilder(this).build();
        try {

            client.startConnection(new InstallReferrerStateListener() {

                @Override
                public void onInstallReferrerSetupFinished(int responseCode) {

                    switch (responseCode) {
                        case InstallReferrerClient.InstallReferrerResponse.OK:
                            try {
                                ReferrerDetails response = client.getInstallReferrer();
                                client.endConnection();

                                SharedPreferences.Editor editor = mSharedPreferences.edit();
                                editor.putBoolean(CHECKED_REFERRER, true);
                                editor.apply();

                                String referrer = response.getInstallReferrer();
                                String decodedReferrer = Uri.decode(referrer);

                                Log.e(LOG_TAG, "Installed with referrer: " + decodedReferrer);
                                if (decodedReferrer != null) {

                                    String[] params = decodedReferrer.split("&");
                                    String twincodeId = null;
                                    for (String param : params) {
                                        String[] paramSplit = param.split("=");
                                        if (paramSplit.length == 2 && paramSplit[0].equals(BuildConfig.INVITATION_PARAM_ID)) {
                                            twincodeId = paramSplit[1];
                                            break;
                                        }
                                    }

                                    if (twincodeId != null) {
                                        Log.i(LOG_TAG, "Found invitation twincode in referrer: '" + twincodeId + "'");
                                        Intent intent = new Intent(MainActivity.this, AcceptInvitationActivity.class);
                                        intent.putExtra(Intents.INTENT_INVITATION_LINK, decodedReferrer);
                                        startActivity(intent);
                                        overridePendingTransition(0, 0);
                                    } else {
                                        Log.i(LOG_TAG, "No invitation twincode in referrer");
                                    }
                                }
                            } catch (RemoteException exception) {
                                Log.e(LOG_TAG, "Error occurred while handling install referrer", exception);
                            }
                            break;

                        case InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED:
                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE:
                        case InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR:
                        case InstallReferrerClient.InstallReferrerResponse.SERVICE_DISCONNECTED:
                            break;
                    }
                }

                @Override
                public void onInstallReferrerServiceDisconnected() {

                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            });
        } catch (SecurityException exception) {
            Log.e(LOG_TAG, "checkReferrer exception=" + exception);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState: outState=" + outState);
        }

        outState.putInt(SELECTED_BOTTOM_TAB, mBottomNavigationView.getSelectedItemId());
        getSupportFragmentManager().putFragment(outState, SKREDBOARD_FRAGMENT, mSkredBoardFragment);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onConfigurationChanged: newConfig" + newConfig);
        }

        super.onConfigurationChanged(newConfig);

        initViews();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onActivityResult requestCode=" + requestCode + " resultCode=" + resultCode + " data=" + data);
        }

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CallsFragment.REQUEST_EXTERNAL_CALL_ONBOARDING && resultCode == RESULT_OK) {
            showPremiumFeatureView(UIPremiumFeature.FeatureType.CLICK_TO_CALL);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onNewIntent intent=" + intent);
        }

        super.onNewIntent(intent);

        parseIntent(intent);
    }

    @Override
    protected void onStop() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onStop");
        }

        super.onStop();
    }

    @Override
    protected void onPause() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onPause");
        }

        super.onPause();

        mIsOnPause = true;
    }

    @Override
    protected void onResume() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }

        super.onResume();

        mIsOnPause = false;

        if (getTwinmeApplication().isRunning()) {

            if (mInitNavigationItemOnResume) {
                mInitNavigationItemOnResume = false;

                if (getTwinmeApplication().defaultTab() == TwinmeApplication.DefaultTab.SPACES.ordinal()) {
                    mBottomNavigationView.setSelectedItemId(R.id.navigation_spaces);
                } else if (getTwinmeApplication().defaultTab() == TwinmeApplication.DefaultTab.CALLS.ordinal()) {
                    mBottomNavigationView.setSelectedItemId(R.id.navigation_calls);
                } else if (getTwinmeApplication().defaultTab() == TwinmeApplication.DefaultTab.CONTACTS.ordinal()) {
                    mBottomNavigationView.setSelectedItemId(R.id.navigation_contacts);
                } else if (getTwinmeApplication().defaultTab() == TwinmeApplication.DefaultTab.CONVERSATIONS.ordinal()) {
                    mBottomNavigationView.setSelectedItemId(R.id.navigation_chat);
                } else {
                    mBottomNavigationView.setSelectedItemId(R.id.navigation_notifications);
                }
            } else if (getTwinmeApplication().showUpgradeScreen()) {
                startActivity(PremiumServicesActivity.class);
            } else if (mSpace != null && !getTwinmeApplication().canShowUpgradeScreen()) {
                mMainService.getConversations();
                mMainService.getContacts();
            }

            showWhatsNew();
            showToolBar(true);
            updateFragment();
            mFragmentFrameLayout.setVisibility(View.VISIBLE);
            mBottomNavigationView.setVisibility(View.VISIBLE);
            mBottomNavigationShadowView.setVisibility(View.VISIBLE);

            if (mBillingManager != null) {
                mBillingManager.fetchPurchases();
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDestroy");
        }

        super.onDestroy();

        mBillingManager.dispose();
        mMainService.dispose();
    }

    private void parseIntent(Intent intent) {
        if (DEBUG) {
            Log.d(LOG_TAG, "parseIntent: intent=" + intent);
        }

        mHasConversations = intent.getBooleanExtra(Intents.INTENT_HAS_CONVERSATIONS, false);
        UUID spaceId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_SPACE_ID));

        if (spaceId != null && (mSpace == null || mSpace.getId() != spaceId)) {
            mMainService.setSpace(spaceId);
        }

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            Uri uri = intent.getData();
            if (uri != null) {
                String path = uri.getPath();
                if (path == null || "/".equals(path)) {
                    // Invitation with twincodeId or skredcodeId
                    String twincodeId = uri.getQueryParameter(BuildConfig.INVITATION_PARAM_ID);
                    if (twincodeId == null) {
                        Bundle bundle = intent.getExtras();
                        if (bundle != null) {
                            // Intent redirection from https://invite.<host> web site or from https://authenticate.<host>
                            // (this could contain a public key).
                            twincodeId = (String) bundle.get("org.twinlife.device.android.twinme.twincodeId");
                            if (twincodeId == null) {
                                twincodeId = (String) bundle.get("org.twinlife.device.android.twinme.authenticate");
                                while (twincodeId != null && twincodeId.startsWith("/")) {
                                    twincodeId = twincodeId.substring(1);
                                }
                            }
                            if (twincodeId == null) {
                                twincodeId = (String) bundle.get("org.twinlife.device.android.twinme.proxy");
                                while (twincodeId != null && twincodeId.startsWith("/")) {
                                    twincodeId = twincodeId.substring(1);
                                }
                            }
                        }
                    }
                    if (twincodeId == null || twincodeId.isEmpty()) {
                        return;
                    }
                    uri = Uri.parse(twincodeId);
                }
                mMainService.parseURI(uri, (ErrorCode errorCode, TwincodeURI twincodeURI) -> {
                    if (errorCode == ErrorCode.SUCCESS && twincodeURI != null) {
                        if (twincodeURI.kind == TwincodeURI.Kind.Invitation) {
                            Intent lIntent = new Intent(Intent.ACTION_VIEW);
                            if (twincodeURI.twincodeOptions != null) {
                                lIntent.setClass(this, AcceptInvitationSubscriptionActivity.class);
                            } else {
                                lIntent.setClass(this, AcceptInvitationActivity.class);
                            }
                            lIntent.putExtra(Intents.INTENT_TRUST_METHOD, TrustMethod.LINK);
                            lIntent.setData(Uri.parse(twincodeURI.uri));
                            startActivity(lIntent);

                        } else if (twincodeURI.kind == TwincodeURI.Kind.Authenticate) {

                            mMainService.verifyAuthenticateURI(Uri.parse(twincodeURI.uri), ((ErrorCode error, Contact contact) -> {
                                if (error == ErrorCode.SUCCESS) {
                                    mMainService.getImage(contact, (Bitmap avatar) -> showSuccessAuthentification(contact.getName(), avatar));
                                } else {
                                    showAlertMessage(getLinkError(error, R.string.add_contact_activity_scan_error_incorect_link));
                                }
                            }));
                        } else if (twincodeURI.kind == TwincodeURI.Kind.Proxy) {
                            addProxy(twincodeURI.twincodeOptions);
                        } else {
                            showAlertMessage(getLinkError(twincodeURI.kind, R.string.add_contact_activity_scan_error_incorect_link));
                        }
                    } else {
                        showAlertMessage(getLinkError(errorCode, R.string.add_contact_activity_scan_error_incorect_link));
                    }
                });
            }
        } else if (intent.getBooleanExtra(Intents.INTENT_NEW_MESSAGE, false)) {
            UUID id = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));

            if (id != null) {
                Intent lIntent = new Intent(this, ConversationActivity.class);
                lIntent.putExtra(Intents.INTENT_CONTACT_ID, id.toString());

                startActivity(lIntent);
            } else {
                id = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));

                if (id != null) {
                    Intent lIntent = new Intent(this, ConversationActivity.class);
                    lIntent.putExtra(Intents.INTENT_GROUP_ID, id.toString());

                    startActivity(lIntent);
                }
            }
        } else if (intent.getBooleanExtra(Intents.INTENT_NEW_INVITATION, false)) {
            UUID id = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
            ConversationService.DescriptorId invitationId = ConversationService.DescriptorId.fromString(intent.getStringExtra(Intents.INTENT_INVITATION_ID));

            if (id != null && invitationId != null) {
                Intent lIntent = new Intent(this, AcceptGroupInvitationActivity.class);
                lIntent.putExtra(Intents.INTENT_CONTACT_ID, id.toString());
                lIntent.putExtra(Intents.INTENT_INVITATION_ID, invitationId.toString());
                startActivity(lIntent);
                overridePendingTransition(0, 0);
            }
        } else if (intent.getBooleanExtra(Intents.INTENT_NEW_CONTACT_INVITATION, false)) {
            ConversationService.DescriptorId descriptorId = ConversationService.DescriptorId.fromString(intent.getStringExtra(Intents.INTENT_DESCRIPTOR_ID));
            UUID groupId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));
            UUID contactId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));
            UUID notificationId = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_NOTIFICATION_ID));
            Intent lIntent = new Intent(this, AcceptInvitationActivity.class);
            if (descriptorId != null) {
                lIntent.putExtra(Intents.INTENT_DESCRIPTOR_ID, descriptorId.toString());
            }
            if (groupId != null) {
                lIntent.putExtra(Intents.INTENT_GROUP_ID, groupId.toString());
            }
            if (contactId != null) {
                lIntent.putExtra(Intents.INTENT_CONTACT_ID, contactId.toString());
            }
            if (notificationId != null) {
                lIntent.putExtra(Intents.INTENT_NOTIFICATION_ID, notificationId.toString());
            }
            startActivity(lIntent);
            overridePendingTransition(0, 0);
        } else if (intent.getBooleanExtra(Intents.INTENT_NEW_CONTACT, false)) {
            UUID id = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_CONTACT_ID));

            if (id != null) {
                startActivity(ShowContactActivity.class, Intents.INTENT_CONTACT_ID, id);
            } else {
                id = Utils.UUIDFromString(intent.getStringExtra(Intents.INTENT_GROUP_ID));
                if (id != null) {
                    startActivity(ShowGroupActivity.class, Intents.INTENT_GROUP_ID, id);
                }
            }
        }
    }

    @Override
    public void onToolBarClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onToolBarClick");
        }

        if (mMenuHiddenMode) {
            mTouchEventCount++;
            if (mTouchEventCount == SideMenuListAdapter.NUMBER_TAP_HIDDEN_MODE) {
                mMenuHiddenMode = false;
                mSideMenuListAdapter.setHiddenMode(false);
                mDrawerListView.invalidateViews();
            }
        }
    }

    //
    // Implement BillingManagerListener.Observer methods
    //

    @Override
    public void onGetProducts(List<ProductDetails> productDetails) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProducts: " + productDetails);
        }

        if (productDetails != null) {
            mBillingManager.fetchPurchases();
        }
    }

    @Override
    public void onGetCurrentSubscription(Purchase purchase) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCurrentSubscription: " + purchase);
        }

        if (!purchase.getProducts().isEmpty()) {
            boolean isSubscribed = isFeatureSubscribed(org.twinlife.twinme.TwinmeApplication.Feature.GROUP_CALL);
            if (!isSubscribed) {
                mMainService.subscribeFeature(purchase.getProducts().get(0), purchase.getPurchaseToken(), purchase.getOrderId());
            }
        }

    }

    @Override
    public void onGetCurrentSubscriptionNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetCurrentSubscriptionNotFound");
        }

    }

    @Override
    public void onPurchaseSuccess(Purchase purchase) {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

    }

    @Override
    public void onPurchaseFailed() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }
    }

    //
    //
    // Implement SkredBoardFragmentDelegate
    //

    @Override
    public void skredboardDidValidateCode(SkredBoardFragment.SkredboardMode mode, @NonNull String code) {
        if (DEBUG) {
            Log.d(LOG_TAG, "skredboardDidValidateCode: mode=" + mode + "code=" + code);
        }

        mSkredBoardFragment.closeSkredBoard();

        switch (mode) {
            case SET_CODE:
                mMainService.setLevel(code);
                break;

            case CREATE_CODE:
                mCreateLevel = true;
                mMainService.createLevel(code);
                break;

            case DELETE_CODE:
                mMainService.deleteLevel(code);
                break;
        }
    }

    //
    // Implement MainService.Observer methods
    //

    @Override
    public void onSignIn() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSignIn");
        }
    }

    @Override
    public void onSignOut() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSignOut");
        }

        mProfile = null;
        mSpace = null;
    }

    @Override
    public void onGetSpaces(@NonNull List<Space> spaces) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpaces: spaces=" + spaces);
        }

        mUISpaces.clear();

        boolean allSpacesAreSecret = true;

        for (Space space : spaces) {
            if (!space.isSecret()) {
                allSpacesAreSecret = false;
                break;
            }
        }

        if (spaces.isEmpty() || allSpacesAreSecret) {
            mSpaceSideMenuListAdapter.notifyDataSetChanged();
            return;
        }

        AtomicInteger avatarCounter = new AtomicInteger(0);
        for (Space space : spaces) {
            if (!space.isSecret()) {
                avatarCounter.incrementAndGet();

                mMainService.getSpaceImage(space, (Bitmap spaceAvatar) ->
                        mMainService.getProfileImage(space.getProfile(), (Bitmap profileAvatar) -> {
                            mSpaceSideMenuListAdapter.updateUISpace(space, spaceAvatar, profileAvatar);

                            if (avatarCounter.decrementAndGet() == 0) {
                                if (mSpace != null) {
                                    for (UISpace lSpace : mUISpaces) {
                                        lSpace.setIsCurrentSpace(lSpace.getSpace().getId() == mSpace.getId());
                                    }
                                }

                                mSpaceSideMenuListAdapter.notifyDataSetChanged();
                            }
                        }));
            }
        }
    }

    @Override
    public void onCreateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateSpace: space=" + space);
        }

        if (space.isSecret()) {
            mSpaceSideMenuListAdapter.notifyDataSetChanged();
        } else {
            mMainService.getSpaceImage(space, (Bitmap spaceAvatar) ->
                    mMainService.getProfileImage(space.getProfile(), (Bitmap profileAvatar) -> {
                        mSpaceSideMenuListAdapter.updateUISpace(space, spaceAvatar, profileAvatar);
                        mSpaceSideMenuListAdapter.notifyDataSetChanged();
                    }));
        }
    }

    @Override
    public void onFatalError(@NonNull ErrorCode errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onFatalError errorCode=" + errorCode);
        }

        Intent intent = new Intent(this, FatalErrorActivity.class);
        intent.putExtra(Intents.INTENT_DATABASE_UPGRADED, getTwinmeContext().isDatabaseUpgraded());
        intent.putExtra(Intents.INTENT_ERROR_ID, errorCode.toString());
        startActivity(intent);
        finish();
    }

    @Override
    public void onUpdateSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateSpace: space=" + space);
        }

        if (mSpace != null && space.getId() == mSpace.getId()) {
            mSpace = space;
            mProfile = space.getProfile();

            SpaceSettings spaceSettings = mSpace.getSpaceSettings();
            if (spaceSettings.getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, true)) {
                spaceSettings = getTwinmeContext().getDefaultSpaceSettings();
            }

            Design.setMainStyle(spaceSettings.getStyle());
            Design.setupColor(getApplicationContext(), getTwinmeApplication());
            updateColor();

            if (!mShowWhatsNew) {
                setStatusBarColor();
            }

            FragmentManager fragmentManager = getSupportFragmentManager();
            if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_calls) {
                setToolBar(R.id.twinme_navigation_call_tool_bar);
                CallsFragment callsFragment = (CallsFragment) fragmentManager.findFragmentByTag(CALLS_FRAGMENT_TAG);
                if (callsFragment != null) {
                    callsFragment.updateColor();
                }
            } else {
                setToolBar(R.id.twinme_navigation_tool_bar);
                if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_spaces) {
                    SpacesFragment spaceFragment = (SpacesFragment) fragmentManager.findFragmentByTag(SPACES_FRAGMENT_TAG);
                    if (spaceFragment != null) {
                        spaceFragment.updateColor();
                    }
                } else if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_chat) {
                    ConversationsFragment conversationsFragment = (ConversationsFragment) fragmentManager.findFragmentByTag(CHAT_FRAGMENT_TAG);
                    if (conversationsFragment != null) {
                        conversationsFragment.updateColor();
                    }
                } else if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_contacts) {
                    ContactsFragment contactsFragment = (ContactsFragment) fragmentManager.findFragmentByTag(CONTACTS_FRAGMENT_TAG);
                    if (contactsFragment != null) {
                        contactsFragment.updateColor();
                    }
                }
            }

            updateBottomNavigationColor();

            SpaceSettings effectiveSpaceSettings = spaceSettings;
            mMainService.getSpaceImage(space, (Bitmap spaceAvatar) ->
                    mMainService.getProfileImage(mProfile, (Bitmap profileAvatar) -> {
                        mSpaceSideMenuListAdapter.updateUISpace(space, spaceAvatar, profileAvatar);
                        mSpaceSideMenuListAdapter.notifyDataSetChanged();

                        if (profileAvatar != null) {
                            mAvatarView.setImage(this, null,
                                    new CircularImageDescriptor(profileAvatar, 0.5f, 0.5f, 0.5f));
                            mCallsAvatarView.setImage(this, null,
                                    new CircularImageDescriptor(profileAvatar, 0.5f, 0.5f, 0.5f));
                            mConversationsAvatarView.setImage(this, null,
                                    new CircularImageDescriptor(profileAvatar, 0.5f, 0.5f, 0.5f));
                        }

                        UISpace uiSpace = new UISpace(getTwinmeApplication(), mSpace, spaceAvatar, profileAvatar, effectiveSpaceSettings);
                        mSideMenuListAdapter.setUISpace(uiSpace);
                        mDrawerListView.invalidateViews();
                    }));

        }
        boolean isReferrerChecked = mCheckReferer && mSharedPreferences.getBoolean(CHECKED_REFERRER, false);

        if (isReferrerChecked) {
            checkReferrer();
        }
    }

    @Override
    public void onDeleteSpace(@NonNull UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteSpace: spaceId=" + spaceId);
        }

        mSpaceSideMenuListAdapter.removeUISpace(spaceId);
    }

    @Override
    public void onUpdateProfile(@NonNull Profile profile) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdateProfile: profile=" + profile);
        }

        if (profile.getSpace() != null) {
            Space space = profile.getSpace();

            mMainService.getSpaceImage(space, (Bitmap spaceAvatar) ->
                    mMainService.getProfileImage(mProfile, (Bitmap profileAvatar) -> {
                        SpaceSettings spaceSettings = space.getSpaceSettings();
                        if (space.getSpaceSettings().getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_MESSAGE_SETTINGS, true)) {
                            spaceSettings = getTwinmeContext().getDefaultSpaceSettings();
                        }

                        UISpace uiSpace = new UISpace(getTwinmeApplication(), space, spaceAvatar, profileAvatar, spaceSettings);
                        if (mSpace != null && space.getId() == mSpace.getId()) {
                            mProfile = profile;

                            if (profileAvatar != null) {
                                mAvatarView.setImage(this, null,
                                        new CircularImageDescriptor(profileAvatar, 0.5f, 0.5f, 0.5f));
                                mCallsAvatarView.setImage(this, null,
                                        new CircularImageDescriptor(profileAvatar, 0.5f, 0.5f, 0.5f));
                                mConversationsAvatarView.setImage(this, null,
                                        new CircularImageDescriptor(profileAvatar, 0.5f, 0.5f, 0.5f));
                            }
                            mSideMenuListAdapter.setUISpace(uiSpace);
                        }

                        mSpaceSideMenuListAdapter.updateUISpace(space, spaceAvatar, profileAvatar);
                        mDrawerListView.invalidateViews();
                    }));
        }
    }

    @Override
    public void onGetContacts(int nbContacts) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetContacts: " + nbContacts);
        }

        mNbContacts = nbContacts;

        canShowUpgradeScreen();
    }

    @Override
    public void onGetConversations(int nbConversations) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetConversations: " + nbConversations);
        }

        mHasConversations = nbConversations > 0;

        canShowUpgradeScreen();
    }

    private void canShowUpgradeScreen() {
        if (DEBUG) {
            Log.d(LOG_TAG, "canShowUpgradeScreen");
        }

        if (mHasConversations && mNbContacts > 1) {
            getTwinmeApplication().setCanShowUpgradeScreen();
        }
    }

    @Override
    public void onSetCurrentSpace(@NonNull Space space) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetCurrentSpace: space=" + space);
        }

        if (mSpace != null && mSpace.getId().equals(space.getId())) {
            return;
        }

        if (mSpace != null && mSpace.isSecret()) {
            mSpaceSideMenuListAdapter.removeUISpace(mSpace.getId());
        }

        String lastLevelName;

        if (mSpace != null) {
            lastLevelName = mSpace.getName();
        } else {
            lastLevelName = "";
        }

        mSpace = space;
        mProfile = space.getProfile();
        mMainService.getPendingNotifications();

        SpaceSettings spaceSettings = mSpace.getSpaceSettings();
        if (spaceSettings.getBoolean(SpaceSettingProperty.PROPERTY_DEFAULT_APPEARANCE_SETTINGS, true)) {
            spaceSettings = getTwinmeContext().getDefaultSpaceSettings();
        }

        getTwinmeApplication().updateDisplayMode(Design.getDisplayMode(Integer.parseInt(spaceSettings.getString(SpaceSettingProperty.PROPERTY_DISPLAY_MODE, DisplayMode.SYSTEM.ordinal() + ""))));

        Design.setMainStyle(spaceSettings.getStyle());
        Design.setupColor(getApplicationContext(), getTwinmeApplication());
        updateColor();

        if (!mIsOnPause) {
            updateFragment();
            showWhatsNew();
        }

        updateBottomNavigationColor();
        SpaceSettings effectiveSpaceSettings = spaceSettings;
        mMainService.getSpaceImage(mSpace, (Bitmap spaceAvatar) ->
                mMainService.getProfileImage(mProfile, (Bitmap profileAvatar) -> {
                    if (profileAvatar != null) {
                        mAvatarView.setImage(this, null,
                                new CircularImageDescriptor(profileAvatar, 0.5f, 0.5f, 0.5f));
                        mCallsAvatarView.setImage(this, null,
                                new CircularImageDescriptor(profileAvatar, 0.5f, 0.5f, 0.5f));
                        mConversationsAvatarView.setImage(this, null,
                                new CircularImageDescriptor(profileAvatar, 0.5f, 0.5f, 0.5f));
                    }

                    UISpace uiSpace = new UISpace(getTwinmeApplication(), mSpace, spaceAvatar, profileAvatar, effectiveSpaceSettings);
                    mSideMenuListAdapter.setUISpace(uiSpace);
                    mDrawerListView.invalidateViews();

                    if (mSpace.isSecret()) {
                        mSpaceSideMenuListAdapter.updateUISpace(mSpace, spaceAvatar, profileAvatar);
                    }

                    for (UISpace lSpace : mUISpaces) {
                        lSpace.setIsCurrentSpace(lSpace.getSpace().getId() == mSpace.getId());
                    }

                    mSpaceSideMenuListAdapter.notifyDataSetChanged();

                    if (mCreateLevel && mSpace.getProfile() == null && !mSpace.getName().equals(getResources().getString(R.string.application_default))) {
                        mCreateLevel = false;
                        Intent intent = new Intent();
                        intent.putExtra(Intents.INTENT_LAST_LEVEL_NAME, lastLevelName);
                        intent.setClass(this, AddProfileActivity.class);
                        startActivity(intent);
                    }

                    boolean isReferrerChecked = mCheckReferer && mSharedPreferences.getBoolean(CHECKED_REFERRER, false);

                    if (!isReferrerChecked) {
                        checkReferrer();
                    }

                    boolean postNotificationEnable = true;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        postNotificationEnable = checkPermissionsWithoutRequest(new Permission[]{Permission.POST_NOTIFICATIONS});
                    }

                    if (getTwinmeApplication().showEnableNotificationScreen() && mProfile != null && (!NotificationManagerCompat.from(this).areNotificationsEnabled() || !postNotificationEnable)){
                        showEnableNotifications();
                    }
                }));
    }

    @Override
    public void onGetProfileNotFound() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetProfileNotFound");
        }

        getTwinmeApplication().setFirstInstallation();
    }

    @Override
    public void onUpdatePendingNotifications(boolean hasPendingNotification) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onUpdatePendingNotifications: hasPendingNotification=" + hasPendingNotification);
        }

        mHasPendingNotification = hasPendingNotification;
        updateBottomNavigationColor();
        mMainService.findSpacesNotifications();
    }

    @Override
    public void onGetSpacesNotifications(@NonNull Map<Space, NotificationService.NotificationStat> spacesNotifications) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetSpacesNotifications: spacesNotifications=" + spacesNotifications);
        }

        for (UISpace space : mUISpaces) {
            NotificationService.NotificationStat stat = spacesNotifications.get(space.getSpace());
            space.setHasNotification(stat != null && stat.getPendingCount() > 0);
        }

        mSpaceSideMenuListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onGetTransferCall(@NonNull CallReceiver callReceiver) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onGetTransferCall: " + callReceiver);
        }

        mTransferCallId = callReceiver.getId();
    }

    @Override
    public void onDeleteTransferCall(@NonNull UUID callReceiverId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onDeleteTransferCall: " + callReceiverId);
        }

        if (callReceiverId.equals(mTransferCallId)) {
            mTransferCallId = null;
        }
    }

    @Override
    public void onRequestPermissions(@NonNull Permission[] grantedPermissions) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onRequestPermissions grantedPermissions=" + Arrays.toString(grantedPermissions));
        }

        if (mCurrentFragment != null) {
            TabbarFragment fragment = mCurrentFragment.get();
            if (fragment != null) {
                fragment.onRequestPermissions(grantedPermissions);
            }
        }
    }

    @Override
    public void onSubscribeSuccess() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubscribeSuccess");
        }

        mDrawerListView.invalidateViews();
    }

    @Override
    public void onSubscribeFailed(@NonNull ErrorCode errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSubscribeFailed " + errorCode);
        }

    }

    @Override
    public void onSetupFinishedInError(int errorCode) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onSetupFinishedInError " + errorCode);
        }
    }

    @Override
    public void updateColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateColor");
        }

        super.updateColor();

        Design.setTheme(this, getTwinmeApplication());

        if (!mShowWhatsNew && mUpdateStatusColor) {
            setStatusBarColor(Design.WHITE_COLOR);
        }

        setBackgroundColor(Design.WHITE_COLOR);

        mBottomNavigationView.setBackgroundColor(Design.WHITE_COLOR);
        if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_calls) {
            setToolBar(R.id.twinme_navigation_call_tool_bar);
        } else {
            setToolBar(R.id.twinme_navigation_tool_bar);
        }

        updateBottomNavigationColor();

        mNotificationView.setBorder(2.0f, Design.WHITE_COLOR);
        mNotificationView.setColor(Design.DELETE_COLOR_RED);

        mCallsNotificationView.setBorder(2.0f, Design.WHITE_COLOR);
        mCallsNotificationView.setColor(Design.DELETE_COLOR_RED);

        mConversationsNotificationView.setBorder(2.0f, Design.WHITE_COLOR);
        mConversationsNotificationView.setColor(Design.DELETE_COLOR_RED);

        mFragmentFrameLayout.setBackgroundColor(Design.WHITE_COLOR);
        mDrawerListView.setBackgroundColor(Design.WHITE_COLOR);
        mDrawerListView.invalidateViews();

        mSpaceDrawerListView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mSpaceDrawerListView.invalidate();

        mCreateSpaceView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);
        mCreateSpaceImageView.setColorFilter(Design.BLACK_COLOR);

        mSpaceSideMenuListAdapter.notifyDataSetChanged();
    }

    public int getSpacesListHeight() {
        if (DEBUG) {
            Log.d(LOG_TAG, "getSpacesListHeight");
        }

        return mSpaceDrawerListView.getHeight();
    }

    @SuppressLint({"NonConstantResourceId", "ClickableViewAccessibility"})
    private void initViews() {
        if (DEBUG) {
            Log.d(LOG_TAG, "initViews");
        }

        Design.setTheme(this, getTwinmeApplication());

        setContentView(R.layout.main_activity);
        setStatusBarColor(Design.WHITE_COLOR);
        setToolBar(R.id.twinme_navigation_tool_bar);
        showToolBar(false);
        setObserver(this);
        applyInsets(R.id.main_activity_content_layout, -1, R.id.twinme_navigation_bottom_navigation, Design.TOOLBAR_COLOR, false);

        mDrawerLayout = findViewById(R.id.main_activity_drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

                mUISpacesLinearLayoutManager.scrollToPositionWithOffset(0, (int) (-DESIGN_SPACE_ITEM_HEIGHT * Design.HEIGHT_RATIO));

                ViewGroup.LayoutParams layoutParams = mDrawerListView.getLayoutParams();
                if (!mUISpaces.isEmpty()) {
                    layoutParams.width = (int) (DESIGN_SIDE_MENU_SETTINGS_WIDTH * Design.WIDTH_RATIO);
                    mLeftMenuView.setVisibility(View.VISIBLE);
                } else {
                    layoutParams.width = (int) (DESIGN_SIDE_MENU_WIDTH * Design.WIDTH_RATIO);
                    mLeftMenuView.setVisibility(View.GONE);
                }

                updateSideMenu();
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }
        });

        mDrawerContainer = findViewById(R.id.main_activity_list_container);
        mDrawerContainer.setBackgroundColor(Design.WHITE_COLOR);

        ViewGroup.LayoutParams layoutParams = mDrawerContainer.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SIDE_MENU_WIDTH * Design.WIDTH_RATIO);

        mDrawerListView = findViewById(R.id.main_activity_drawer_list_view);
        mDrawerListView.setBackgroundColor(Design.WHITE_COLOR);

        layoutParams = mDrawerListView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SIDE_SETTINGS_WIDTH * Design.WIDTH_RATIO);

        mLeftMenuView = findViewById(R.id.main_activity_left_view);
        layoutParams = mLeftMenuView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_SIDE_SPACES_WIDTH * Design.WIDTH_RATIO);

        mCreateSpaceView = findViewById(R.id.main_activity_create_space_view);
        mCreateSpaceView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        layoutParams = mCreateSpaceView.getLayoutParams();
        layoutParams.height = (int) (DESIGN_SPACE_ITEM_HEIGHT * Design.HEIGHT_RATIO);

        mCreateSpaceView.setOnClickListener(v -> onCreateSpaceClick());

        mCreateSpaceImageView = findViewById(R.id.main_activity_create_space_image_view);
        mCreateSpaceImageView.setColorFilter(Design.BLACK_COLOR);

        layoutParams = mCreateSpaceImageView.getLayoutParams();
        layoutParams.width = (int) (DESIGN_CREATE_SPACE_ICON_SIZE * Design.WIDTH_RATIO);
        layoutParams.height = (int) (DESIGN_CREATE_SPACE_ICON_SIZE * Design.HEIGHT_RATIO);

        mSpaceDrawerListView = findViewById(R.id.main_activity_drawer_spaces_list_view);
        layoutParams = mSpaceDrawerListView.getLayoutParams();
        layoutParams.height = Design.DISPLAY_HEIGHT -  ((int) (DESIGN_SPACE_ITEM_HEIGHT * Design.HEIGHT_RATIO) * 2);

        mUISpacesLinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        mSpaceDrawerListView.setLayoutManager(mUISpacesLinearLayoutManager);
        mSpaceDrawerListView.setItemAnimator(null);
        mSpaceDrawerListView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        mContentFrameLayout = findViewById(R.id.main_activity_content_layout);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.application_name, R.string.application_name) {
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                float moveFactor = (mDrawerContainer.getWidth() * slideOffset);
                if (CommonUtils.isLayoutDirectionRTL()) {
                    mContentFrameLayout.setTranslationX(-moveFactor);
                } else {
                    mContentFrameLayout.setTranslationX(moveFactor);
                }
            }
        };
        mDrawerLayout.addDrawerListener(drawerToggle);

        SideMenuListAdapter.OnMenuClickListener onMenuClickListener = this::onMenuItemClick;

        mSideMenuListAdapter = new SideMenuListAdapter(this, onMenuClickListener);
        mDrawerListView.setAdapter(mSideMenuListAdapter);

        SpacesSideMenuListAdapter.OnSpaceClickListener onSpaceClickListener = new SpacesSideMenuListAdapter.OnSpaceClickListener() {
            @Override
            public void onSpaceClick(int position) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mMainService.setSpace(mUISpaces.get(position).getId());
            }

            @Override
            public void onSecretSpaceClick() {
                enterSecretSpace();
            }

            @Override
            public void onSpaceLongClick(int position) {

                mDrawerLayout.closeDrawer(GravityCompat.START);
                onShowSpaceClick(mUISpaces.get(position).getId());
            }
        };

        mSpaceSideMenuListAdapter = new SpacesSideMenuListAdapter(this, (int) (DESIGN_SPACE_ITEM_HEIGHT * Design.HEIGHT_RATIO), mUISpaces, onSpaceClickListener);
        mSpaceDrawerListView.setAdapter(mSpaceSideMenuListAdapter);

        View twinmeToolbar = findViewById(R.id.twinme_navigation_tool_bar);
        View callToolbar = findViewById(R.id.twinme_navigation_call_tool_bar);
        View conversationsToolbar = findViewById(R.id.twinme_navigation_conversations_tool_bar);

        SkredboardGestureListener.OnSkredboardGestureListener onSkredboardGestureListener = new SkredboardGestureListener.OnSkredboardGestureListener() {
            @Override
            public void onOpenSkredboard() {
                mSkredBoardFragment.openSkredBoard();
            }

            @Override
            public void onCloseSkredboard() {
                mSkredBoardFragment.closeSkredBoard();
            }

            @Override
            public void onSingleTap() {}
        };

        SkredboardGestureListener toolbarGestureListener = new SkredboardGestureListener(this, onSkredboardGestureListener);

        GestureDetector toolbarGestureDetector = new GestureDetector(this, toolbarGestureListener);
        twinmeToolbar.setOnTouchListener((v, event) -> toolbarGestureDetector.onTouchEvent(event));

        GestureDetector callToolbarGestureDetector = new GestureDetector(this, toolbarGestureListener);
        callToolbar.setOnTouchListener((v, event) -> callToolbarGestureDetector.onTouchEvent(event));

        GestureDetector conversationsToolbarGestureDetector = new GestureDetector(this, toolbarGestureListener);
        conversationsToolbar.setOnTouchListener((v, event) -> conversationsToolbarGestureDetector.onTouchEvent(event));

        mAvatarView = findViewById(R.id.toolbar_image);
        mAvatarView.setImage(this, null,
                new CircularImageDescriptor(getDefaultAvatar(), 0.5f, 0.5f, 0.5f));
        mAvatarView.setOnClickListener(view -> openDrawerLayout());

        mNotificationView = findViewById(R.id.toolbar_notification_rounded_view);
        mNotificationView.setBorder(2.0f, Design.WHITE_COLOR);
        mNotificationView.setColor(Design.DELETE_COLOR_RED);

        mCallsAvatarView = findViewById(R.id.calls_toolbar_image);
        mCallsAvatarView.setImage(this, null,
                new CircularImageDescriptor(getDefaultAvatar(), 0.5f, 0.5f, 0.5f));
        mCallsAvatarView.setOnClickListener(view -> openDrawerLayout());

        mCallsNotificationView = findViewById(R.id.calls_toolbar_notification_rounded_view);
        mCallsNotificationView.setBorder(2.0f, Design.WHITE_COLOR);
        mCallsNotificationView.setColor(Design.DELETE_COLOR_RED);

        mConversationsAvatarView = findViewById(R.id.conversations_toolbar_image);
        mConversationsAvatarView.setImage(this, null,
                new CircularImageDescriptor(getDefaultAvatar(), 0.5f, 0.5f, 0.5f));
        mConversationsAvatarView.setOnClickListener(view -> openDrawerLayout());

        mConversationsNotificationView = findViewById(R.id.conversations_toolbar_notification_rounded_view);
        mConversationsNotificationView.setBorder(2.0f, Design.WHITE_COLOR);
        mConversationsNotificationView.setColor(Design.DELETE_COLOR_RED);

        mFragmentFrameLayout = findViewById(R.id.twinme_navigation_frame_layout);
        mFragmentFrameLayout.setBackgroundColor(Design.WHITE_COLOR);

        mBottomNavigationShadowView = findViewById(R.id.twinme_navigation_bottom_navigation_shadow_view);

        mBottomNavigationView = findViewById(R.id.twinme_navigation_bottom_navigation);
        mBottomNavigationView.inflateMenu(R.menu.tabbar_menu);
        mBottomNavigationView.setItemIconTintList(null);
        mBottomNavigationView.setBackgroundColor(Design.WHITE_COLOR);

        mFragmentFrameLayout.setVisibility(View.INVISIBLE);
        mBottomNavigationView.setVisibility(View.INVISIBLE);
        mBottomNavigationShadowView.setVisibility(View.INVISIBLE);

        mBottomNavigationView.setOnItemSelectedListener(item -> {

            // Ignore if we are paused because showFragment will raise an IllegalStateException.
            if (mIsOnPause) {
                return false;
            }

            hapticFeedback();

            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            if (!mShowWhatsNew && mUpdateStatusColor) {
                setStatusBarColor();
            }

            final int currentTab = item.getItemId();
            if (currentTab == R.id.navigation_spaces) {
                setToolBar(R.id.twinme_navigation_tool_bar);
                setTitle(capitalizedTitle(getString(R.string.spaces_activity_title)));

                twinmeToolbar.setVisibility(View.VISIBLE);
                callToolbar.setVisibility(View.GONE);
                conversationsToolbar.setVisibility(View.GONE);
                if (getTwinmeApplication().hasNewVersion()) {
                    mNotificationView.setVisibility(View.VISIBLE);
                } else {
                    mNotificationView.setVisibility(View.GONE);
                }

                SpacesFragment spaceFragment = new SpacesFragment();
                showFragment(spaceFragment, SPACES_FRAGMENT_TAG);

                showSpaceOnboarding();
            } else if (currentTab == R.id.navigation_calls) {
                setTitle(capitalizedTitle(getString(R.string.calls_fragment_title)));
                setToolBar(R.id.twinme_navigation_call_tool_bar);
                twinmeToolbar.setVisibility(View.GONE);
                callToolbar.setVisibility(View.VISIBLE);
                conversationsToolbar.setVisibility(View.GONE);
                if (getTwinmeApplication().hasNewVersion()) {
                    mCallsNotificationView.setVisibility(View.VISIBLE);
                } else {
                    mCallsNotificationView.setVisibility(View.GONE);
                }

                CallsFragment callsFragment = new CallsFragment();
                showFragment(callsFragment, CALLS_FRAGMENT_TAG);

            } else if (currentTab == R.id.navigation_contacts) {
                setTitle(capitalizedTitle(getString(R.string.contacts_fragment_title)));
                setToolBar(R.id.twinme_navigation_tool_bar);
                twinmeToolbar.setVisibility(View.VISIBLE);
                callToolbar.setVisibility(View.GONE);
                conversationsToolbar.setVisibility(View.GONE);
                if (getTwinmeApplication().hasNewVersion()) {
                    mNotificationView.setVisibility(View.VISIBLE);
                } else {
                    mNotificationView.setVisibility(View.GONE);
                }
                ContactsFragment contactsFragment = new ContactsFragment();
                showFragment(contactsFragment, CONTACTS_FRAGMENT_TAG);

            } else if (currentTab == R.id.navigation_chat) {
                setTitle(capitalizedTitle(getString(R.string.conversations_fragment_title)));
                setToolBar(R.id.twinme_navigation_conversations_tool_bar);
                twinmeToolbar.setVisibility(View.GONE);
                callToolbar.setVisibility(View.GONE);
                conversationsToolbar.setVisibility(View.VISIBLE);
                if (getTwinmeApplication().hasNewVersion()) {
                    mConversationsNotificationView.setVisibility(View.VISIBLE);
                } else {
                    mConversationsNotificationView.setVisibility(View.GONE);
                }
                ConversationsFragment conversationsFragment = new ConversationsFragment();
                showFragment(conversationsFragment, CHAT_FRAGMENT_TAG);

            } else if (currentTab == R.id.navigation_notifications) {
                setTitle(capitalizedTitle(getString(R.string.notifications_fragment_title)));
                setToolBar(R.id.twinme_navigation_tool_bar);
                twinmeToolbar.setVisibility(View.VISIBLE);
                callToolbar.setVisibility(View.GONE);
                conversationsToolbar.setVisibility(View.GONE);
                if (getTwinmeApplication().hasNewVersion()) {
                    mNotificationView.setVisibility(View.VISIBLE);
                } else {
                    mNotificationView.setVisibility(View.GONE);
                }
                NotificationsFragment notificationsFragment = new NotificationsFragment();
                showFragment(notificationsFragment, NOTIFICATIONS_FRAGMENT_TAG);
            }

            applyInsets(R.id.main_activity_content_layout, -1, R.id.twinme_navigation_bottom_navigation, Design.TOOLBAR_COLOR, false);

            return true;
        });

        mOverlayView = findViewById(R.id.twinme_navigation_overlay_view);

        if (mSavedInstanceState != null) {
            mSkredBoardFragment = (SkredBoardFragment) getSupportFragmentManager().getFragment(mSavedInstanceState, SKREDBOARD_FRAGMENT);
        }
        if (mSkredBoardFragment == null) {
            mSkredBoardFragment = SkredBoardFragment.newInstance();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.main_activity_skred_board, mSkredBoardFragment).commitAllowingStateLoss();
            getSupportFragmentManager().executePendingTransactions();
        }
        mSkredBoardFragment.setDelegate(this);

        mCoachMarkView = findViewById(R.id.main_activity_coach_mark_view);
        CoachMarkView.OnCoachMarkViewListener onCoachMarkViewListener = new CoachMarkView.OnCoachMarkViewListener() {
            @Override
            public void onCloseCoachMark() {

                mCoachMarkView.setVisibility(View.GONE);
            }

            @Override
            public void onTapCoachMarkFeature() {

                mCoachMarkView.setVisibility(View.GONE);
                getTwinmeApplication().hideCoachMark(CoachMark.CoachMarkTag.PRIVACY);
                onCoachMarkFeatureClick();
            }

            @Override
            public void onLongPressCoachMarkFeature() {

            }
        };

        mCoachMarkView.setOnCoachMarkViewListener(onCoachMarkViewListener);
    }

    private void showFragment(@NonNull TabbarFragment fragment, @NonNull String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.twinme_navigation_frame_layout, fragment, tag)
                .commit();

        // Keep a weak reference to the active fragment to forward the request permissions.
        mCurrentFragment = new WeakReference<>(fragment);
    }

    private String capitalizedTitle(String title) {
        if (DEBUG) {
            Log.d(LOG_TAG, "capitalizedTitle: title=" + title);
        }

        String capitalizedTitle = "";
        if (title != null && !title.trim().isEmpty()) {
            capitalizedTitle = title.substring(0, 1).toUpperCase() + title.substring(1);
        }
        return capitalizedTitle;
    }

    private void onMenuItemClick(MenuItem menuItem) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onMenuItemClick: menuItem=" + menuItem);
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        Intent intent = new Intent();

        switch (menuItem.getAction()) {
            case NO_ACTION:
                break;

            case HELP:
                startActivity(HelpActivity.class);
                break;

            case PERSONALIZATION:
                intent.setClass(this, PersonalizationActivity.class);
                if (mSpace != null) {
                    intent.putExtra(Intents.INTENT_SPACE_ID, mSpace.getId().toString());
                }
                startActivity(intent);
                break;

            case SOUND_SETTINGS:
                startActivity(SoundsSettingsActivity.class);
                break;

            case MESSAGE_SETTINGS:
                intent.setClass(this, MessagesSettingsActivity.class);
                if (mSpace != null) {
                    intent.putExtra(Intents.INTENT_SPACE_ID, mSpace.getId().toString());
                }
                startActivity(intent);
                break;

            case PRIVACY:
                startActivity(PrivacyActivity.class);
                break;

            case SUBSCRIBE:
                intent.setClass(this, InAppSubscriptionActivity.class);
                startActivity(intent);
                break;

            case TRANSFER_CALL:
                if (!isFeatureSubscribed(org.twinlife.twinme.TwinmeApplication.Feature.GROUP_CALL)) {
                    showOnboardingView();
                } else if (mTransferCallId != null) {
                    intent.putExtra(Intents.INTENT_CALL_RECEIVER_ID, mTransferCallId.toString());
                    intent.setClass(this, TransferCallActivity.class);
                    startActivity(intent);
                } else {
                    intent.putExtra(Intents.INTENT_TRANSFER_CALL, true);
                    intent.setClass(this, CreateExternalCallActivity.class);
                    startActivity(intent);
                }
                break;

            case PROFILE:
                if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_spaces) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                    break;
                }

                if (mProfile != null) {
                    intent.putExtra(Intents.INTENT_PROFILE_ID, mProfile.getId().toString());
                    intent.setClass(this, EditProfileActivity.class);
                } else {
                    intent.putExtra(Intents.INTENT_FIRST_PROFILE, true);
                    intent.setClass(this, AddProfileActivity.class);
                }
                startActivity(intent);
                break;

            case SETTINGS_ADVANCED:
                startActivity(SettingsAdvancedActivity.class);
                break;

            case ABOUT_TWINME:
                startActivity(AboutActivity.class);
                break;

            case ACCOUNT:
                startActivity(AccountActivity.class);
                break;

            case SIGN_OUT:
                getTwinmeContext().getAccountService().signOut();

                getTwinmeApplication().stop();
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra(Intents.INTENT_SHOW_SPLASHSCREEN, false);
                intent.setClass(this, MainActivity.class);
                startActivity(intent);
                break;
        }
    }

    public void onCreateSpaceClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCreateSpaceClick");
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        if (getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.SPACE)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra(Intents.INTENT_SHOW_FIRST_PART_ONBOARDING, mSpace != null);
            intent.setClass(this, OnboardingSpaceActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setClass(this, TemplateSpaceActivity.class);
            startActivity(intent);
        }
    }

    private void showSpaceOnboarding() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showSpaceOnboarding");
        }

        if (mSpace != null && getTwinmeApplication().showSpaceDescription()) {
            getTwinmeApplication().hideSpaceDescription();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra(Intents.INTENT_SHOW_CREATE_SPACE, false);
            intent.setClass(this, OnboardingSpaceActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            getTwinmeApplication().hideSpaceDescription();
        }
    }

    private void onShowSpaceClick(UUID spaceId) {
        if (DEBUG) {
            Log.d(LOG_TAG, "onShowSpaceClick: " + spaceId);
        }

        Intent intent = new Intent();
        intent.setClass(this, ShowSpaceActivity.class);
        intent.putExtra(Intents.INTENT_SPACE_ID, spaceId.toString());
        startActivity(intent);
    }

    private void updateBottomNavigationColor() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateBottomNavigationColor");
        }

        int mainColor = Design.getMainStyle();
        int itemColor = getResources().getColor(R.color.bottom_navigation_item_color);
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        mainColor,
                        itemColor
                }
        );

        if (mBottomNavigationView.getMenu().size() == TAB_COUNT) {
            Drawable profileDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tab_bar_spaces_grey, null);
            if (profileDrawable != null) {
                profileDrawable.setTintList(colorStateList);
            }
            mBottomNavigationView.getMenu().getItem(PROFILE_FRAGMENT_INDEX).setIcon(profileDrawable);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBottomNavigationView.getMenu().getItem(PROFILE_FRAGMENT_INDEX).setContentDescription(getString(R.string.application_profile));
            }

            Drawable callDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tab_bar_call_grey, null);
            if (callDrawable != null) {
                callDrawable.setTintList(colorStateList);
            }
            mBottomNavigationView.getMenu().getItem(CALLS_FRAGMENT_INDEX).setIcon(callDrawable);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBottomNavigationView.getMenu().getItem(CALLS_FRAGMENT_INDEX).setContentDescription(getString(R.string.calls_fragment_title));
            }

            Drawable contactDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tab_bar_contacts_grey, null);
            if (contactDrawable != null) {
                contactDrawable.setTintList(colorStateList);
            }
            mBottomNavigationView.getMenu().getItem(CONTACTS_FRAGMENT_INDEX).setIcon(contactDrawable);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBottomNavigationView.getMenu().getItem(CONTACTS_FRAGMENT_INDEX).setContentDescription(getString(R.string.contacts_fragment_title));
            }

            Drawable chatDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tab_bar_chat_grey, null);
            if (chatDrawable != null) {
                chatDrawable.setTintList(colorStateList);
            }
            mBottomNavigationView.getMenu().getItem(CHAT_FRAGMENT_INDEX).setIcon(chatDrawable);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBottomNavigationView.getMenu().getItem(CHAT_FRAGMENT_INDEX).setContentDescription(getString(R.string.conversations_fragment_title));
            }

            if (mHasPendingNotification) {
                Drawable badgeDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tab_bar_notification_badge, null);
                Drawable notificationDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tab_bar_notification, null);
                if (notificationDrawable != null) {
                    notificationDrawable.setTintList(colorStateList);
                }

                Drawable[] layers = {notificationDrawable, badgeDrawable};
                LayerDrawable layerDrawable = new LayerDrawable(layers);
                mBottomNavigationView.getMenu().getItem(NOTIFICATIONS_FRAGMENT_INDEX).setIcon(layerDrawable);
            } else {
                Drawable notificationDrawable = ResourcesCompat.getDrawable(getResources(), R.drawable.tab_bar_notification_grey, null);
                if (notificationDrawable != null) {
                    notificationDrawable.setTintList(colorStateList);
                }
                mBottomNavigationView.getMenu().getItem(NOTIFICATIONS_FRAGMENT_INDEX).setIcon(notificationDrawable);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBottomNavigationView.getMenu().getItem(NOTIFICATIONS_FRAGMENT_INDEX).setContentDescription(getString(R.string.notifications_fragment_title));
            }
        }
    }

    private void enterSecretSpace() {
        if (DEBUG) {
            Log.d(LOG_TAG, "enterSecretSpace");
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        Intent intent = new Intent();
        intent.setClass(this, SecretSpaceActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void updateSideMenu() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateSideMenu");
        }

        mLeftMenuView.setBackgroundColor(Design.LIGHT_GREY_BACKGROUND_COLOR);

        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) mDrawerListView.getLayoutParams();
        marginLayoutParams.topMargin = getBarTopInset();
        marginLayoutParams.bottomMargin = getBarBottomInset();

        marginLayoutParams = (ViewGroup.MarginLayoutParams) mSpaceDrawerListView.getLayoutParams();
        marginLayoutParams.topMargin = getBarTopInset();
        marginLayoutParams.bottomMargin = getBarBottomInset();
    }

    private void updateFragment() {
        if (DEBUG) {
            Log.d(LOG_TAG, "updateFragment");
        }

        if (!mShowWhatsNew && mUpdateStatusColor) {
            setStatusBarColor();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);

        if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_calls) {
            setToolBar(R.id.twinme_navigation_call_tool_bar);
        } else if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_chat) {
            setToolBar(R.id.twinme_navigation_conversations_tool_bar);
        } else if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_spaces) {
            setToolBar(R.id.twinme_navigation_tool_bar);
            SpacesFragment spaceFragment = (SpacesFragment) fragmentManager.findFragmentByTag(SPACES_FRAGMENT_TAG);
            if (spaceFragment != null) {
                spaceFragment.updateColor();
            }
        } else {
            setToolBar(R.id.twinme_navigation_tool_bar);
            if (mBottomNavigationView.getSelectedItemId() == R.id.navigation_contacts) {
                ContactsFragment contactsFragment = (ContactsFragment) fragmentManager.findFragmentByTag(CONTACTS_FRAGMENT_TAG);
                if (contactsFragment != null) {
                    contactsFragment.updateColor();
                }
            }
        }

        applyInsets(R.id.main_activity_content_layout, -1, R.id.twinme_navigation_bottom_navigation, Design.TOOLBAR_COLOR, false);
    }

    private void showCoachMark() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showCoachMark");
        }

        if (getTwinmeApplication().showCoachMark(CoachMark.CoachMarkTag.PRIVACY)) {
            if (mDrawerListView.getCount() > 5) {
                View view = mDrawerListView.getChildAt(5);

                mCoachMarkView.postDelayed(() -> {
                    mCoachMarkView.setVisibility(View.VISIBLE);
                    CoachMark coachMark = new CoachMark(getString(R.string.privacy_activity_coach_mark), CoachMark.CoachMarkTag.PRIVACY, true, true, new Point((int) mDrawerListView.getX(), (int) view.getY()), view.getWidth(), view.getHeight(), 0);
                    mCoachMarkView.openCoachMark(coachMark);
                }, COACH_MARK_DELAY);
            }
        }
    }

    private void onCoachMarkFeatureClick() {
        if (DEBUG) {
            Log.d(LOG_TAG, "onCoachMarkFeatureClick");
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);

        if (mCoachMarkView.getCoachMark().getCoachMarkTag() == CoachMark.CoachMarkTag.PRIVACY) {
            Intent intent = new Intent();
            intent.setClass(this, PrivacyActivity.class);
            startActivity(intent);
        } else {
            onCreateSpaceClick();
        }
    }

    private void showSuccessAuthentification(String name, Bitmap avatar) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showSuccessAuthentification");
        }

        SuccessAuthentifiedRelationView successAuthentifiedRelationView = findViewById(R.id.main_activity_success_authentification_view);
        successAuthentifiedRelationView.setInfo(this,name, avatar);
        successAuthentifiedRelationView.setSuccessAuthentifiedRelationListener(() -> {
            successAuthentifiedRelationView.setVisibility(View.GONE);
            setStatusBarColor();
        });

        successAuthentifiedRelationView.setVisibility(View.VISIBLE);
        successAuthentifiedRelationView.bringToFront();
        openMenuColor();
    }

    private void showEnableNotifications() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showEnableNotifications");
        }

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        defaultConfirmView.setLayoutParams(layoutParams);
        defaultConfirmView.setTitle(getString(R.string.quality_of_service_activity_settings));
        defaultConfirmView.setMessage(getString(R.string.quality_of_service_activity_enable_notifications_warning));

        boolean darkMode = false;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        int displayMode = Settings.displayMode.getInt();
        if ((currentNightMode == Configuration.UI_MODE_NIGHT_YES && displayMode == DisplayMode.SYSTEM.ordinal())  || displayMode == DisplayMode.DARK.ordinal()) {
            darkMode = true;
        }

        defaultConfirmView.setImage(ResourcesCompat.getDrawable(getResources(), darkMode ? R.drawable.enable_notification_dark : R.drawable.enable_notication, null));
        defaultConfirmView.setConfirmTitle(getString(R.string.quality_of_service_activity_enable_notifications));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                mDrawerLayout.removeView(defaultConfirmView);
                setStatusBarColor();

                if (fromConfirmAction) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        final AndroidDeviceInfo androidDeviceInfo = new AndroidDeviceInfo(getApplicationContext());
                        Intent intent = null;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && androidDeviceInfo.isNetworkRestricted()) {
                            intent = new Intent(android.provider.Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS);
                            intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        } else if (!androidDeviceInfo.isIgnoringBatteryOptimizations()) {
                            intent = new Intent();
                            intent.setAction(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        }

                        if (intent != null) {
                            startActivity(intent);
                        }
                    }
                }
            }
        };

        defaultConfirmView.setObserver(observer);
        mDrawerLayout.addView(defaultConfirmView);
        defaultConfirmView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void showWhatsNew() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showWhatsNew");
        }

        if (!mShowWhatsNew && (mSpace != null && getTwinmeApplication().showWhatsNew() && getTwinmeApplication().getLastVersion() != null)) {
            mShowWhatsNew = true;

            WhatsNewDialog whatsNewDialog = new WhatsNewDialog(this, false);
            DialogInterface.OnCancelListener dialogCancelListener = dialog -> {};
            DialogInterface.OnDismissListener dismissListener = dialogInterface -> {
                mShowWhatsNew = false;
                setStatusBarColor();
            };
            whatsNewDialog.setOnCancelListener(dialogCancelListener);
            whatsNewDialog.setOnDismissListener(dismissListener);
            whatsNewDialog.setup(getTwinmeApplication().getLastVersion(), () -> {
                whatsNewDialog.dismiss();
                mShowWhatsNew = false;
                setStatusBarColor();
            }, false);
            whatsNewDialog.show();

            Window window = getWindow();
            window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        }
    }

    private void addProxy(String proxy) {
        if (DEBUG) {
            Log.d(LOG_TAG, "addProxy: proxy= " + proxy);
        }

        mUpdateStatusColor = false;

        final List<ProxyDescriptor> proxies = getTwinmeContext().getConnectivityService().getUserProxies();
        if (proxies.size() >= ConnectivityService.MAX_PROXIES) {
            showAlertMessage(String.format(getString(R.string.proxy_activity_limit), ConnectivityService.MAX_PROXIES));
            return;
        }

        for (ProxyDescriptor proxyDescriptor : proxies) {
            if (proxyDescriptor.getDescriptor().equalsIgnoreCase(proxy)) {
                showAlertMessage(getString(R.string.proxy_activity_already_use));
                return;
            }
        }

        DefaultConfirmView defaultConfirmView = new DefaultConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        defaultConfirmView.setLayoutParams(layoutParams);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(getString(R.string.proxy_activity_title));
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_DEFAULT), 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("\n\n");
        int startSubTitle = spannableStringBuilder.length();
        spannableStringBuilder.append(proxy);
        spannableStringBuilder.setSpan(new ForegroundColorSpan(Design.FONT_COLOR_GREY), startSubTitle, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        defaultConfirmView.setSpannableTitle(spannableStringBuilder);
        defaultConfirmView.setMessage(getString(R.string.proxy_activity_url));
        defaultConfirmView.setImage(ResourcesCompat.getDrawable(getResources(),  R.drawable.onboarding_proxy, null));
        defaultConfirmView.setConfirmTitle(getString(R.string.proxy_activity_enable));
        defaultConfirmView.setCancelTitle(getString(R.string.application_cancel));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                defaultConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                mDrawerLayout.removeView(defaultConfirmView);
                setStatusBarColor();
                mUpdateStatusColor = true;

                if (fromConfirmAction) {
                    SNIProxyDescriptor proxyDescriptor = SNIProxyDescriptor.create(proxy);
                    if (proxyDescriptor == null) {
                        showAlertMessage(getString(R.string.proxy_activity_invalid_format));
                        return;
                    }
                    proxies.add(proxyDescriptor);
                    getTwinmeContext().getConnectivityService().setUserProxies(proxies);
                    startActivity(SettingsAdvancedActivity.class);
                }
            }
        };

        defaultConfirmView.setObserver(observer);
        mDrawerLayout.addView(defaultConfirmView);
        defaultConfirmView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void showAlertMessage(String message) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showAlertMessage");
        }

        mUpdateStatusColor = false;

        AlertMessageView alertMessageView = new AlertMessageView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        alertMessageView.setLayoutParams(layoutParams);
        alertMessageView.setMessage(message);

        AlertMessageView.Observer observer = new AlertMessageView.Observer() {

            @Override
            public void onConfirmClick() {
                alertMessageView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                alertMessageView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd() {
                mDrawerLayout.removeView(alertMessageView);
                mUpdateStatusColor = true;
                setStatusBarColor();
            }
        };
        alertMessageView.setObserver(observer);

        mDrawerLayout.addView(alertMessageView);
        alertMessageView.show();

        Window window = getWindow();
        window.setNavigationBarColor(Design.POPUP_BACKGROUND_COLOR);

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    public void showPremiumFeatureView(UIPremiumFeature.FeatureType featureType) {
        if (DEBUG) {
            Log.d(LOG_TAG, "showPremiumFeatureView: " + featureType);
        }

        PremiumFeatureConfirmView premiumFeatureConfirmView = new PremiumFeatureConfirmView(this, null);
        PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        premiumFeatureConfirmView.setLayoutParams(layoutParams);
        premiumFeatureConfirmView.initWithPremiumFeature(new UIPremiumFeature(this, featureType));

        AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
            @Override
            public void onConfirmClick() {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), InAppSubscriptionActivity.class);
                startActivity(intent);
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCancelClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onDismissClick() {
                premiumFeatureConfirmView.animationCloseConfirmView();
            }

            @Override
            public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                mDrawerLayout.removeView(premiumFeatureConfirmView);
                setStatusBarColor();
            }
        };
        premiumFeatureConfirmView.setObserver(observer);

        mDrawerLayout.addView(premiumFeatureConfirmView);
        premiumFeatureConfirmView.show();

        int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
        setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
    }

    private void showOnboardingView() {
        if (DEBUG) {
            Log.d(LOG_TAG, "showOnboardingView");
        }

        if (getTwinmeApplication().startOnboarding(TwinmeApplication.OnboardingType.TRANSFER_CALL)) {
            OnboardingDetailView onboardingDetailView = new OnboardingDetailView(this, null);
            PercentRelativeLayout.LayoutParams layoutParams = new PercentRelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            onboardingDetailView.setLayoutParams(layoutParams);

            UIPremiumFeature uiPremiumFeature = new UIPremiumFeature(this, UIPremiumFeature.FeatureType.TRANSFER_CALL);
            onboardingDetailView.setPremiumFeature(uiPremiumFeature);
            onboardingDetailView.setConfirmTitle(getString(R.string.application_ok));
            onboardingDetailView.setCancelTitle(getString(R.string.application_do_not_display));

            AbstractConfirmView.Observer observer = new AbstractConfirmView.Observer() {
                @Override
                public void onConfirmClick() {
                    onboardingDetailView.animationCloseConfirmView();
                }

                @Override
                public void onCancelClick() {
                    onboardingDetailView.animationCloseConfirmView();
                    getTwinmeApplication().setShowOnboardingType(TwinmeApplication.OnboardingType.TRANSFER_CALL, false);
                }

                @Override
                public void onDismissClick() {
                    onboardingDetailView.animationCloseConfirmView();
                }

                @Override
                public void onCloseViewAnimationEnd(boolean fromConfirmAction) {
                    mDrawerLayout.removeView(onboardingDetailView);
                    setStatusBarColor();

                    if (fromConfirmAction) {
                        showPremiumFeatureView(UIPremiumFeature.FeatureType.TRANSFER_CALL);
                    }
                }
            };
            onboardingDetailView.setObserver(observer);
            mDrawerLayout.addView(onboardingDetailView);
            onboardingDetailView.show();

            int color = ColorUtils.compositeColors(Design.OVERLAY_VIEW_COLOR, Design.TOOLBAR_COLOR);
            setStatusBarColor(color, Design.POPUP_BACKGROUND_COLOR);
        } else {
            showPremiumFeatureView(UIPremiumFeature.FeatureType.TRANSFER_CALL);
        }
    }
}
