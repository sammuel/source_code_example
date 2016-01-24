package com.photoapp.view.fragments;


import android.content.Context;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.photoapp.model.events.Bus;
import com.photoapp.controller.events.SignUpEvent;
import com.photoapp.R;
import com.photoapp.controller.keyboard.KeyBoardHelper;


public class SignUpFragment extends BaseFragment {

    private static final int RES_STRING_ID_EDIT_TEXT_USERNAME_ERROR = R.string.edit_text_username_error_sign_up;

    private Button buttonSignUp;
    private EditText editTextUserName;
    private boolean isSignedUp = false;

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        return fragment;
    }

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        InitButton(view);

        InitEditText(view);


    }

    private void InitEditText(View view) {
        editTextUserName = (EditText) view.findViewById(R.id.edit_text_username);
        editTextUserName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    showNextFragment();
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void InitButton(View view) {
        buttonSignUp = (Button) view.findViewById(R.id.button_sign_up);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNextFragment();
            }
        });
    }

    private void showNextFragment() {
        if (!isSignedUp) {
            boolean isEmpty = CheckIsEmpty(editTextUserName);
            if (isEmpty) {
                String str = getString(RES_STRING_ID_EDIT_TEXT_USERNAME_ERROR);
                editTextUserName.setError(str);

            } else {
                // Clear error message
                editTextUserName.setError(null);
                isSignedUp = true;
                String text = editTextUserName.getText().toString();
                Context context = getContext();
                KeyBoardHelper.hide(context, editTextUserName);

                Bus.post(new SignUpEvent(text));
            }
        }
    }

    private boolean CheckIsEmpty(EditText editTextUserName) {
        String text = editTextUserName.getText().toString();
        return TextUtils.isEmpty(text);
    }
}
