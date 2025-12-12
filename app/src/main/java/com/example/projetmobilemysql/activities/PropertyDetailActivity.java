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
import androidx.appcompat.app.AlertDialog;
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

public class PropertyDetailActivity extends AppCompatActivity {

    private TextInputEditText titleInput, configInput, priceDayInput, priceWeekInput;
    private TextInputEditText priceMonthInput, addressInput, distanceBeachInput;
    private TextInputEditText maxCapacityInput, bathroomsInput, ownerContactInput, descriptionInput;

    private RadioButton typeMaison, typeAppartement;
    private CheckBox checkWifi, checkAirCondition, checkPool, checkSeaView;
    private CheckBox checkTerrace, checkKitchen, checkGarage;

    private MaterialButton saveButton, deleteButton, viewCalendarButton, addImageButton;
    private MaterialToolbar toolbar;
    private RecyclerView imagesRecyclerView;

    private PropertyDAO propertyDAO;
    private PropertyImageDAO imageDAO;
    private int currentUserId;
    private int propertyId;
    private Property currentProperty;

    // Gestion des images
    private PropertyImageAdapter imageAdapter;
    private List<PropertyImage> imageList = new ArrayList<>();
    private String currentPhotoPath;
    private boolean isRequestingGallery = false;

    // Launchers
    private ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher;
    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_detail);

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
        setupActivityLaunchers();

        toolbar.setNavigationOnClickListener(v -> finish());

        loadPropertyData();
        loadPropertyImages();

        saveButton.setOnClickListener(v -> updateProperty());
        deleteButton.setOnClickListener(v -> showDeleteDialog());
        viewCalendarButton.setOnClickListener(v -> openCalendar());
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
        deleteButton = findViewById(R.id.deleteButton);
        viewCalendarButton = findViewById(R.id.viewCalendarButton);
        addImageButton = findViewById(R.id.addImageButton);
        imagesRecyclerView = findViewById(R.id.imagesRecyclerView);
    }

    private void setupImageRecyclerView() {
        imageAdapter = new PropertyImageAdapter(imageList, this);
        imagesRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        imagesRecyclerView.setAdapter(imageAdapter);

        imageAdapter.setOnImageActionListener(new PropertyImageAdapter.OnImageActionListener() {
            @Override
            public void onDeleteImage(PropertyImage image, int position) {
                showDeleteImageDialog(image, position);
            }

            @Override
            public void onSetMainImage(PropertyImage image, int position) {
                setMainImage(image, position);
            }

            @Override
            public void onImageClick(PropertyImage image) {
                // Ouvrir en plein écran (optionnel)
                openImageFullScreen(image);
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

    private void loadPropertyImages() {
        new Thread(() -> {
            try {
                List<PropertyImage> images = imageDAO.getPropertyImages(propertyId);

                runOnUiThread(() -> {
                    imageList.clear();
                    imageList.addAll(images);
                    imageAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur chargement images: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
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
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.projetmobilemysql.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
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
        new Thread(() -> {
            try {
                String destinationPath = copyImageToAppStorage(uri);

                if (destinationPath != null) {
                    PropertyImage image = new PropertyImage();
                    image.setPropertyId(propertyId);
                    image.setImagePath(destinationPath);
                    image.setMain(imageList.isEmpty());
                    image.setPosition(imageList.size());

                    long imageId = imageDAO.addImage(image);
                    image.setId((int) imageId);

                    runOnUiThread(() -> {
                        imageList.add(image);
                        imageAdapter.notifyItemInserted(imageList.size() - 1);
                        Toast.makeText(this, "Image ajoutée", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void addImageFromPath(String path) {
        new Thread(() -> {
            try {
                PropertyImage image = new PropertyImage();
                image.setPropertyId(propertyId);
                image.setImagePath(path);
                image.setMain(imageList.isEmpty());
                image.setPosition(imageList.size());

                long imageId = imageDAO.addImage(image);
                image.setId((int) imageId);

                runOnUiThread(() -> {
                    imageList.add(image);
                    imageAdapter.notifyItemInserted(imageList.size() - 1);
                    Toast.makeText(this, "Photo ajoutée", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
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

    private void showDeleteImageDialog(PropertyImage image, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer l'image")
                .setMessage("Voulez-vous vraiment supprimer cette image ?")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteImage(image, position))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void deleteImage(PropertyImage image, int position) {
        new Thread(() -> {
            try {
                // Supprimer de la base de données
                imageDAO.deleteImage(image.getId());

                // Supprimer le fichier physique
                File file = new File(image.getImagePath());
                if (file.exists()) {
                    file.delete();
                }

                runOnUiThread(() -> {
                    imageList.remove(position);
                    imageAdapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Image supprimée", Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void setMainImage(PropertyImage image, int position) {
        new Thread(() -> {
            try {
                boolean success = imageDAO.setMainImage(image.getId(), propertyId);

                if (success) {
                    runOnUiThread(() -> {
                        // Mettre à jour l'affichage
                        for (PropertyImage img : imageList) {
                            img.setMain(false);
                        }
                        imageList.get(position).setMain(true);
                        imageAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "Image principale définie", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void openImageFullScreen(PropertyImage image) {
        Intent intent = new Intent(this, ImageFullScreenActivity.class);
        intent.putExtra("property_id", propertyId);
        intent.putExtra("current_position", imageList.indexOf(image));
        startActivity(intent);
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
                // Supprimer toutes les images
                List<PropertyImage> images = imageDAO.getPropertyImages(propertyId);
                for (PropertyImage image : images) {
                    File file = new File(image.getImagePath());
                    if (file.exists()) {
                        file.delete();
                    }
                }
                imageDAO.deletePropertyImages(propertyId);

                // Supprimer la propriété
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