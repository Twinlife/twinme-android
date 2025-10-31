/*
 *  Copyright (c) 2017-2021 twinlife SA & Telefun SAS.
 *  SPDX-License-Identifier: AGPL-3.0-only
 *
 *  Contributors:
 *   Thibaud David (contact@thibauddavid.com)
 *   Christian Jacquemot (Christian.Jacquemot@twinlife-systems.com)
 *   Stephane Carrez (Stephane.Carrez@twin.life)
 *   Fabrice Trescartes (Fabrice.Trescartes@twin.life)
 */

package org.twinlife.twinme.ui.mainActivity.skredBoard;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.twinlife.device.android.twinme.R;
import org.twinlife.twinme.skin.Design;

public class SkredBoardFragment extends Fragment {

    private static final float DESIGN_SKREDBOARD_VIEW_HEIGHT_PERCENT = 0.3733f;
    private static int SKREDBOARD_VIEW_HEIGHT;

    private DigitsAdapter mDigitAdapter;

    public enum SkredboardMode {

        SET_CODE, CREATE_CODE, DELETE_CODE,
    }

    private TextView skredBoardTitle;
    private ImageView createAccountButton;
    private Button deleteAccountButton;
    private Button validateButton;
    private RecyclerView recyclerView;

    private SkredboardMode skredBoardMode = SkredboardMode.SET_CODE;

    private SkredBoardFragmentDelegate delegate;

    private String code = "";

    public static SkredBoardFragment newInstance() {

        SkredBoardFragment skredBoardFragment = new SkredBoardFragment();
        Bundle bundle = new Bundle();
        skredBoardFragment.setArguments(bundle);
        return skredBoardFragment;
    }

    // Default constructor is required by Android for proper activity restoration.
    public SkredBoardFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.skredboard_fragment_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        setupDesign(view.getContext());

        skredBoardTitle = view.findViewById(R.id.skredBoardTitle);
        skredBoardTitle.setTextColor(Color.WHITE);

        TextView enterTextView = view.findViewById(R.id.skredBoardEnter);
        enterTextView.setTextColor(Color.WHITE);
        
        createAccountButton = view.findViewById(R.id.createAccountButton);
        deleteAccountButton = view.findViewById(R.id.deleteAccountButton);
        validateButton = view.findViewById(R.id.validateButton);
        recyclerView = view.findViewById(R.id.digitsGridView);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 5);

        mDigitAdapter = new DigitsAdapter(R.layout.digit_item, recyclerView);
        recyclerView.setAdapter(mDigitAdapter);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnLayoutChangeListener((v, i, i1, i2, i3, i4, i5, i6, i7) -> mDigitAdapter.notifyDataSetChanged());

        setListeners();

        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = SKREDBOARD_VIEW_HEIGHT;
        view.setY(-SKREDBOARD_VIEW_HEIGHT);
    }

    public void setDelegate(SkredBoardFragmentDelegate delegate) {

        this.delegate = delegate;
    }

    public void openSkredBoard() {

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(getView(), "translationY", 0);
        objectAnimator.setDuration(200);
        objectAnimator.start();
    }

    public void closeSkredBoard() {

        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(getView(), "translationY", -SKREDBOARD_VIEW_HEIGHT);
        objectAnimator.setDuration(200);
        objectAnimator.start();
    }

    private void setListeners() {

        RecyclerView.Adapter<RecyclerView.ViewHolder> adapter = recyclerView.getAdapter();
        if (adapter != null) {
            ((DigitsAdapter) adapter).setDigitClickListener(digit -> {
                String digitStr = String.valueOf(digit);
                if (code.contains(digitStr)) {
                    code = code.replace(digitStr, "");
                } else {
                    code += digitStr;
                }
                codeDidUpdate();
            });
        }

        createAccountButton.setOnClickListener(v -> updateSkredboardMode(SkredboardMode.CREATE_CODE));
        deleteAccountButton.setOnClickListener(v -> updateSkredboardMode(SkredboardMode.DELETE_CODE));

        validateButton.setOnClickListener(v -> {
            if (delegate != null && code != null) {
                delegate.skredboardDidValidateCode(skredBoardMode, code);
            }
            code = "";
            skredBoardMode = SkredboardMode.SET_CODE;
            codeDidUpdate();
            updateSkredboardMode(SkredboardMode.SET_CODE);
        });
    }

    private void updateSkredboardMode(@NonNull SkredboardMode skredBoardMode) {

        this.skredBoardMode = skredBoardMode;
        code = "";
        codeDidUpdate();
        switch (skredBoardMode) {
            case SET_CODE:
                deleteAccountButton.setVisibility(View.VISIBLE);
                createAccountButton.setVisibility(View.VISIBLE);
                skredBoardTitle.setText(R.string.main_activity_skredboard_access_title);
                break;
            case CREATE_CODE:
                deleteAccountButton.setVisibility(View.VISIBLE);
                createAccountButton.setVisibility(View.GONE);
                skredBoardTitle.setText(R.string.main_activity_skredboard_create_title);
                break;
            case DELETE_CODE:
                deleteAccountButton.setVisibility(View.GONE);
                createAccountButton.setVisibility(View.VISIBLE);
                skredBoardTitle.setText(R.string.main_activity_skredboard_delete_title);
                break;
        }
    }

    private void codeDidUpdate() {

        mDigitAdapter.codeDidUpdate(code);
    }

    private void setupDesign(Context context) {

        int displayHeight = 0;

        if (Design.DISPLAY_HEIGHT != 0) {
            displayHeight = Design.DISPLAY_HEIGHT;
        } else {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();

            final DisplayManager displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
            final Display defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY);

            if (metrics.heightPixels > metrics.widthPixels) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
                    displayHeight = bounds.height();
                } else {
                    if (defaultDisplay != null) {
                        Point size = new Point();
                        defaultDisplay.getRealSize(size);
                        displayHeight = size.y;

                    } else {
                        displayHeight = metrics.heightPixels;
                    }
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
                    Rect bounds = windowManager.getCurrentWindowMetrics().getBounds();
                    displayHeight = bounds.width();
                } else {
                    if (defaultDisplay != null) {
                        Point size = new Point();
                        defaultDisplay.getRealSize(size);
                        //noinspection SuspiciousNameCombination
                        displayHeight = size.x;
                    } else {
                        //noinspection SuspiciousNameCombination
                        displayHeight = metrics.widthPixels;
                    }
                }
            }
        }

        SKREDBOARD_VIEW_HEIGHT = (int) (DESIGN_SKREDBOARD_VIEW_HEIGHT_PERCENT * displayHeight);
    }
}
