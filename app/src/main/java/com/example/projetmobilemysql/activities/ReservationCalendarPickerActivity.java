package com.example.projetmobilemysql.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.database.PropertyAvailabilityDAO;
import com.example.projetmobilemysql.database.ReservationDAO;
import com.example.projetmobilemysql.models.PropertyAvailability;
import com.example.projetmobilemysql.models.Reservation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ReservationCalendarPickerActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private CalendarView calendarView;
    private TextView startDateText, endDateText, infoText;
    private MaterialButton confirmButton, clearButton;

    private PropertyAvailabilityDAO availabilityDAO;
    private ReservationDAO reservationDAO;

    private int propertyId;
    private String propertyTitle;
    private String startDate = null;
    private String endDate = null;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_calendar_picker);

        propertyId = getIntent().getIntExtra("property_id", -1);
        propertyTitle = getIntent().getStringExtra("property_title");

        if (propertyId == -1) {
            Toast.makeText(this, "Erreur: propriété introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        availabilityDAO = new PropertyAvailabilityDAO(this);
        reservationDAO = new ReservationDAO(this);

        initViews();

        toolbar.setTitle("Sélectionner dates - " + propertyTitle);
        toolbar.setNavigationOnClickListener(v -> finish());

        setupCalendar();

        confirmButton.setOnClickListener(v -> confirmSelection());
        clearButton.setOnClickListener(v -> clearSelection());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        calendarView = findViewById(R.id.calendarView);
        startDateText = findViewById(R.id.startDateText);
        endDateText = findViewById(R.id.endDateText);
        infoText = findViewById(R.id.infoText);
        confirmButton = findViewById(R.id.confirmButton);
        clearButton = findViewById(R.id.clearButton);
    }

    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        calendarView.setMinDate(today.getTimeInMillis());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth);
            String selectedDate = dateFormatter.format(selectedCal.getTime());

            if (startDate == null) {
                // Première sélection: date de début
                startDate = selectedDate;
                startDateText.setText("Début: " + startDate);
                endDateText.setText("Fin: (sélectionnez)");
                infoText.setText("Maintenant, sélectionnez la date de fin");
                confirmButton.setEnabled(false);
            } else if (endDate == null) {
                // Deuxième sélection: date de fin
                if (selectedDate.compareTo(startDate) < 0) {
                    Toast.makeText(this,
                            "La date de fin doit être après la date de début",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                endDate = selectedDate;
                endDateText.setText("Fin: " + endDate);

                // Vérifier la disponibilité de la période
                checkPeriodAvailability();
            } else {
                // Reset et recommencer
                startDate = selectedDate;
                endDate = null;
                startDateText.setText("Début: " + startDate);
                endDateText.setText("Fin: (sélectionnez)");
                infoText.setText("Maintenant, sélectionnez la date de fin");
                confirmButton.setEnabled(false);
            }
        });
    }

    private void checkPeriodAvailability() {
        new Thread(() -> {
            try {
                List<Reservation> reservations = reservationDAO.getReservationsByProperty(propertyId);
                boolean hasConflict = false;
                String conflictMessage = "";

                // Vérifier chaque date de la période
                Calendar current = Calendar.getInstance();
                current.setTime(dateFormatter.parse(startDate));
                Calendar end = Calendar.getInstance();
                end.setTime(dateFormatter.parse(endDate));

                while (!current.after(end)) {
                    String checkDate = dateFormatter.format(current.getTime());

                    // Vérifier les réservations
                    for (Reservation res : reservations) {
                        if (isDateInRange(checkDate, res.getStartDate(), res.getEndDate())) {
                            if (res.getStatus().equals("reserved")) {
                                hasConflict = true;
                                conflictMessage = "❌ Période déjà réservée du " +
                                        res.getStartDate() + " au " + res.getEndDate();
                                break;
                            } else if (res.getStatus().equals("pending") && res.getAdvanceAmount() > 0) {
                                hasConflict = true;
                                conflictMessage = "⚠️ Période en attente avec avance reçue";
                                break;
                            }
                        }
                    }

                    if (hasConflict) break;

                    // Vérifier la disponibilité manuelle
                    PropertyAvailability availability = availabilityDAO.getAvailability(propertyId, checkDate);
                    if (availability != null && availability.getStatus().equals("unavailable")) {
                        hasConflict = true;
                        conflictMessage = "⚠️ Certaines dates sont marquées comme non disponibles";
                        break;
                    }

                    current.add(Calendar.DAY_OF_MONTH, 1);
                }

                final boolean conflict = hasConflict;
                final String message = conflictMessage;

                runOnUiThread(() -> {
                    if (conflict) {
                        infoText.setText(message);
                        infoText.setTextColor(0xFFE53935); // Rouge
                        confirmButton.setEnabled(false);
                    } else {
                        infoText.setText("✓ Période disponible!");
                        infoText.setTextColor(0xFF43A047); // Vert
                        confirmButton.setEnabled(true);
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private boolean isDateInRange(String date, String start, String end) {
        return date.compareTo(start) >= 0 && date.compareTo(end) <= 0;
    }

    private void clearSelection() {
        startDate = null;
        endDate = null;
        startDateText.setText("Début: (sélectionnez)");
        endDateText.setText("Fin: (sélectionnez)");
        infoText.setText("Sélectionnez la date de début en cliquant sur le calendrier");
        infoText.setTextColor(0xFF757575);
        confirmButton.setEnabled(false);
    }

    private void confirmSelection() {
        if (startDate != null && endDate != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("start_date", startDate);
            resultIntent.putExtra("end_date", endDate);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}