package com.example.projetmobilemysql.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class RevenueHistoryDAO {
    private DatabaseHelper dbHelper;
    private static final String TAG = "RevenueHistoryDAO";

    public RevenueHistoryDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Ajouter une entrée de revenu
     * @param userId ID du samsar
     * @param reservationId ID de la réservation
     * @param amount Montant
     * @param type Type: "advance", "completion", "refund"
     */
    public long addRevenueEntry(int userId, int reservationId, double amount, String type) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_REV_USER_ID, userId);
        values.put(DatabaseHelper.COLUMN_REV_RESERVATION_ID, reservationId);
        values.put(DatabaseHelper.COLUMN_REV_AMOUNT, amount);
        values.put(DatabaseHelper.COLUMN_REV_TYPE, type);

        long id = db.insert(DatabaseHelper.TABLE_REVENUE_HISTORY, null, values);
        db.close();

        Log.d(TAG, "Revenu ajouté: " + amount + " TND (" + type + ")");
        return id;
    }

    /**
     * Calculer le revenu total d'un utilisateur
     */
    public double getTotalRevenue(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT " +
                "SUM(CASE WHEN " + DatabaseHelper.COLUMN_REV_TYPE + " IN ('advance', 'completion') " +
                "THEN " + DatabaseHelper.COLUMN_REV_AMOUNT + " ELSE 0 END) - " +
                "SUM(CASE WHEN " + DatabaseHelper.COLUMN_REV_TYPE + " = 'refund' " +
                "THEN " + DatabaseHelper.COLUMN_REV_AMOUNT + " ELSE 0 END) as total " +
                "FROM " + DatabaseHelper.TABLE_REVENUE_HISTORY + " " +
                "WHERE " + DatabaseHelper.COLUMN_REV_USER_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }

        db.close();
        return total;
    }

    /**
     * Vérifier si un revenu d'avance existe pour une réservation
     */
    public boolean hasAdvanceRevenue(int reservationId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_REV_RESERVATION_ID + " = ? AND " +
                DatabaseHelper.COLUMN_REV_TYPE + " = 'advance'";
        String[] selectionArgs = {String.valueOf(reservationId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_REVENUE_HISTORY,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();

        return exists;
    }

    /**
     * Vérifier si un revenu de complétion existe pour une réservation
     */
    public boolean hasCompletionRevenue(int reservationId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_REV_RESERVATION_ID + " = ? AND " +
                DatabaseHelper.COLUMN_REV_TYPE + " = 'completion'";
        String[] selectionArgs = {String.valueOf(reservationId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_REVENUE_HISTORY,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();

        return exists;
    }

    /**
     * Obtenir l'historique des revenus d'un utilisateur
     */
    public List<RevenueEntry> getRevenueHistory(int userId) {
        List<RevenueEntry> history = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_REV_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_REVENUE_HISTORY,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COLUMN_REV_CREATED_AT + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                RevenueEntry entry = new RevenueEntry();
                entry.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REV_ID));
                entry.userId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REV_USER_ID));
                entry.reservationId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REV_RESERVATION_ID));
                entry.amount = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REV_AMOUNT));
                entry.type = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REV_TYPE));
                entry.createdAt = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REV_CREATED_AT));
                history.add(entry);
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return history;
    }

    /**
     * Classe interne pour représenter une entrée de revenu
     */
    public static class RevenueEntry {
        public int id;
        public int userId;
        public int reservationId;
        public double amount;
        public String type;
        public String createdAt;
    }
}