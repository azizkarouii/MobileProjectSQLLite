package com.example.projetmobilemysql.activities;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.database.PropertyDAO;
import com.example.projetmobilemysql.models.Property;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddPropertyActivity extends AppCompatActivity {

    private TextInputEditText titleInput, configInput, priceDayInput, priceWeekInput;
    private TextInputEditText priceMonthInput, addressInput, distanceBeachInput;
    private TextInputEditText maxCapacityInput, bathroomsInput, ownerContactInput, descriptionInput;

    private RadioButton typeMaison, typeAppartement;
    private CheckBox checkWifi, checkAirCondition, checkPool, checkSeaView;
    private CheckBox checkTerrace, checkKitchen, checkGarage;

    private MaterialButton saveButton;
    private MaterialToolbar toolbar;

    private PropertyDAO propertyDAO;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        // Initialiser DAO
        propertyDAO = new PropertyDAO(this);

        // Récupérer l'ID de l'utilisateur connecté
        SharedPreferences prefs = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        // Initialiser les vues
        initViews();

        // Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Listener
        saveButton.setOnClickListener(v -> saveProperty());
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
    }

    private void saveProperty() {
        // Récupérer les valeurs
        String title = titleInput.getText().toString().trim();
        String config = configInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String ownerContact = ownerContactInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        // Validations
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

        String priceDay = priceDayInput.getText().toString().trim();
        if (TextUtils.isEmpty(priceDay)) {
            priceDayInput.setError("Prix par jour requis");
            priceDayInput.requestFocus();
            return;
        }

        // Créer l'objet Property
        Property property = new Property();
        property.setTitle(title);
        property.setType(typeMaison.isChecked() ? "maison" : "appartement");
        property.setConfiguration(config);
        property.setPricePerDay(parseDouble(priceDay));
        property.setPricePerWeek(parseDouble(priceWeekInput.getText().toString().trim()));
        property.setPricePerMonth(parseDouble(priceMonthInput.getText().toString().trim()));
        property.setAddress(address);
        property.setDistanceBeach(parseDouble(distanceBeachInput.getText().toString().trim()));
        property.setMaxCapacity(parseInt(maxCapacityInput.getText().toString().trim()));
        property.setBathrooms(parseInt(bathroomsInput.getText().toString().trim()));
        property.setOwnerContact(ownerContact);
        property.setDescription(description);

        // Équipements
        property.setWifi(checkWifi.isChecked());
        property.setAirCondition(checkAirCondition.isChecked());
        property.setPool(checkPool.isChecked());
        property.setSeaView(checkSeaView.isChecked());
        property.setTerrace(checkTerrace.isChecked());
        property.setKitchen(checkKitchen.isChecked());
        property.setGarage(checkGarage.isChecked());

        property.setCreatedBy(currentUserId);

        // Sauvegarder en arrière-plan
        saveButton.setEnabled(false);
        saveButton.setText("Enregistrement...");

        new Thread(() -> {
            try {
                long propertyId = propertyDAO.createProperty(property);

                if (propertyId > 0) {
                    // Ajouter le courtier à la propriété
                    propertyDAO.addSamsarToProperty((int) propertyId, currentUserId);
                }

                runOnUiThread(() -> {
                    if (propertyId > 0) {
                        Toast.makeText(this, "Propriété ajoutée avec succès!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de l'ajout",
                                Toast.LENGTH_LONG).show();
                        saveButton.setEnabled(true);
                        saveButton.setText("Enregistrer");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    saveButton.setEnabled(true);
                    saveButton.setText("Enregistrer");
                });
            }
        }).start();
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