package com.example.projetmobilemysql.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.database.PropertyDAO;
import com.example.projetmobilemysql.models.Property;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class PropertyDetailActivity extends AppCompatActivity {

    private TextInputEditText titleInput, configInput, priceDayInput, priceWeekInput;
    private TextInputEditText priceMonthInput, addressInput, distanceBeachInput;
    private TextInputEditText maxCapacityInput, bathroomsInput, ownerContactInput, descriptionInput;

    private RadioButton typeMaison, typeAppartement;
    private CheckBox checkWifi, checkAirCondition, checkPool, checkSeaView;
    private CheckBox checkTerrace, checkKitchen, checkGarage;

    private MaterialButton saveButton, deleteButton, viewCalendarButton;
    private MaterialToolbar toolbar;

    private PropertyDAO propertyDAO;
    private int currentUserId;
    private int propertyId;
    private Property currentProperty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail);

        // Initialiser DAO
        propertyDAO = new PropertyDAO(this);

        // Récupérer l'ID de l'utilisateur connecté et l'ID de la propriété
        SharedPreferences prefs = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        propertyId = getIntent().getIntExtra("property_id", -1);

        if (propertyId == -1) {
            Toast.makeText(this, "Erreur: propriété introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        initViews();

        // Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Charger les données de la propriété
        loadPropertyData();

        // Listeners
        saveButton.setOnClickListener(v -> updateProperty());
        deleteButton.setOnClickListener(v -> showDeleteDialog());
        viewCalendarButton.setOnClickListener(v -> openCalendar());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);

        titleInput = findViewById(R.id.titleInput);
        configInput = findViewById(R.id.configInput);
        priceDayInput = findViewById(R.id.priceDayInput);
        priceWeekInput = findViewById(R.id.priceWeekInput);
        priceMonthInput = findViewById(R.id.priceMonthInput);
        addressInput = findViewById(R.id.addressInput);
        distanceBeachInput = findViewById(R.id.distanceBeachInput);
        maxCapacityInput = findViewById(R.id.maxCapacityInput);
        bathroomsInput = findViewById(R.id.bathroomsInput);
        ownerContactInput = findViewById(R.id.ownerContactInput);
        descriptionInput = findViewById(R.id.descriptionInput);

        typeMaison = findViewById(R.id.typeMaison);
        typeAppartement = findViewById(R.id.typeAppartement);

        checkWifi = findViewById(R.id.checkWifi);
        checkAirCondition = findViewById(R.id.checkAirCondition);
        checkPool = findViewById(R.id.checkPool);
        checkSeaView = findViewById(R.id.checkSeaView);
        checkTerrace = findViewById(R.id.checkTerrace);
        checkKitchen = findViewById(R.id.checkKitchen);
        checkGarage = findViewById(R.id.checkGarage);

        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        viewCalendarButton = findViewById(R.id.viewCalendarButton);
    }

    private void loadPropertyData() {
        new Thread(() -> {
            try {
                currentProperty = propertyDAO.getPropertyById(propertyId);

                runOnUiThread(() -> {
                    if (currentProperty != null) {
                        populateFields();
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

    private void populateFields() {
        titleInput.setText(currentProperty.getTitle());
        configInput.setText(currentProperty.getConfiguration());
        priceDayInput.setText(String.valueOf(currentProperty.getPricePerDay()));
        priceWeekInput.setText(String.valueOf(currentProperty.getPricePerWeek()));
        priceMonthInput.setText(String.valueOf(currentProperty.getPricePerMonth()));
        addressInput.setText(currentProperty.getAddress());
        distanceBeachInput.setText(String.valueOf(currentProperty.getDistanceBeach()));
        maxCapacityInput.setText(String.valueOf(currentProperty.getMaxCapacity()));
        bathroomsInput.setText(String.valueOf(currentProperty.getBathrooms()));
        ownerContactInput.setText(currentProperty.getOwnerContact());
        descriptionInput.setText(currentProperty.getDescription());

        if (currentProperty.getType().equals("maison")) {
            typeMaison.setChecked(true);
        } else {
            typeAppartement.setChecked(true);
        }

        checkWifi.setChecked(currentProperty.isWifi());
        checkAirCondition.setChecked(currentProperty.isAirCondition());
        checkPool.setChecked(currentProperty.isPool());
        checkSeaView.setChecked(currentProperty.isSeaView());
        checkTerrace.setChecked(currentProperty.isTerrace());
        checkKitchen.setChecked(currentProperty.isKitchen());
        checkGarage.setChecked(currentProperty.isGarage());
    }

    private void updateProperty() {
        String title = titleInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String priceDay = priceDayInput.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            titleInput.setError("Titre requis");
            titleInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(address)) {
            addressInput.setError("Adresse requise");
            addressInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(priceDay)) {
            priceDayInput.setError("Prix par jour requis");
            priceDayInput.requestFocus();
            return;
        }

        // Mettre à jour l'objet Property
        currentProperty.setTitle(title);
        currentProperty.setType(typeMaison.isChecked() ? "maison" : "appartement");
        currentProperty.setConfiguration(configInput.getText().toString().trim());
        currentProperty.setPricePerDay(parseDouble(priceDay));
        currentProperty.setPricePerWeek(parseDouble(priceWeekInput.getText().toString().trim()));
        currentProperty.setPricePerMonth(parseDouble(priceMonthInput.getText().toString().trim()));
        currentProperty.setAddress(address);
        currentProperty.setDistanceBeach(parseDouble(distanceBeachInput.getText().toString().trim()));
        currentProperty.setMaxCapacity(parseInt(maxCapacityInput.getText().toString().trim()));
        currentProperty.setBathrooms(parseInt(bathroomsInput.getText().toString().trim()));
        currentProperty.setOwnerContact(ownerContactInput.getText().toString().trim());
        currentProperty.setDescription(descriptionInput.getText().toString().trim());

        currentProperty.setWifi(checkWifi.isChecked());
        currentProperty.setAirCondition(checkAirCondition.isChecked());
        currentProperty.setPool(checkPool.isChecked());
        currentProperty.setSeaView(checkSeaView.isChecked());
        currentProperty.setTerrace(checkTerrace.isChecked());
        currentProperty.setKitchen(checkKitchen.isChecked());
        currentProperty.setGarage(checkGarage.isChecked());

        saveButton.setEnabled(false);
        saveButton.setText("Mise à jour...");

        new Thread(() -> {
            try {
                int rows = propertyDAO.updateProperty(currentProperty);

                runOnUiThread(() -> {
                    if (rows > 0) {
                        Toast.makeText(this, "Propriété mise à jour!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_LONG).show();
                        saveButton.setEnabled(true);
                        saveButton.setText("Mettre à jour");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    saveButton.setEnabled(true);
                    saveButton.setText("Mettre à jour");
                });
            }
        }).start();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la propriété")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette propriété ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteProperty())
                .setNegativeButton("Annuler", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteProperty() {
        new Thread(() -> {
            try {
                int rows = propertyDAO.deleteProperty(propertyId);

                runOnUiThread(() -> {
                    if (rows > 0) {
                        Toast.makeText(this, "Propriété supprimée", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void openCalendar() {
        Intent intent = new Intent(this, PropertyCalendarActivity.class);
        intent.putExtra("property_id", propertyId);
        intent.putExtra("property_title", currentProperty.getTitle());
        startActivity(intent);
    }

    private double parseDouble(String value) {
        try {
            return TextUtils.isEmpty(value) ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private int parseInt(String value) {
        try {
            return TextUtils.isEmpty(value) ? 0 : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}