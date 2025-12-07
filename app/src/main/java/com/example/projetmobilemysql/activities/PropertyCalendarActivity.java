package com.example.projetmobilemysql.activities;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.adapters.CalendarReservationAdapter;
import com.example.projetmobilemysql.database.PropertyAvailabilityDAO;
import com.example.projetmobilemysql.database.ReservationDAO;
import com.example.projetmobilemysql.models.PropertyAvailability;
import com.example.projetmobilemysql.models.Reservation;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PropertyCalendarActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private CalendarView calendarView;
    private RecyclerView reservationsRecyclerView;
    private TextView statusIndicator, legendText;
    private MaterialButton btnAvailable, btnPending, btnUnavailable;

    private PropertyAvailabilityDAO availabilityDAO;
    private ReservationDAO reservationDAO;
    private CalendarReservationAdapter adapter;

    private int propertyId;
    private String propertyTitle;
    private String selectedDate;
    private List<Reservation> reservationList;

    private SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_calendar);

        // Récupérer les données de l'intent
        propertyId = getIntent().getIntExtra("property_id", -1);
        propertyTitle = getIntent().getStringExtra("property_title");

        if (propertyId == -1) {
            Toast.makeText(this, "Erreur: propriété introuvable", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser DAOs
        availabilityDAO = new PropertyAvailabilityDAO(this);
        reservationDAO = new ReservationDAO(this);

        // Initialiser les vues
        initViews();

        // Configuration
        toolbar.setTitle("Calendrier - " + propertyTitle);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Configuration du CalendarView
        setupCalendar();

        // Configuration des boutons de statut
        setupStatusButtons();

        // Charger les réservations
        loadReservations();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        calendarView = findViewById(R.id.calendarView);
        reservationsRecyclerView = findViewById(R.id.reservationsRecyclerView);
        statusIndicator = findViewById(R.id.statusIndicator);
        legendText = findViewById(R.id.legendText);
        btnAvailable = findViewById(R.id.btnAvailable);
        btnPending = findViewById(R.id.btnPending);
        btnUnavailable = findViewById(R.id.btnUnavailable);

        // Configuration RecyclerView
        reservationList = new ArrayList<>();
        adapter = new CalendarReservationAdapter(reservationList, this);
        reservationsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        reservationsRecyclerView.setAdapter(adapter);
    }

    private void setupCalendar() {
        Calendar today = Calendar.getInstance();
        selectedDate = dateFormatter.format(today.getTime());

        calendarView.setMinDate(today.getTimeInMillis());

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth);
            selectedDate = dateFormatter.format(selectedCal.getTime());

            updateStatusIndicator(selectedDate);
        });

        updateStatusIndicator(selectedDate);
    }

    private void setupStatusButtons() {
        btnAvailable.setOnClickListener(v -> updateDateStatus("available"));
        btnPending.setOnClickListener(v -> updateDateStatus("pending"));
        btnUnavailable.setOnClickListener(v -> updateDateStatus("unavailable"));
    }

    private void updateStatusIndicator(String date) {
        new Thread(() -> {
            try {
                PropertyAvailability availability = availabilityDAO.getAvailability(propertyId, date);

                // Vérifier s'il y a une réservation confirmée pour cette date
                List<Reservation> reservations = reservationDAO.getReservationsByProperty(propertyId);
                boolean hasConfirmedReservation = false;
                boolean hasPendingReservation = false;
                Reservation currentReservation = null;

                for (Reservation res : reservations) {
                    if (isDateInRange(date, res.getStartDate(), res.getEndDate())) {
                        if (res.getStatus().equals("reserved")) {
                            hasConfirmedReservation = true;
                            currentReservation = res;
                            break;
                        } else if (res.getStatus().equals("pending")) {
                            hasPendingReservation = true;
                            currentReservation = res;
                        }
                    }
                }

                String status;
                int color;
                boolean canModify = true;

                if (hasConfirmedReservation) {
                    status = "Réservé (Non disponible)";
                    color = 0xFFE53935; // Rouge
                    canModify = false;
                } else if (hasPendingReservation) {
                    Reservation finalRes = currentReservation;
                    boolean hasAdvance = finalRes != null && finalRes.getAdvanceAmount() > 0;

                    if (hasAdvance) {
                        status = "En attente avec avance (Non modifiable)";
                        color = 0xFFFB8C00; // Orange foncé
                        canModify = false;
                    } else {
                        status = "En attente sans avance (Modifiable)";
                        color = 0xFFFFA726; // Orange clair
                        canModify = true;
                    }
                } else if (availability != null && availability.getStatus().equals("unavailable")) {
                    status = "Non disponible";
                    color = 0xFFE53935; // Rouge
                } else {
                    status = "Disponible";
                    color = 0xFF43A047; // Vert
                }

                final boolean finalCanModify = canModify;
                runOnUiThread(() -> {
                    statusIndicator.setText(status);
                    statusIndicator.setTextColor(color);

                    // Activer/désactiver les boutons selon la possibilité de modification
                    btnAvailable.setEnabled(finalCanModify);
                    btnPending.setEnabled(finalCanModify);
                    btnUnavailable.setEnabled(finalCanModify);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private boolean isDateInRange(String date, String startDate, String endDate) {
        return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
    }

    private void updateDateStatus(String newStatus) {
        new Thread(() -> {
            try {
                // Vérifier s'il y a une réservation confirmée
                List<Reservation> reservations = reservationDAO.getReservationsByProperty(propertyId);
                for (Reservation res : reservations) {
                    if (isDateInRange(selectedDate, res.getStartDate(), res.getEndDate())) {
                        if (res.getStatus().equals("reserved")) {
                            runOnUiThread(() -> {
                                Toast.makeText(this,
                                        "Impossible de modifier: réservation confirmée",
                                        Toast.LENGTH_SHORT).show();
                            });
                            return;
                        } else if (res.getStatus().equals("pending") && res.getAdvanceAmount() > 0) {
                            runOnUiThread(() -> {
                                Toast.makeText(this,
                                        "Impossible de modifier: avance reçue",
                                        Toast.LENGTH_SHORT).show();
                            });
                            return;
                        }
                    }
                }

                PropertyAvailability availability = new PropertyAvailability();
                availability.setPropertyId(propertyId);
                availability.setDate(selectedDate);
                availability.setStatus(newStatus);

                availabilityDAO.setAvailability(availability);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Statut mis à jour", Toast.LENGTH_SHORT).show();
                    updateStatusIndicator(selectedDate);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadReservations() {
        new Thread(() -> {
            try {
                List<Reservation> reservations = reservationDAO.getReservationsByProperty(propertyId);

                runOnUiThread(() -> {
                    reservationList.clear();
                    reservationList.addAll(reservations);
                    adapter.notifyDataSetChanged();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }
}