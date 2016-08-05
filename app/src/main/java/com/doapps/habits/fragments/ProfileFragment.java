package com.doapps.habits.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.doapps.habits.BuildConfig;
import com.doapps.habits.R;
import com.doapps.habits.activity.EditPhotoActivity;
import com.doapps.habits.activity.MainActivity;
import com.doapps.habits.helper.RoundedTransformation;
import com.doapps.habits.slider.swipeselector.PixelUtils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

public class ProfileFragment extends Fragment {
    private static final boolean[] editorOpened = new boolean[1];
    private static final String TAG = ProfileFragment.class.getName();

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        getActivity().findViewById(R.id.toolbar_shadow).setVisibility(View.GONE);
        // Inflate the layout for this fragment
        final View result = inflater.inflate(R.layout.percent_profile_fragment, container, false);
        final TextView name = (TextView) result.findViewById(R.id.name);
        final TextView email = (TextView) result.findViewById(R.id.email);
        final TextView location = (TextView) result.findViewById(R.id.location);
        final Button btnDelete = (Button) result.findViewById(R.id.btn_delete_user);
        final FloatingActionButton fab = (FloatingActionButton) result.findViewById(R.id.fab);

        fab.setOnClickListener(view -> {
            if (!editorOpened[0]) {
                getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.userInfo, new ProfileEditFragment())
                        .commit();
                editorOpened[0] = true;
            } else {
                Log.i("ProfileFragment", "fab set improperly");
            }
        });


        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {

            location.setText(getActivity()
                    .getSharedPreferences("pref", Context.MODE_PRIVATE)
                    .getString("location", ""));

            final ImageView avatar = (ImageView) result.findViewById(R.id.imageView3);
            if (user.getPhotoUrl() != null && !user.getPhotoUrl().toString().contains("facebook")) {
                final Button btnFacebook = (Button) result.findViewById(R.id.btn_connect_facebook);
                btnFacebook.setVisibility(View.VISIBLE);
                avatar.setOnClickListener(view -> {
                    final Intent intent = new Intent(getActivity(), EditPhotoActivity.class);
                    startActivity(intent);
                });
                FacebookSdk.sdkInitialize(getContext().getApplicationContext());
                final CallbackManager callbackManager =
                        ((MainActivity) getActivity()).getCallbackManager();
                LoginManager.getInstance().registerCallback(callbackManager,
                        new FacebookCallback<LoginResult>() {
                            @Override
                            public void onSuccess(final LoginResult result) {
                                if (BuildConfig.DEBUG) {
                                    Log.d(TAG, "facebook:onSuccess:" + result);
                                }
                                final AuthCredential credential =
                                        FacebookAuthProvider.getCredential(result.getAccessToken().getToken());
                                user.linkWithCredential(credential)
                                        .addOnCompleteListener(task -> {
                                            if (BuildConfig.DEBUG) {
                                                Log.d("FA", "linkWithCredential:onComplete:" + task.isSuccessful());
                                            }
                                            if (task.isSuccessful()) {
                                                Toast.makeText(getContext().getApplicationContext(),
                                                        task.getResult() +
                                                                " Successfully connected with Facebook",
                                                        Toast.LENGTH_SHORT).show();
                                                final ImageView avatar = (ImageView)
                                                        getActivity().findViewById(R.id.imageView3);
                                                Picasso.with(getActivity().getApplicationContext())
                                                        .load(user.getPhotoUrl())
                                                        .transform(new RoundedTransformation())
                                                        .into(avatar);
                                            } else {
                                                Toast.makeText(getContext().getApplicationContext(),
                                                        "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }

                            @Override
                            public void onCancel() {
                                Log.d(TAG, "facebook:onCancel");
                                // ...
                            }

                            @Override
                            public void onError(final FacebookException error) {
                                Log.d(TAG, "facebook:onError", error);
                            }
                        });
                // set up facebook btn
                btnFacebook.setOnClickListener(view ->
                        LoginManager.getInstance().
                                logInWithReadPermissions(getActivity(),
                                        Arrays.asList("email", "public_profile")));
            } else {
                Picasso.with(getActivity().getApplicationContext())
                        .load(user.getPhotoUrl())
                        .transform(new RoundedTransformation())
                        .into(avatar);
                final View topPanel = result.findViewById(R.id.topPanel);
                avatar.setOnClickListener(view -> {
                    final Intent intent = new Intent(getActivity(), EditPhotoActivity.class);
                    startActivity(intent);
                });
                topPanel.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                    if (topPanel.getHeight() - PixelUtils.dpToPixel(getContext(), 50) < 200) {
                        avatar.setImageAlpha(0);
                        name.setGravity(Gravity.CENTER);
                        location.setGravity(Gravity.CENTER);
                        Log.i("Top Panel", "I really can't fit on top panel, the view is only " +
                                (topPanel.getHeight() - PixelUtils.dpToPixel(getContext(), 50)));
                    } else {
                        avatar.setImageAlpha(255);
                        name.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                        location.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                        Log.i("Top Panel", "I fit on top panel");
                    }
                });
            }


            btnDelete.setOnClickListener(view -> deleteUser(user));
            name.setText(user.getDisplayName());
            email.setText(user.getEmail());
//            getChildFragmentManager()
//                    .beginTransaction()
//                    .add(R.id.userInfo, new EditPhotoFragment())
//                    .commit();
        }

        return result;
    }

    private void deleteUser(final FirebaseUser user) {
        if (user.getPhotoUrl() == null || !user.getPhotoUrl().toString().contains("facebook")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Please re-enter your password");

            // Set up the input
            final EditText input = new EditText(getActivity());
            // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            builder.setView(input);

            // Set up the buttons
            builder.setPositiveButton("OK", (dialog, which) -> {
                final String inputPassword = input.getText().toString();
                if (!inputPassword.isEmpty() && user.getEmail() != null) {
                    final AuthCredential credential = EmailAuthProvider
                            .getCredential(user.getEmail(), inputPassword);
                    user.reauthenticate(credential).addOnCompleteListener(task ->
                            user.delete().addOnCompleteListener(t -> {
                                if (task.isSuccessful()) {
                                    Log.d("FA", "User account deleted.");
                                } else {
                                    Toast.makeText(getContext().getApplicationContext(), "Password isn't correct",
                                            Toast.LENGTH_SHORT);
                                }
                            }));
                } else if (inputPassword.isEmpty()) {
                    Toast.makeText(getContext().getApplicationContext(), "Password was empty",
                            Toast.LENGTH_SHORT);
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        } else {
            FacebookSdk.sdkInitialize(getContext().getApplicationContext());
            LoginManager.getInstance().
                    logInWithReadPermissions(getActivity(),
                            Arrays.asList("email", "public_profile"));
            final CallbackManager callbackManager =
                    ((MainActivity) getActivity()).getCallbackManager();
            LoginManager.getInstance().registerCallback(callbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(final LoginResult result) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "facebook:onSuccess:" + result);
                            }
                            final AuthCredential credential = FacebookAuthProvider.getCredential(
                                    result.getAccessToken().getToken());
                            user.reauthenticate(credential).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    user.delete().addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            final Intent intent = new Intent(getActivity(), MainActivity.class);
                                            startActivity(intent);
                                            Log.i("FA", "user deleted");
                                        } else {
                                            Log.e("FA", "error deleting user");
                                        }
                                    });
                                } else if (BuildConfig.DEBUG) {
                                    Log.i("FA", "error reauthorizing user");
                                }
                            });
                        }

                        @Override
                        public void onCancel() {
                            Log.d(TAG, "facebook:onCancel");
                            // ignored
                        }

                        @Override
                        public void onError(final FacebookException error) {
                            Log.d(TAG, "facebook:onError", error);
                        }
                    });
        }
    }

    static boolean[] getEditorOpened() {
        return editorOpened;
    }
}