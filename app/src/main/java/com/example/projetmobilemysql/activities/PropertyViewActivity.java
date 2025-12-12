package com.example.projetmobilemysql.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.adapters.PropertyImageAdapter;
import com.example.projetmobilemysql.database.PropertyDAO;
import com.example.projetmobilemysql.database.PropertyImageDAO;
import com.example.projetmobilemysql.models.Property;
import com.example.projetmobilemysql.models.PropertyImage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PropertyViewActivity extends AppCompatActivity {

    private TextView titleText, typeText, configText, addressText;
    private TextView pricePerDayText, pricePerWeekText, pricePerMonthText;
    private TextView distanceBeachText, maxCapacityText, bathroomsText;
    private TextView ownerContactText, descriptionText, equipmentsText;
    private MaterialButton editButton, viewCalendarButton;
    private MaterialToolbar toolbar;
    private ImageView mainImageView;
    private RecyclerView imagesRecyclerView;

    private PropertyDAO propertyDAO;
    private PropertyImageDAO imageDAO;
    private int currentUserId;
    private int propertyId;
    private Property currentProperty;

    private PropertyImageAdapter imageAdapter;
    private List<PropertyImage> imageList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_view);

        propertyDAO = new PropertyDAO(this);
        imageDAO = new PropertyImageDAO(this);

        SharedPreferences prefs = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        propertyId = getIntent().getIntExtra("property_id", -1);

        if (propertyId == -1) {
            Toast.makeText(this, "Erreur: propriété introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupImageRecyclerView();

        toolbar.setNavigationOnClickListener(v -> finish());

        loadPropertyData();
        loadPropertyImages();

        editButton.setOnClickListener(v -> openEditActivity());
        viewCalendarButton.setOnClickListener(v -> openCalendar());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        titleText = findViewById(R.id.titleText);
        typeText = findViewById(R.id.typeText);
        configText = findViewById(R.id.configText);
        addressText = findViewById(R.id.addressText);
        pricePerDayText = findViewById(R.id.pricePerDayText);
        pricePerWeekText = findViewById(R.id.pricePerWeekText);
        pricePerMonthText = findViewById(R.id.pricePerMonthText);
        distanceBeachText = findViewById(R.id.distanceBeachText);
        maxCapacityText = findViewById(R.id.maxCapacityText);
        bathroomsText = findViewById(R.id.bathroomsText);
        ownerContactText = findViewById(R.id.ownerContactText);
        descriptionText = findViewById(R.id.descriptionText);
        equipmentsText = findViewById(R.id.equipmentsText);

        editButton = findViewById(R.id.editButton);
        viewCalendarButton = findViewById(R.id.viewCalendarButton);
        mainImageView = findViewById(R.id.mainImageView);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
    }

    private void setupImageRecyclerView() {
        imageAdapter = new PropertyImageAdapter(imageList, this);
        imagesRecyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imagesRecyclerView.setAdapter(imageAdapter);

        imageAdapter.setOnImageActionListener(new PropertyImageAdapter.OnImageActionListener() {
            @Override
            public void onDeleteImage(PropertyImage image, int position) {
                // Mode lecture seule - pas de suppression
            }

            @Override
            public void onSetMainImage(PropertyImage image, int position) {
                // Mode lecture seule - pas de modification
            }

            @Override
            public void onImageClick(PropertyImage image) {
                openImageFullScreen(image);
            }
        });
    }

    private void loadPropertyData() {
        new Thread(() -> {
            try {
                currentProperty = propertyDAO.getPropertyById(propertyId);

                runOnUiThread(() -> {
                    if (currentProperty != null) {
                        displayProperty();
                    } else {
                        Toast.makeText(this, "Propriété introuvable", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        }).start();
    }

    private void loadPropertyImages() {
        new Thread(() -> {
            try {
                List<PropertyImage> images = imageDAO.getPropertyImages(propertyId);

                runOnUiThread(() -> {
                    imageList.clear();
                    imageList.addAll(images);
                    imageAdapter.notifyDataSetChanged();

                    // Charger l'image principale
                    if (!images.isEmpty()) {
                        PropertyImage mainImage = null;
                        for (PropertyImage img : images) {
                            if (img.isMain()) {
                                mainImage = img;
                                break;
                            }
                        }

                        if (mainImage == null && !images.isEmpty()) {
                            mainImage = images.get(0);
                        }

                        if (mainImage != null) {
                            loadMainImage(mainImage.getImagePath());
                        }

                        imagesRecyclerView.setVisibility(View.VISIBLE);
                    } else {
                        mainImageView.setVisibility(View.GONE);
                        imagesRecyclerView.setVisibility(View.GONE);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur chargement images: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadMainImage(String imagePath) {
        new Thread(() -> {
            try {
                File imgFile = new File(imagePath);
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    runOnUiThread(() -> {
                        mainImageView.setImageBitmap(bitmap);
                        mainImageView.setVisibility(View.VISIBLE);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void displayProperty() {
        toolbar.setTitle(currentProperty.getTitle());

        titleText.setText(currentProperty.getTitle());
        typeText.setText(currentProperty.getType().equals("maison") ? "🏠 Maison" : "🏢 Appartement");
        configText.setText(currentProperty.getConfiguration());
        addressText.setText(currentProperty.getAddress());

        pricePerDayText.setText(String.format("%.0f TND/jour", currentProperty.getPricePerDay()));
        pricePerWeekText.setText(String.format("%.0f TND/semaine", currentProperty.getPricePerWeek()));
        pricePerMonthText.setText(String.format("%.0f TND/mois", currentProperty.getPricePerMonth()));

        distanceBeachText.setText(String.format("%.0f m", currentProperty.getDistanceBeach()));
        maxCapacityText.setText(String.format("%d personnes", currentProperty.getMaxCapacity()));
        bathroomsText.setText(String.format("%d salles de bain", currentProperty.getBathrooms()));

        ownerContactText.setText(currentProperty.getOwnerContact());
        descriptionText.setText(currentProperty.getDescription());

        StringBuilder equipments = new StringBuilder();
        if (currentProperty.isWifi()) equipments.append("✓ WiFi\n");
        if (currentProperty.isAirCondition()) equipments.append("✓ Climatisation\n");
        if (currentProperty.isPool()) equipments.append("✓ Piscine\n");
        if (currentProperty.isSeaView()) equipments.append("✓ Vue sur mer\n");
        if (currentProperty.isTerrace()) equipments.append("✓ Terrasse\n");
        if (currentProperty.isKitchen()) equipments.append("✓ Cuisine équipée\n");
        if (currentProperty.isGarage()) equipments.append("✓ Garage\n");

        if (equipments.length() > 0) {
            equipmentsText.setText(equipments.toString().trim());
        } else {
            equipmentsText.setText("Aucun équipement listé");
        }
    }

    private void openEditActivity() {
        Intent intent = new Intent(this, PropertyDetailActivity.class);
        intent.putExtra("property_id", propertyId);
        startActivity(intent);
        finish();
    }

    private void openCalendar() {
        Intent intent = new Intent(this, PropertyCalendarActivity.class);
        intent.putExtra("property_id", propertyId);
        intent.putExtra("property_title", currentProperty.getTitle());
        startActivity(intent);
    }

    private void openImageFullScreen(PropertyImage image) {
        Intent intent = new Intent(this, ImageFullScreenActivity.class);
        intent.putExtra("property_id", propertyId);
        intent.putExtra("current_position", imageList.indexOf(image));
        startActivity(intent);
    }
}