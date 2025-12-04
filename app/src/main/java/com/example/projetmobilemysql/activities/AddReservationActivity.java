package com.example.projetmobilemysql.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.database.PropertyDAO;
import com.example.projetmobilemysql.database.ReservationDAO;
import com.example.projetmobilemysql.models.Property;
import com.example.projetmobilemysql.models.Reservation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddReservationActivity extends AppCompatActivity {

    private Spinner propertySpinner;
    private TextInputEditText clientNameInput, clientPhoneInput;
    private TextInputEditText startDateInput, endDateInput;
    private TextInputEditText totalAmountInput, advanceAmountInput, notesInput;
    private RadioButton statusPending, statusReserved;
    private MaterialButton saveButton;
    private MaterialToolbar toolbar;

    private PropertyDAO propertyDAO;
    private ReservationDAO reservationDAO;
    private int currentUserId;
    private List<Property> propertyList;
    private int selectedPropertyId = -1;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_reservation);

        // Initialiser DAOs
        propertyDAO = new PropertyDAO(this);
        reservationDAO = new ReservationDAO(this);

        // Récupérer l'ID de l'utilisateur connecté
        SharedPreferences prefs = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        // Initialiser les vues
        initViews();

        // Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Charger les propriétés
        loadProperties();

        // Listeners pour les dates
        startDateInput.setOnClickListener(v -> showStartDatePicker());
        endDateInput.setOnClickListener(v -> showEndDatePicker());

        // Listener
        saveButton.setOnClickListener(v -> saveReservation());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        propertySpinner = findViewById(R.id.propertySpinner);
        clientNameInput = findViewById(R.id.clientNameInput);
        clientPhoneInput = findViewById(R.id.clientPhoneInput);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
        totalAmountInput = findViewById(R.id.totalAmountInput);
        advanceAmountInput = findViewById(R.id.advanceAmountInput);
        notesInput = findViewById(R.id.notesInput);
        statusPending = findViewById(R.id.statusPending);
        statusReserved = findViewById(R.id.statusReserved);
        saveButton = findViewById(R.id.saveButton);
    }

    private void loadProperties() {
        new Thread(() -> {
            try {
                propertyList = propertyDAO.getAllProperties();

                runOnUiThread(() -> {
                    if (propertyList.isEmpty()) {
                        Toast.makeText(this,
                                "Aucune propriété disponible. Ajoutez-en une d'abord.",
                                Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    // Créer la liste des titres pour le Spinner
                    List<String> propertyTitles = new ArrayList<>();
                    propertyTitles.add("Sélectionnez une propriété");
                    for (Property property : propertyList) {
                        propertyTitles.add(property.getTitle());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            this,
                            android.R.layout.simple_spinner_item,
                            propertyTitles
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    propertySpinner.setAdapter(adapter);

                    propertySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position > 0) {
                                Property selectedProperty = propertyList.get(position - 1);
                                selectedPropertyId = selectedProperty.getId();

                                // Pré-remplir le prix
                                totalAmountInput.setText(String.valueOf(selectedProperty.getPricePerDay()));
                            } else {
                                selectedPropertyId = -1;
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedPropertyId = -1;
                        }
                    });
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showStartDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    startCalendar.set(year, month, dayOfMonth);
                    startDateInput.setText(dateFormatter.format(startCalendar.getTime()));
                },
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    endCalendar.set(year, month, dayOfMonth);
                    endDateInput.setText(dateFormatter.format(endCalendar.getTime()));
                },
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void saveReservation() {
        // Récupérer les valeurs
        String clientName = clientNameInput.getText().toString().trim();
        String clientPhone = clientPhoneInput.getText().toString().trim();
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();
        String totalAmount = totalAmountInput.getText().toString().trim();
        String advanceAmount = advanceAmountInput.getText().toString().trim();
        String notes = notesInput.getText().toString().trim();

        // Validations
        if (selectedPropertyId == -1) {
            Toast.makeText(this, "Sélectionnez une propriété", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(clientName)) {
            clientNameInput.setError("Nom du client requis");
            clientNameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(startDate)) {
            Toast.makeText(this, "Sélectionnez la date de début", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(endDate)) {
            Toast.makeText(this, "Sélectionnez la date de fin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(totalAmount)) {
            totalAmountInput.setError("Montant total requis");
            totalAmountInput.requestFocus();
            return;
        }

        // Vérifier que la date de fin est après la date de début
        if (endCalendar.before(startCalendar)) {
            Toast.makeText(this, "La date de fin doit être après la date de début",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Créer l'objet Reservation
        Reservation reservation = new Reservation();
        reservation.setPropertyId(selectedPropertyId);
        reservation.setSamsarId(currentUserId);
        reservation.setStartDate(startDate);
        reservation.setEndDate(endDate);
        reservation.setClientName(clientName);
        reservation.setClientPhone(clientPhone);
        reservation.setTotalAmount(parseDouble(totalAmount));
        reservation.setAdvanceAmount(parseDouble(advanceAmount));
        reservation.setNotes(notes);
        reservation.setStatus(statusReserved.isChecked() ? "reserved" : "pending");

        // Sauvegarder en arrière-plan
        saveButton.setEnabled(false);
        saveButton.setText("Enregistrement...");

        new Thread(() -> {
            try {
                // Vérifier la disponibilité
                boolean available = reservationDAO.isPropertyAvailable(
                        selectedPropertyId, startDate, endDate
                );

                if (!available) {
                    runOnUiThread(() -> {
                        Toast.makeText(this,
                                "Cette propriété n'est pas disponible pour cette période",
                                Toast.LENGTH_LONG).show();
                        saveButton.setEnabled(true);
                        saveButton.setText("Enregistrer");
                    });
                    return;
                }

                long reservationId = reservationDAO.createReservation(reservation);

                runOnUiThread(() -> {
                    if (reservationId > 0) {
                        Toast.makeText(this, "Réservation créée avec succès!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de la création",
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
}