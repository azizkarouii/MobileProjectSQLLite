package com.example.projetmobilemysql.utils;

import android.content.Context;
import android.util.Log;

import com.example.projetmobilemysql.database.ReservationDAO;
import com.example.projetmobilemysql.database.RevenueHistoryDAO;
import com.example.projetmobilemysql.models.Reservation;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Utilitaire pour mettre à jour automatiquement le statut des réservations
 * et enregistrer les revenus correspondants
 */
public class ReservationStatusUpdater {
    private static final String TAG = "ReservationStatusUpdater";

    private Context context;
    private ReservationDAO reservationDAO;
    private RevenueHistoryDAO revenueDAO;
    private SimpleDateFormat dateFormatter;

    public ReservationStatusUpdater(Context context) {
        this.context = context;
        this.reservationDAO = new ReservationDAO(context);
        this.revenueDAO = new RevenueHistoryDAO(context);
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    /**
     * Mettre à jour toutes les réservations qui devraient être actives
     */
    public void updateAllReservations() {
        new Thread(() -> {
            try {
                // Date et heure actuelles
                Calendar now = Calendar.getInstance();
                String today = dateFormatter.format(now.getTime());
                String currentTime = String.format(Locale.getDefault(), "%02d:%02d",
                        now.get(Calendar.HOUR_OF_DAY),
                        now.get(Calendar.MINUTE));

                Log.d(TAG, "🔄 Vérification des réservations pour: " + today + " " + currentTime);

                // Récupérer toutes les réservations
                List<Reservation> allReservations = reservationDAO.getAllReservations();

                int updatedCount = 0;

                for (Reservation reservation : allReservations) {
                    String startDate = reservation.getStartDate();
                    String endDate = reservation.getEndDate();
                    String currentStatus = reservation.getStatus();

                    // Vérifier si la réservation devrait être active
                    boolean shouldBeActive = isDateInRange(today, startDate, endDate);

                    if (shouldBeActive && !currentStatus.equals("active")) {
                        // La réservation devrait être active mais ne l'est pas

                        // Mettre à jour le statut
                        reservation.setStatus("active");
                        reservationDAO.updateReservation(reservation);

                        // Ajouter le revenu si pas encore fait
                        if (!revenueDAO.hasCompletionRevenue(reservation.getId())) {
                            double advanceAmount = reservation.getAdvanceAmount();
                            double totalAmount = reservation.getTotalAmount();

                            if (advanceAmount > 0) {
                                // Ajouter l'avance si pas déjà enregistrée
                                if (!revenueDAO.hasAdvanceRevenue(reservation.getId())) {
                                    revenueDAO.addRevenueEntry(
                                            reservation.getSamsarId(),
                                            reservation.getId(),
                                            advanceAmount,
                                            "advance"
                                    );
                                }

                                // Ajouter le reste
                                double remaining = totalAmount - advanceAmount;
                                if (remaining > 0) {
                                    revenueDAO.addRevenueEntry(
                                            reservation.getSamsarId(),
                                            reservation.getId(),
                                            remaining,
                                            "completion"
                                    );
                                }
                            } else {
                                // Pas d'avance, ajouter tout le montant
                                revenueDAO.addRevenueEntry(
                                        reservation.getSamsarId(),
                                        reservation.getId(),
                                        totalAmount,
                                        "completion"
                                );
                            }
                        }

                        updatedCount++;
                        Log.d(TAG, "Réservation #" + reservation.getId() + " activée: " +
                                reservation.getClientName());
                    }

                    // Vérifier si la réservation est terminée
                    if (today.compareTo(endDate) > 0 && currentStatus.equals("active")) {
                        // La réservation est terminée
                        reservation.setStatus("completed");
                        reservationDAO.updateReservation(reservation);

                        Log.d(TAG, "Réservation #" + reservation.getId() + " terminée: " +
                                reservation.getClientName());
                    }
                }

                if (updatedCount > 0) {
                    Log.d(TAG, "✅ " + updatedCount + " réservation(s) activée(s)");
                }

            } catch (Exception e) {
                Log.e(TAG, "Erreur lors de la mise à jour des réservations", e);
            }
        }).start();
    }

    /**
     * Mettre à jour une réservation spécifique
     */
    public void updateReservation(int reservationId) {
        new Thread(() -> {
            try {
                Reservation reservation = reservationDAO.getReservationById(reservationId);

                if (reservation == null) return;

                String today = dateFormatter.format(Calendar.getInstance().getTime());
                String startDate = reservation.getStartDate();
                String endDate = reservation.getEndDate();
                String currentStatus = reservation.getStatus();

                boolean shouldBeActive = isDateInRange(today, startDate, endDate);

                if (shouldBeActive && !currentStatus.equals("active")) {
                    reservation.setStatus("active");
                    reservationDAO.updateReservation(reservation);

                    // Ajouter les revenus
                    if (!revenueDAO.hasCompletionRevenue(reservation.getId())) {
                        double advanceAmount = reservation.getAdvanceAmount();
                        double totalAmount = reservation.getTotalAmount();

                        if (advanceAmount > 0) {
                            if (!revenueDAO.hasAdvanceRevenue(reservation.getId())) {
                                revenueDAO.addRevenueEntry(
                                        reservation.getSamsarId(),
                                        reservation.getId(),
                                        advanceAmount,
                                        "advance"
                                );
                            }

                            double remaining = totalAmount - advanceAmount;
                            if (remaining > 0) {
                                revenueDAO.addRevenueEntry(
                                        reservation.getSamsarId(),
                                        reservation.getId(),
                                        remaining,
                                        "completion"
                                );
                            }
                        } else {
                            revenueDAO.addRevenueEntry(
                                    reservation.getSamsarId(),
                                    reservation.getId(),
                                    totalAmount,
                                    "completion"
                            );
                        }
                    }

                    Log.d(TAG, "Réservation #" + reservationId + " mise à jour");
                }

            } catch (Exception e) {
                Log.e(TAG, "Erreur mise à jour réservation #" + reservationId, e);
            }
        }).start();
    }

    /**
     * Vérifier si une date est dans une plage
     */
    private boolean isDateInRange(String date, String startDate, String endDate) {
        return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
    }
}