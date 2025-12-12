package com.example.projetmobilemysql.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.adapters.ImagePagerAdapter;
import com.example.projetmobilemysql.database.PropertyImageDAO;
import com.example.projetmobilemysql.models.PropertyImage;
import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageFullScreenActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private MaterialToolbar toolbar;
    private TextView imageCounter;
    private PropertyImageDAO imageDAO;
    private ImagePagerAdapter adapter;
    private List<PropertyImage> imageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_full_screen);

        imageDAO = new PropertyImageDAO(this);

        toolbar = findViewById(R.id.toolbar);
        viewPager = findViewById(R.id.imageViewPager);
        imageCounter = findViewById(R.id.imageCounter);

        toolbar.setNavigationOnClickListener(v -> finish());

        int propertyId = getIntent().getIntExtra("property_id", -1);
        int currentPosition = getIntent().getIntExtra("current_position", 0);

        if (propertyId == -1) {
            Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadImages(propertyId, currentPosition);
    }

    private void loadImages(int propertyId, int startPosition) {
        new Thread(() -> {
            try {
                List<PropertyImage> images = imageDAO.getPropertyImages(propertyId);

                runOnUiThread(() -> {
                    if (images.isEmpty()) {
                        Toast.makeText(this, "Aucune image", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    imageList.clear();
                    imageList.addAll(images);

                    adapter = new ImagePagerAdapter(imageList, this);
                    viewPager.setAdapter(adapter);
                    viewPager.setCurrentItem(startPosition, false);

                    updateCounter(startPosition + 1, imageList.size());

                    viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            super.onPageSelected(position);
                            updateCounter(position + 1, imageList.size());
                        }
                    });
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void updateCounter(int current, int total) {
        imageCounter.setText(current + " / " + total);
    }
}