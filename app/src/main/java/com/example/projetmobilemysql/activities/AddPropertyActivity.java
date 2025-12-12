package com.example.projetmobilemysql.activities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.adapters.PropertyImageAdapter;
import com.example.projetmobilemysql.database.PropertyDAO;
import com.example.projetmobilemysql.database.PropertyImageDAO;
import com.example.projetmobilemysql.models.Property;
import com.example.projetmobilemysql.models.PropertyImage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddPropertyActivity extends AppCompatActivity {

    private TextInputEditText titleInput, configInput, priceDayInput, priceWeekInput;
    private TextInputEditText priceMonthInput, addressInput, distanceBeachInput;
    private TextInputEditText maxCapacityInput, bathroomsInput, ownerContactInput, descriptionInput;

    private RadioButton typeMaison, typeAppartement;
    private CheckBox checkWifi, checkAirCondition, checkPool, checkSeaView;
    private CheckBox checkTerrace, checkKitchen, checkGarage;

    private MaterialButton saveButton, addImageButton;
    private MaterialToolbar toolbar;
    private RecyclerView imagesRecyclerView;

    private PropertyDAO propertyDAO;
    private PropertyImageDAO imageDAO;
    private int currentUserId;

    private PropertyImageAdapter imageAdapter;
    private List<PropertyImage> tempImageList = new ArrayList<>();
    private String currentPhotoPath;
    private boolean isRequestingGallery = false;

    private ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        propertyDAO = new PropertyDAO(this);
        imageDAO = new PropertyImageDAO(this);

        SharedPreferences prefs = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        initViews();
        setupImageRecyclerView();
        setupActivityLaunchers();

        toolbar.setNavigationOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> saveProperty());
        addImageButton.setOnClickListener(v -> checkPermissionAndPickImage());
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
        addImageButton = findViewById(R.id.addImageButton);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
    }

    private void setupImageRecyclerView() {
        imageAdapter = new PropertyImageAdapter(tempImageList, this);
        imagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imagesRecyclerView.setAdapter(imageAdapter);

        imageAdapter.setOnImageActionListener(new PropertyImageAdapter.OnImageActionListener() {
            @Override
            public void onDeleteImage(PropertyImage image, int position) {
                tempImageList.remove(position);
                imageAdapter.notifyItemRemoved(position);
                Toast.makeText(AddPropertyActivity.this, "Image supprimée", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSetMainImage(PropertyImage image, int position) {
                for (PropertyImage img : tempImageList) {
                    img.setMain(false);
                }
                tempImageList.get(position).setMain(true);
                imageAdapter.notifyDataSetChanged();
                Toast.makeText(AddPropertyActivity.this, "Image principale définie", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onImageClick(PropertyImage image) {
                // Optionnel
            }
        });
    }

    private void setupActivityLaunchers() {
        // Multiple permissions launcher
        requestMultiplePermissionsLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                permissions -> {
                    boolean allGranted = true;
                    for (Boolean granted : permissions.values()) {
                        if (!granted) {
                            allGranted = false;
                            break;
                        }
                    }

                    if (allGranted) {
                        if (isRequestingGallery) {
                            openGallery();
                        } else {
                            openCamera();
                        }
                    } else {
                        Toast.makeText(this, "Permissions nécessaires refusées", Toast.LENGTH_LONG).show();
                    }
                }
        );

        // Gallery launcher
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            addImageFromUri(selectedImageUri);
                        }
                    }
                }
        );

        // Camera launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (currentPhotoPath != null) {
                            addImageFromPath(currentPhotoPath);
                        }
                    }
                }
        );
    }

    private void checkPermissionAndPickImage() {
        isRequestingGallery = true;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestMultiplePermissionsLauncher.launch(new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES
                });
            }
        } else {
            // Android 12 et inférieur
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestMultiplePermissionsLauncher.launch(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                });
            }
        }
    }

    private void checkPermissionAndTakePhoto() {
        isRequestingGallery = false;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            requestMultiplePermissionsLauncher.launch(new String[]{
                    Manifest.permission.CAMERA
            });
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Erreur création fichier", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.projetmobilemysql.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
            Toast.makeText(this, "Aucune application caméra disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "PROPERTY_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void addImageFromUri(Uri uri) {
        try {
            String destinationPath = copyImageToAppStorage(uri);

            if (destinationPath != null) {
                PropertyImage image = new PropertyImage();
                image.setImagePath(destinationPath);
                image.setMain(tempImageList.isEmpty());
                image.setPosition(tempImageList.size());

                tempImageList.add(image);
                imageAdapter.notifyItemInserted(tempImageList.size() - 1);

                Toast.makeText(this, "Image ajoutée", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void addImageFromPath(String path) {
        PropertyImage image = new PropertyImage();
        image.setImagePath(path);
        image.setMain(tempImageList.isEmpty());
        image.setPosition(tempImageList.size());

        tempImageList.add(image);
        imageAdapter.notifyItemInserted(tempImageList.size() - 1);

        Toast.makeText(this, "Photo ajoutée", Toast.LENGTH_SHORT).show();
    }

    private String copyImageToAppStorage(Uri sourceUri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return null;

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "PROPERTY_" + timeStamp + ".jpg";

            File destFile = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

            OutputStream outputStream = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

            return destFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveProperty() {
        String title = titleInput.getText().toString().trim();
        String config = configInput.getText().toString().trim();
        String address = addressInput.getText().toString().trim();
        String ownerContact = ownerContactInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

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

        property.setWifi(checkWifi.isChecked());
        property.setAirCondition(checkAirCondition.isChecked());
        property.setPool(checkPool.isChecked());
        property.setSeaView(checkSeaView.isChecked());
        property.setTerrace(checkTerrace.isChecked());
        property.setKitchen(checkKitchen.isChecked());
        property.setGarage(checkGarage.isChecked());

        property.setCreatedBy(currentUserId);

        saveButton.setEnabled(false);
        saveButton.setText("Enregistrement...");

        new Thread(() -> {
            try {
                long propertyId = propertyDAO.createProperty(property);

                if (propertyId > 0) {
                    propertyDAO.addSamsarToProperty((int) propertyId, currentUserId);

                    // Sauvegarder les images
                    for (PropertyImage image : tempImageList) {
                        image.setPropertyId((int) propertyId);
                        imageDAO.addImage(image);
                    }
                }

                runOnUiThread(() -> {
                    if (propertyId > 0) {
                        Toast.makeText(this, "Propriété ajoutée avec succès!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_LONG).show();
                        saveButton.setEnabled(true);
                        saveButton.setText("Enregistrer");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
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