package com.inflexionlabs.ringtoner.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.inflexionlabs.ringtoner.BuildConfig;
import com.inflexionlabs.ringtoner.R;
import com.inflexionlabs.ringtoner.activities.AboutActivity;


public class AccountFragment extends Fragment {

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        BottomNavigationView bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setVisibility(View.VISIBLE);
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout linearLayout = view.findViewById(R.id.linearLayout);
        Animation prevAnim = AnimationUtils.makeInAnimation(getActivity(), true);

        final String appPackageName = requireActivity().getPackageName(); // getPackageName() from Context or Activity object

        linearLayout.setAnimation(prevAnim);

        TextView aboutText = view.findViewById(R.id.aboutText);
        TextView rateUsText = view.findViewById(R.id.rateText);
        TextView shareText = view.findViewById(R.id.shareText);

        aboutText.setOnClickListener(view1 -> {
            Intent aboutIntent = new Intent(getActivity(), AboutActivity.class);
            startActivity(aboutIntent);
        });

        shareText.setOnClickListener(view1 -> {
            try {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, requireActivity().getString(R.string.app_name));
                String shareMessage= "\nHey i found some amazing ringtones, with vast categories such as Bollywood, Marimba, Gaming, BGM, NCS and more in "+ requireActivity().getString(R.string.app_name)+" download the app right now! \nClick the link - ";
                shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID +"\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                startActivity(Intent.createChooser(shareIntent, "choose one"));
            } catch(Exception e) {
                //e.toString();
            }
        });

        rateUsText.setOnClickListener(view1 -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
            }
        });

    }
}