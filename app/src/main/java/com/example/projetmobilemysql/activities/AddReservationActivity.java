package com.example.projetmobilemysql.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
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
import com.example.projetmobilemysql.database.PropertyAvailabilityDAO;
import com.example.projetmobilemysql.database.PropertyDAO;
import com.example.projetmobilemysql.database.ReservationDAO;
import com.example.projetmobilemysql.models.Property;
import com.example.projetmobilemysql.models.PropertyAvailability;
import com.example.projetmobilemysql.models.Reservation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class AddReservationActivity extends AppCompatActivity {

    private Spinner propertySpinner;
    private TextInputEditText clientNameInput, clientPhoneInput;
    private TextInputEditText startDateInput, endDateInput;
    private TextInputEditText totalAmountInput, advanceAmountInput, notesInput;
    private RadioButton statusPending, statusReserved;
    private MaterialButton saveButton, viewCalendarButton;
    private MaterialToolbar toolbar;

    private PropertyDAO propertyDAO;
    private ReservationDAO reservationDAO;
    private PropertyAvailabilityDAO availabilityDAO;
    private int currentUserId;
    private List<Property> propertyList;
    private int selectedPropertyId = -1;
    private Property selectedProperty = null;

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
        availabilityDAO = new PropertyAvailabilityDAO(this);

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

        viewCalendarButton.setOnClickListener(v -> openPropertyCalendar());
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
        viewCalendarButton = findViewById(R.id.viewCalendarButton);
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
                                selectedProperty = propertyList.get(position - 1);
                                selectedPropertyId = selectedProperty.getId();

                                // Activer le bouton calendrier
                                viewCalendarButton.setEnabled(true);

                                // Recalculer le prix si les dates sont déjà sélectionnées
                                calculateTotalAmount();
                            } else {
                                selectedPropertyId = -1;
                                selectedProperty = null;
                                viewCalendarButton.setEnabled(false);
                                totalAmountInput.setText("");
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedPropertyId = -1;
                            selectedProperty = null;
                            viewCalendarButton.setEnabled(false);
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

    private void openPropertyCalendar() {
        if (selectedPropertyId == -1) {
            Toast.makeText(this, "Sélectionnez d'abord une propriété", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedProperty != null) {
            Intent intent = new Intent(this, ReservationCalendarPickerActivity.class);
            intent.putExtra("property_id", selectedPropertyId);
            intent.putExtra("property_title", selectedProperty.getTitle());
            startActivityForResult(intent, 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String startDate = data.getStringExtra("start_date");
            String endDate = data.getStringExtra("end_date");

            if (startDate != null && endDate != null) {
                startDateInput.setText(startDate);
                endDateInput.setText(endDate);

                // Mettre à jour les calendriers
                try {
                    startCalendar.setTime(dateFormatter.parse(startDate));
                    endCalendar.setTime(dateFormatter.parse(endDate));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Calculer automatiquement le montant
                calculateTotalAmount();

                Toast.makeText(this, "Dates sélectionnées depuis le calendrier",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showStartDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    startCalendar.set(year, month, dayOfMonth);
                    String date = dateFormatter.format(startCalendar.getTime());
                    startDateInput.setText(date);

                    // Calculer automatiquement le montant
                    calculateTotalAmount();

                    // Vérifier la disponibilité
                    if (selectedPropertyId != -1) {
                        checkDateAvailability(date);
                    }
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
                    String date = dateFormatter.format(endCalendar.getTime());
                    endDateInput.setText(date);

                    // Calculer automatiquement le montant
                    calculateTotalAmount();

                    // Vérifier la disponibilité
                    if (selectedPropertyId != -1) {
                        checkDateAvailability(date);
                    }
                },
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    /**
     * Calculer automatiquement le montant total basé sur le nombre de jours et le prix par jour
     */
    private void calculateTotalAmount() {
        String startDate = startDateInput.getText().toString().trim();
        String endDate = endDateInput.getText().toString().trim();

        if (TextUtils.isEmpty(startDate) || TextUtils.isEmpty(endDate) || selectedProperty == null) {
            return;
        }

        try {
            // Calculer le nombre de jours
            long diffInMillis = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
            long numberOfDays = TimeUnit.MILLISECONDS.toDays(diffInMillis) + 1; // +1 pour inclure le dernier jour

            if (numberOfDays <= 0) {
                Toast.makeText(this, "La date de fin doit être après la date de début",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculer le montant total
            double pricePerDay = selectedProperty.getPricePerDay();
            double totalAmount = numberOfDays * pricePerDay;

            // Afficher dans le champ
            totalAmountInput.setText(String.format("%.0f", totalAmount));

            // Afficher un message informatif
            Toast.makeText(this,
                    String.format("%d jour(s) × %.0f TND = %.0f TND", numberOfDays, pricePerDay, totalAmount),
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkDateAvailability(String date) {
        new Thread(() -> {
            try {
                PropertyAvailability availability = availabilityDAO.getAvailability(selectedPropertyId, date);

                // Vérifier les réservations confirmées
                List<Reservation> reservations = reservationDAO.getReservationsByProperty(selectedPropertyId);
                boolean hasConflict = false;

                for (Reservation res : reservations) {
                    if (isDateInRange(date, res.getStartDate(), res.getEndDate())) {
                        if (res.getStatus().equals("reserved")) {
                            hasConflict = true;
                            break;
                        }
                    }
                }

                final boolean conflict = hasConflict;
                runOnUiThread(() -> {
                    if (conflict) {
                        Toast.makeText(this, "⚠️ Cette date est déjà réservée",
                                Toast.LENGTH_SHORT).show();
                    } else if (availability != null && availability.getStatus().equals("unavailable")) {
                        Toast.makeText(this, "⚠️ Cette date est marquée comme non disponible",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                // Ignorer l'erreur
            }
        }).start();
    }

    private boolean isDateInRange(String date, String startDate, String endDate) {
        return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
    }

    private void saveReservation() {
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

        if (endCalendar.before(startCalendar)) {
            Toast.makeText(this, "La date de fin doit être après la date de début",
                    Toast.LENGTH_LONG).show();
            return;
        }

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

        saveButton.setEnabled(false);
        saveButton.setText("Enregistrement...");

        new Thread(() -> {
            try {
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