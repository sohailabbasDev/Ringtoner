package com.inflexionlabs.ringtoner.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import com.inflexionlabs.ringtoner.R;
import java.util.Objects;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        //toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbarAbout);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);

        toolbar.setNavigationIcon(ResourcesCompat.getDrawable(getResources(),R.drawable.ic_baseline_keyboard_backspace_24,null));
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        TextView privacyText = findViewById(R.id.privacyPolicyText);

        TextView feedbackText = findViewById(R.id.haveFeedbackText);
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        LinearLayout linearLayout = findViewById(R.id.aboutLayout);
        linearLayout.setAnimation(anim);

        feedbackText.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:inflexionlabs@gmail.com")); // only email apps should handle this
            startActivity(intent);
        });

        privacyText.setOnClickListener(view -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://pages.flycricket.io/ringtoner/privacy.html"));
            startActivity(browserIntent);
        });

    }
}