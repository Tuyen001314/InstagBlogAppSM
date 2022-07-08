package com.example.instagappsm.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.instagappsm.R;

import java.awt.font.TextAttribute;

import de.hdodenhof.circleimageview.CircleImageView;

public class OtherUserProfile extends AppCompatActivity {

    private CircleImageView circleImageView;
    private TextView tvName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        circleImageView = findViewById(R.id.avt_other_user);
        tvName = findViewById(R.id.name_other_user);

        String name = getIntent().getStringExtra("name");
        String image = getIntent().getStringExtra("image");

        tvName.setText(name);
        Glide.with(this).load(image).into(circleImageView);
    }
}