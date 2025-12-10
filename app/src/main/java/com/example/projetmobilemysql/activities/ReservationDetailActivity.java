package com.example.projetmobilemysql.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.database.ReservationDAO;
import com.example.projetmobilemysql.database.RevenueHistoryDAO;
import com.example.projetmobilemysql.models.Reservation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ReservationDetailActivity extends AppCompatActivity {

    private TextInputEditText clientNameInput, clientPhoneInput;
    private TextInputEditText startDateInput, endDateInput;
    private TextInputEditText checkInTimeInput, checkOutTimeInput;
    private TextInputEditText totalAmountInput, advanceAmountInput, notesInput;
    private RadioButton statusPending, statusReserved, statusActive;
    private MaterialButton saveButton, deleteButton, activateButton;
    private MaterialToolbar toolbar;

    private ReservationDAO reservationDAO;
    private RevenueHistoryDAO revenueDAO;
    private int reservationId;
    private int currentUserId;
    private Reservation currentReservation;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_detail);

        reservationDAO = new ReservationDAO(this);
        revenueDAO = new RevenueHistoryDAO(this);

        SharedPreferences prefs = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);
        reservationId = getIntent().getIntExtra("reservation_id", -1);

        if (reservationId == -1) {
            Toast.makeText(this, "Erreur: réservation introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();

        toolbar.setNavigationOnClickListener(v -> finish());

        loadReservationData();

        startDateInput.setOnClickListener(v -> showStartDatePicker());
        endDateInput.setOnClickListener(v -> showEndDatePicker());
        checkInTimeInput.setOnClickListener(v -> showCheckInTimePicker());
        checkOutTimeInput.setOnClickListener(v -> showCheckOutTimePicker());

        saveButton.setOnClickListener(v -> updateReservation());
        deleteButton.setOnClickListener(v -> showDeleteDialog());
        activateButton.setOnClickListener(v -> activateReservation());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        clientNameInput = findViewById(R.id.clientNameInput);
        clientPhoneInput = findViewById(R.id.clientPhoneInput);
        startDateInput = findViewById(R.id.startDateInput);
        endDateInput = findViewById(R.id.endDateInput);
        checkInTimeInput = findViewById(R.id.checkInTimeInput);
        checkOutTimeInput = findViewById(R.id.checkOutTimeInput);
        totalAmountInput = findViewById(R.id.totalAmountInput);
        advanceAmountInput = findViewById(R.id.advanceAmountInput);
        notesInput = findViewById(R.id.notesInput);
        statusPending = findViewById(R.id.statusPending);
        statusReserved = findViewById(R.id.statusReserved);
        statusActive = findViewById(R.id.statusActive);
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        activateButton = findViewById(R.id.activateButton);
    }

    private void loadReservationData() {
        new Thread(() -> {
            try {
                currentReservation = reservationDAO.getReservationById(reservationId);

                runOnUiThread(() -> {
                    if (currentReservation != null) {
                        populateFields();
                    } else {
                        Toast.makeText(this, "Réservation introuvable", Toast.LENGTH_SHORT).show();
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
        clientNameInput.setText(currentReservation.getClientName());
        clientPhoneInput.setText(currentReservation.getClientPhone());
        startDateInput.setText(currentReservation.getStartDate());
        endDateInput.setText(currentReservation.getEndDate());
        checkInTimeInput.setText(currentReservation.getCheckInTime());
        checkOutTimeInput.setText(currentReservation.getCheckOutTime());
        totalAmountInput.setText(String.valueOf(currentReservation.getTotalAmount()));
        advanceAmountInput.setText(String.valueOf(currentReservation.getAdvanceAmount()));
        notesInput.setText(currentReservation.getNotes());

        String status = currentReservation.getStatus();
        if (status.equals("pending")) {
            statusPending.setChecked(true);
        } else if (status.equals("reserved")) {
            statusReserved.setChecked(true);
        } else if (status.equals("active")) {
            statusActive.setChecked(true);
        }

        // Afficher/masquer le bouton d'activation
        if (status.equals("pending") || status.equals("reserved")) {
            activateButton.setVisibility(android.view.View.VISIBLE);
        } else {
            activateButton.setVisibility(android.view.View.GONE);
        }
    }

    private void showStartDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    startCalendar.set(year, month, day);
                    startDateInput.setText(dateFormatter.format(startCalendar.getTime()));
                },
                startCalendar.get(Calendar.YEAR),
                startCalendar.get(Calendar.MONTH),
                startCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showEndDatePicker() {
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, day) -> {
                    endCalendar.set(year, month, day);
                    endDateInput.setText(dateFormatter.format(endCalendar.getTime()));
                },
                endCalendar.get(Calendar.YEAR),
                endCalendar.get(Calendar.MONTH),
                endCalendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showCheckInTimePicker() {
        String currentTime = checkInTimeInput.getText().toString();
        int hour = 14, minute = 0;
        if (!TextUtils.isEmpty(currentTime) && currentTime.contains(":")) {
            String[] parts = currentTime.split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        }

        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, h, m) -> {
                    checkInTimeInput.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
                }, hour, minute, true);
        dialog.show();
    }

    private void showCheckOutTimePicker() {
        String currentTime = checkOutTimeInput.getText().toString();
        int hour = 12, minute = 0;
        if (!TextUtils.isEmpty(currentTime) && currentTime.contains(":")) {
            String[] parts = currentTime.split(":");
            hour = Integer.parseInt(parts[0]);
            minute = Integer.parseInt(parts[1]);
        }

        TimePickerDialog dialog = new TimePickerDialog(this,
                (view, h, m) -> {
                    checkOutTimeInput.setText(String.format(Locale.getDefault(), "%02d:%02d", h, m));
                }, hour, minute, true);
        dialog.show();
    }

    private void updateReservation() {
        String clientName = clientNameInput.getText().toString().trim();
        String totalAmount = totalAmountInput.getText().toString().trim();
        String advanceAmount = advanceAmountInput.getText().toString().trim();

        if (TextUtils.isEmpty(clientName)) {
            clientNameInput.setError("Nom requis");
            return;
        }

        if (TextUtils.isEmpty(totalAmount)) {
            totalAmountInput.setError("Montant requis");
            return;
        }

        // Récupérer les anciennes valeurs
        double oldAdvance = currentReservation.getAdvanceAmount();
        double newAdvance = parseDouble(advanceAmount);
        String oldStatus = currentReservation.getStatus();

        // Mettre à jour l'objet
        currentReservation.setClientName(clientName);
        currentReservation.setClientPhone(clientPhoneInput.getText().toString().trim());
        currentReservation.setStartDate(startDateInput.getText().toString().trim());
        currentReservation.setEndDate(endDateInput.getText().toString().trim());
        currentReservation.setCheckInTime(checkInTimeInput.getText().toString().trim());
        currentReservation.setCheckOutTime(checkOutTimeInput.getText().toString().trim());
        currentReservation.setTotalAmount(parseDouble(totalAmount));
        currentReservation.setAdvanceAmount(newAdvance);
        currentReservation.setNotes(notesInput.getText().toString().trim());

        String newStatus;
        if (statusActive.isChecked()) {
            newStatus = "active";
        } else if (statusReserved.isChecked()) {
            newStatus = "reserved";
        } else {
            newStatus = "pending";
        }
        currentReservation.setStatus(newStatus);

        saveButton.setEnabled(false);
        saveButton.setText("Mise à jour...");

        new Thread(() -> {
            try {
                // Gérer les revenus selon les changements
                handleRevenueChanges(oldAdvance, newAdvance, oldStatus, newStatus);

                int rows = reservationDAO.updateReservation(currentReservation);

                runOnUiThread(() -> {
                    if (rows > 0) {
                        Toast.makeText(this, "Réservation mise à jour!", Toast.LENGTH_SHORT).show();
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

    private void activateReservation() {
        new AlertDialog.Builder(this)
                .setTitle("Activer la réservation")
                .setMessage("Marquer cette réservation comme 'En cours' ?")
                .setPositiveButton("Activer", (dialog, which) -> {
                    statusActive.setChecked(true);
                    updateReservation();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la réservation")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette réservation ? Les revenus associés seront également annulés.")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteReservation())
                .setNegativeButton("Annuler", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteReservation() {
        new Thread(() -> {
            try {
                // Si avance payée, créer un remboursement
                if (currentReservation.getAdvanceAmount() > 0) {
                    revenueDAO.addRevenueEntry(currentUserId, reservationId,
                            currentReservation.getAdvanceAmount(), "refund");
                }

                int rows = reservationDAO.deleteReservation(reservationId);

                runOnUiThread(() -> {
                    if (rows > 0) {
                        Toast.makeText(this, "Réservation supprimée", Toast.LENGTH_SHORT).show();
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

    private void handleRevenueChanges(double oldAdvance, double newAdvance, String oldStatus, String newStatus) {
        // Si l'avance a changé
        if (newAdvance != oldAdvance) {
            double difference = newAdvance - oldAdvance;
            if (difference > 0 && !revenueDAO.hasAdvanceRevenue(reservationId)) {
                // Nouvelle avance reçue
                revenueDAO.addRevenueEntry(currentUserId, reservationId, newAdvance, "advance");
            }
        }

        // Si le statut passe à "active"
        if (!oldStatus.equals("active") && newStatus.equals("active")) {
            if (!revenueDAO.hasCompletionRevenue(reservationId)) {
                double remaining = currentReservation.getTotalAmount() - newAdvance;
                if (newAdvance == 0) {
                    // Pas d'avance, tout le montant est ajouté
                    revenueDAO.addRevenueEntry(currentUserId, reservationId,
                            currentReservation.getTotalAmount(), "completion");
                } else {
                    // Avec avance, ajouter le reste
                    revenueDAO.addRevenueEntry(currentUserId, reservationId, remaining, "completion");
                }
            }
        }
    }

    private double parseDouble(String value) {
        try {
            return TextUtils.isEmpty(value) ? 0 : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}