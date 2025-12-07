package com.example.projetmobilemysql.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.projetmobilemysql.models.PropertyAvailability;

import java.util.ArrayList;
import java.util.List;

public class PropertyAvailabilityDAO {
    private DatabaseHelper dbHelper;
    private static final String TAG = "PropertyAvailabilityDAO";

    public PropertyAvailabilityDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Définir ou mettre à jour la disponibilité d'une propriété pour une date
     */
    public long setAvailability(PropertyAvailability availability) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Vérifier si un enregistrement existe déjà
        PropertyAvailability existing = getAvailability(availability.getPropertyId(), availability.getDate());

        if (existing != null) {
            // Mise à jour
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_AVAIL_STATUS, availability.getStatus());
            values.put(DatabaseHelper.COLUMN_AVAIL_NOTES, availability.getNotes());

            String whereClause = DatabaseHelper.COLUMN_AVAIL_PROPERTY_ID + " = ? AND "
                    + DatabaseHelper.COLUMN_AVAIL_DATE + " = ?";
            String[] whereArgs = {
                    String.valueOf(availability.getPropertyId()),
                    availability.getDate()
            };

            int rows = db.update(DatabaseHelper.TABLE_PROPERTY_AVAILABILITY, values, whereClause, whereArgs);
            db.close();
            return rows;
        } else {
            // Insertion
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COLUMN_AVAIL_PROPERTY_ID, availability.getPropertyId());
            values.put(DatabaseHelper.COLUMN_AVAIL_DATE, availability.getDate());
            values.put(DatabaseHelper.COLUMN_AVAIL_STATUS, availability.getStatus());
            values.put(DatabaseHelper.COLUMN_AVAIL_NOTES, availability.getNotes());

            long id = db.insert(DatabaseHelper.TABLE_PROPERTY_AVAILABILITY, null, values);
            db.close();
            Log.d(TAG, "Disponibilité créée avec ID: " + id);
            return id;
        }
    }

    /**
     * Récupérer la disponibilité d'une propriété pour une date spécifique
     */
    public PropertyAvailability getAvailability(int propertyId, String date) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_AVAIL_PROPERTY_ID + " = ? AND "
                + DatabaseHelper.COLUMN_AVAIL_DATE + " = ?";
        String[] selectionArgs = {String.valueOf(propertyId), date};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PROPERTY_AVAILABILITY,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        PropertyAvailability availability = null;
        if (cursor != null && cursor.moveToFirst()) {
            availability = cursorToAvailability(cursor);
            cursor.close();
        }

        db.close();
        return availability;
    }

    /**
     * Récupérer toutes les disponibilités d'une propriété
     */
    public List<PropertyAvailability> getPropertyAvailabilities(int propertyId) {
        List<PropertyAvailability> availabilities = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_AVAIL_PROPERTY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(propertyId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PROPERTY_AVAILABILITY,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COLUMN_AVAIL_DATE + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                availabilities.add(cursorToAvailability(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return availabilities;
    }

    /**
     * Récupérer les disponibilités pour une période
     */
    public List<PropertyAvailability> getAvailabilitiesForPeriod(int propertyId, String startDate, String endDate) {
        List<PropertyAvailability> availabilities = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_AVAIL_PROPERTY_ID + " = ? AND "
                + DatabaseHelper.COLUMN_AVAIL_DATE + " >= ? AND "
                + DatabaseHelper.COLUMN_AVAIL_DATE + " <= ?";
        String[] selectionArgs = {String.valueOf(propertyId), startDate, endDate};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PROPERTY_AVAILABILITY,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COLUMN_AVAIL_DATE + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                availabilities.add(cursorToAvailability(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return availabilities;
    }

    /**
     * Vérifier si une période est disponible
     */
    public boolean isPeriodAvailable(int propertyId, String startDate, String endDate) {
        List<PropertyAvailability> availabilities = getAvailabilitiesForPeriod(propertyId, startDate, endDate);

        for (PropertyAvailability avail : availabilities) {
            if (avail.getStatus().equals("unavailable")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Supprimer la disponibilité d'une date
     */
    public int deleteAvailability(int propertyId, String date) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_AVAIL_PROPERTY_ID + " = ? AND "
                + DatabaseHelper.COLUMN_AVAIL_DATE + " = ?";
        String[] whereArgs = {String.valueOf(propertyId), date};

        int rows = db.delete(DatabaseHelper.TABLE_PROPERTY_AVAILABILITY, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Disponibilité supprimée: " + rows + " ligne(s)");
        return rows;
    }

    /**
     * Supprimer toutes les disponibilités d'une propriété
     */
    public int deletePropertyAvailabilities(int propertyId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_AVAIL_PROPERTY_ID + " = ?";
        String[] whereArgs = {String.valueOf(propertyId)};

        int rows = db.delete(DatabaseHelper.TABLE_PROPERTY_AVAILABILITY, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Disponibilités supprimées: " + rows + " ligne(s)");
        return rows;
    }

    /**
     * Convertir un Cursor en objet PropertyAvailability
     */
    private PropertyAvailability cursorToAvailability(Cursor cursor) {
        PropertyAvailability availability = new PropertyAvailability();
        availability.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVAIL_ID)));
        availability.setPropertyId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVAIL_PROPERTY_ID)));
        availability.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVAIL_DATE)));
        availability.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVAIL_STATUS)));
        availability.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVAIL_NOTES)));
        availability.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AVAIL_CREATED_AT)));
        return availability;
    }
}