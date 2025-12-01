package com.example.projetmobilemysql.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.projetmobilemysql.models.Reservation;

import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private DatabaseHelper dbHelper;
    private static final String TAG = "ReservationDAO";

    public ReservationDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Créer une nouvelle réservation
     */
    public long createReservation(Reservation reservation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_RES_PROPERTY_ID, reservation.getPropertyId());
        values.put(DatabaseHelper.COLUMN_RES_SAMSAR_ID, reservation.getSamsarId());
        values.put(DatabaseHelper.COLUMN_RES_START_DATE, reservation.getStartDate());
        values.put(DatabaseHelper.COLUMN_RES_END_DATE, reservation.getEndDate());
        values.put(DatabaseHelper.COLUMN_RES_STATUS, reservation.getStatus());
        values.put(DatabaseHelper.COLUMN_RES_CLIENT_NAME, reservation.getClientName());
        values.put(DatabaseHelper.COLUMN_RES_CLIENT_PHONE, reservation.getClientPhone());
        values.put(DatabaseHelper.COLUMN_RES_ADVANCE_AMOUNT, reservation.getAdvanceAmount());
        values.put(DatabaseHelper.COLUMN_RES_TOTAL_AMOUNT, reservation.getTotalAmount());
        values.put(DatabaseHelper.COLUMN_RES_NOTES, reservation.getNotes());

        long id = db.insert(DatabaseHelper.TABLE_RESERVATIONS, null, values);
        db.close();

        Log.d(TAG, "Réservation créée avec ID: " + id);
        return id;
    }

    /**
     * Récupérer une réservation par ID
     */
    public Reservation getReservationById(int reservationId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_RES_ID + " = ?";
        String[] selectionArgs = {String.valueOf(reservationId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RESERVATIONS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Reservation reservation = null;
        if (cursor != null && cursor.moveToFirst()) {
            reservation = cursorToReservation(cursor);
            cursor.close();
        }

        db.close();
        return reservation;
    }

    /**
     * Récupérer toutes les réservations
     */
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RESERVATIONS,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_RES_CREATED_AT + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                reservations.add(cursorToReservation(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return reservations;
    }

    /**
     * Récupérer les réservations d'un courtier
     */
    public List<Reservation> getReservationsBySamsar(int samsarId) {
        List<Reservation> reservations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_RES_SAMSAR_ID + " = ?";
        String[] selectionArgs = {String.valueOf(samsarId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RESERVATIONS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COLUMN_RES_START_DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                reservations.add(cursorToReservation(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return reservations;
    }

    /**
     * Récupérer les réservations d'une propriété
     */
    public List<Reservation> getReservationsByProperty(int propertyId) {
        List<Reservation> reservations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_RES_PROPERTY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(propertyId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RESERVATIONS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COLUMN_RES_START_DATE + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                reservations.add(cursorToReservation(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return reservations;
    }

    /**
     * Récupérer les réservations par statut
     */
    public List<Reservation> getReservationsByStatus(int samsarId, String status) {
        List<Reservation> reservations = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_RES_SAMSAR_ID + " = ? AND "
                + DatabaseHelper.COLUMN_RES_STATUS + " = ?";
        String[] selectionArgs = {String.valueOf(samsarId), status};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_RESERVATIONS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COLUMN_RES_START_DATE + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                reservations.add(cursorToReservation(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return reservations;
    }

    /**
     * Vérifier si une propriété est disponible pour une période
     */
    public boolean isPropertyAvailable(int propertyId, String startDate, String endDate) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_RESERVATIONS + " "
                + "WHERE " + DatabaseHelper.COLUMN_RES_PROPERTY_ID + " = ? "
                + "AND " + DatabaseHelper.COLUMN_RES_STATUS + " IN ('pending', 'reserved') "
                + "AND NOT (" + DatabaseHelper.COLUMN_RES_END_DATE + " <= ? OR "
                + DatabaseHelper.COLUMN_RES_START_DATE + " >= ?)";

        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(propertyId), startDate, endDate
        });

        boolean available = true;
        if (cursor != null && cursor.moveToFirst()) {
            available = cursor.getInt(0) == 0;
            cursor.close();
        }

        db.close();
        return available;
    }

    /**
     * Mettre à jour une réservation
     */
    public int updateReservation(Reservation reservation) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_RES_START_DATE, reservation.getStartDate());
        values.put(DatabaseHelper.COLUMN_RES_END_DATE, reservation.getEndDate());
        values.put(DatabaseHelper.COLUMN_RES_STATUS, reservation.getStatus());
        values.put(DatabaseHelper.COLUMN_RES_CLIENT_NAME, reservation.getClientName());
        values.put(DatabaseHelper.COLUMN_RES_CLIENT_PHONE, reservation.getClientPhone());
        values.put(DatabaseHelper.COLUMN_RES_ADVANCE_AMOUNT, reservation.getAdvanceAmount());
        values.put(DatabaseHelper.COLUMN_RES_TOTAL_AMOUNT, reservation.getTotalAmount());
        values.put(DatabaseHelper.COLUMN_RES_NOTES, reservation.getNotes());

        String whereClause = DatabaseHelper.COLUMN_RES_ID + " = ?";
        String[] whereArgs = {String.valueOf(reservation.getId())};

        int rows = db.update(DatabaseHelper.TABLE_RESERVATIONS, values, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Réservation mise à jour: " + rows + " ligne(s)");
        return rows;
    }

    /**
     * Changer le statut d'une réservation
     */
    public boolean updateStatus(int reservationId, String newStatus) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_RES_STATUS, newStatus);

        String whereClause = DatabaseHelper.COLUMN_RES_ID + " = ?";
        String[] whereArgs = {String.valueOf(reservationId)};

        int rows = db.update(DatabaseHelper.TABLE_RESERVATIONS, values, whereClause, whereArgs);
        db.close();

        return rows > 0;
    }

    /**
     * Supprimer une réservation
     */
    public int deleteReservation(int reservationId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_RES_ID + " = ?";
        String[] whereArgs = {String.valueOf(reservationId)};

        int rows = db.delete(DatabaseHelper.TABLE_RESERVATIONS, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Réservation supprimée: " + rows + " ligne(s)");
        return rows;
    }

    /**
     * Obtenir les statistiques des réservations
     */
    public double getTotalRevenue(int samsarId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT SUM(" + DatabaseHelper.COLUMN_RES_TOTAL_AMOUNT + ") FROM "
                + DatabaseHelper.TABLE_RESERVATIONS + " "
                + "WHERE " + DatabaseHelper.COLUMN_RES_SAMSAR_ID + " = ? "
                + "AND " + DatabaseHelper.COLUMN_RES_STATUS + " = 'reserved'";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(samsarId)});

        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }

        db.close();
        return total;
    }

    /**
     * Convertir un Cursor en objet Reservation
     */
    private Reservation cursorToReservation(Cursor cursor) {
        Reservation reservation = new Reservation();
        reservation.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_ID)));
        reservation.setPropertyId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_PROPERTY_ID)));
        reservation.setSamsarId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_SAMSAR_ID)));
        reservation.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_START_DATE)));
        reservation.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_END_DATE)));
        reservation.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_STATUS)));
        reservation.setClientName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_CLIENT_NAME)));
        reservation.setClientPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_CLIENT_PHONE)));
        reservation.setAdvanceAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_ADVANCE_AMOUNT)));
        reservation.setTotalAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_TOTAL_AMOUNT)));
        reservation.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_NOTES)));
        reservation.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_CREATED_AT)));
        reservation.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_RES_UPDATED_AT)));
        return reservation;
    }
}
