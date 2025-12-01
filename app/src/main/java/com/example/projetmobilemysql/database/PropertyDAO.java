package com.example.projetmobilemysql.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.projetmobilemysql.models.Property;

import java.util.ArrayList;
import java.util.List;

public class PropertyDAO {
    private DatabaseHelper dbHelper;
    private static final String TAG = "PropertyDAO";

    public PropertyDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Créer une nouvelle propriété
     */
    public long createProperty(Property property) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_PROP_TITLE, property.getTitle());
        values.put(DatabaseHelper.COLUMN_PROP_TYPE, property.getType());
        values.put(DatabaseHelper.COLUMN_PROP_CONFIG, property.getConfiguration());
        values.put(DatabaseHelper.COLUMN_PROP_PRICE_DAY, property.getPricePerDay());
        values.put(DatabaseHelper.COLUMN_PROP_PRICE_WEEK, property.getPricePerWeek());
        values.put(DatabaseHelper.COLUMN_PROP_PRICE_MONTH, property.getPricePerMonth());
        values.put(DatabaseHelper.COLUMN_PROP_DISTANCE_BEACH, property.getDistanceBeach());
        values.put(DatabaseHelper.COLUMN_PROP_MAX_CAPACITY, property.getMaxCapacity());
        values.put(DatabaseHelper.COLUMN_PROP_ADDRESS, property.getAddress());
        values.put(DatabaseHelper.COLUMN_PROP_OWNER_CONTACT, property.getOwnerContact());
        values.put(DatabaseHelper.COLUMN_PROP_AIR_CONDITION, property.isAirCondition() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_WIFI, property.isWifi() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_GARAGE, property.isGarage() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_POOL, property.isPool() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_KITCHEN, property.isKitchen() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_SEA_VIEW, property.isSeaView() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_TERRACE, property.isTerrace() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_BATHROOMS, property.getBathrooms());
        values.put(DatabaseHelper.COLUMN_PROP_PHOTOS, property.getPhotos());
        values.put(DatabaseHelper.COLUMN_PROP_DESCRIPTION, property.getDescription());
        values.put(DatabaseHelper.COLUMN_PROP_CREATED_BY, property.getCreatedBy());

        long id = db.insert(DatabaseHelper.TABLE_PROPERTIES, null, values);
        db.close();

        Log.d(TAG, "Propriété créée avec ID: " + id);
        return id;
    }

    /**
     * Ajouter un courtier à une propriété
     */
    public boolean addSamsarToProperty(int propertyId, int samsarId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_PS_PROPERTY_ID, propertyId);
        values.put(DatabaseHelper.COLUMN_PS_SAMSAR_ID, samsarId);

        long result = db.insert(DatabaseHelper.TABLE_PROPERTY_SAMSARS, null, values);
        db.close();

        return result != -1;
    }

    /**
     * Retirer un courtier d'une propriété
     */
    public boolean removeSamsarFromProperty(int propertyId, int samsarId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_PS_PROPERTY_ID + " = ? AND "
                + DatabaseHelper.COLUMN_PS_SAMSAR_ID + " = ?";
        String[] whereArgs = {String.valueOf(propertyId), String.valueOf(samsarId)};

        int rows = db.delete(DatabaseHelper.TABLE_PROPERTY_SAMSARS, whereClause, whereArgs);
        db.close();

        return rows > 0;
    }

    /**
     * Récupérer une propriété par ID
     */
    public Property getPropertyById(int propertyId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_PROP_ID + " = ?";
        String[] selectionArgs = {String.valueOf(propertyId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PROPERTIES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        Property property = null;
        if (cursor != null && cursor.moveToFirst()) {
            property = cursorToProperty(cursor);
            cursor.close();
        }

        db.close();
        return property;
    }

    /**
     * Récupérer toutes les propriétés
     */
    public List<Property> getAllProperties() {
        List<Property> properties = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PROPERTIES,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_PROP_CREATED_AT + " DESC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                properties.add(cursorToProperty(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return properties;
    }

    /**
     * Récupérer les propriétés d'un courtier
     */
    public List<Property> getPropertiesBySamsar(int samsarId) {
        List<Property> properties = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT p.* FROM " + DatabaseHelper.TABLE_PROPERTIES + " p "
                + "INNER JOIN " + DatabaseHelper.TABLE_PROPERTY_SAMSARS + " ps "
                + "ON p." + DatabaseHelper.COLUMN_PROP_ID + " = ps." + DatabaseHelper.COLUMN_PS_PROPERTY_ID + " "
                + "WHERE ps." + DatabaseHelper.COLUMN_PS_SAMSAR_ID + " = ? "
                + "ORDER BY p." + DatabaseHelper.COLUMN_PROP_CREATED_AT + " DESC";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(samsarId)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                properties.add(cursorToProperty(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return properties;
    }

    /**
     * Rechercher des propriétés
     */
    public List<Property> searchProperties(String type, String config, double maxPrice, String address) {
        List<Property> properties = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        List<String> conditions = new ArrayList<>();
        List<String> args = new ArrayList<>();

        if (type != null && !type.isEmpty()) {
            conditions.add(DatabaseHelper.COLUMN_PROP_TYPE + " = ?");
            args.add(type);
        }

        if (config != null && !config.isEmpty()) {
            conditions.add(DatabaseHelper.COLUMN_PROP_CONFIG + " = ?");
            args.add(config);
        }

        if (maxPrice > 0) {
            conditions.add(DatabaseHelper.COLUMN_PROP_PRICE_DAY + " <= ?");
            args.add(String.valueOf(maxPrice));
        }

        if (address != null && !address.isEmpty()) {
            conditions.add(DatabaseHelper.COLUMN_PROP_ADDRESS + " LIKE ?");
            args.add("%" + address + "%");
        }

        String selection = conditions.isEmpty() ? null : String.join(" AND ", conditions);
        String[] selectionArgs = args.isEmpty() ? null : args.toArray(new String[0]);

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PROPERTIES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COLUMN_PROP_PRICE_DAY + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                properties.add(cursorToProperty(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return properties;
    }

    /**
     * Mettre à jour une propriété
     */
    public int updateProperty(Property property) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_PROP_TITLE, property.getTitle());
        values.put(DatabaseHelper.COLUMN_PROP_TYPE, property.getType());
        values.put(DatabaseHelper.COLUMN_PROP_CONFIG, property.getConfiguration());
        values.put(DatabaseHelper.COLUMN_PROP_PRICE_DAY, property.getPricePerDay());
        values.put(DatabaseHelper.COLUMN_PROP_PRICE_WEEK, property.getPricePerWeek());
        values.put(DatabaseHelper.COLUMN_PROP_PRICE_MONTH, property.getPricePerMonth());
        values.put(DatabaseHelper.COLUMN_PROP_DISTANCE_BEACH, property.getDistanceBeach());
        values.put(DatabaseHelper.COLUMN_PROP_MAX_CAPACITY, property.getMaxCapacity());
        values.put(DatabaseHelper.COLUMN_PROP_ADDRESS, property.getAddress());
        values.put(DatabaseHelper.COLUMN_PROP_OWNER_CONTACT, property.getOwnerContact());
        values.put(DatabaseHelper.COLUMN_PROP_AIR_CONDITION, property.isAirCondition() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_WIFI, property.isWifi() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_GARAGE, property.isGarage() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_POOL, property.isPool() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_KITCHEN, property.isKitchen() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_SEA_VIEW, property.isSeaView() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_TERRACE, property.isTerrace() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_PROP_BATHROOMS, property.getBathrooms());
        values.put(DatabaseHelper.COLUMN_PROP_PHOTOS, property.getPhotos());
        values.put(DatabaseHelper.COLUMN_PROP_DESCRIPTION, property.getDescription());

        String whereClause = DatabaseHelper.COLUMN_PROP_ID + " = ?";
        String[] whereArgs = {String.valueOf(property.getId())};

        int rows = db.update(DatabaseHelper.TABLE_PROPERTIES, values, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Propriété mise à jour: " + rows + " ligne(s)");
        return rows;
    }

    /**
     * Supprimer une propriété
     */
    public int deleteProperty(int propertyId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Supprimer d'abord les relations
        String whereClausePS = DatabaseHelper.COLUMN_PS_PROPERTY_ID + " = ?";
        String[] whereArgsPS = {String.valueOf(propertyId)};
        db.delete(DatabaseHelper.TABLE_PROPERTY_SAMSARS, whereClausePS, whereArgsPS);

        // Supprimer la propriété
        String whereClause = DatabaseHelper.COLUMN_PROP_ID + " = ?";
        String[] whereArgs = {String.valueOf(propertyId)};
        int rows = db.delete(DatabaseHelper.TABLE_PROPERTIES, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Propriété supprimée: " + rows + " ligne(s)");
        return rows;
    }

    /**
     * Convertir un Cursor en objet Property
     */
    private Property cursorToProperty(Cursor cursor) {
        Property property = new Property();
        property.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_ID)));
        property.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_TITLE)));
        property.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_TYPE)));
        property.setConfiguration(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_CONFIG)));
        property.setPricePerDay(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_PRICE_DAY)));
        property.setPricePerWeek(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_PRICE_WEEK)));
        property.setPricePerMonth(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_PRICE_MONTH)));
        property.setDistanceBeach(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_DISTANCE_BEACH)));
        property.setMaxCapacity(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_MAX_CAPACITY)));
        property.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_ADDRESS)));
        property.setOwnerContact(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_OWNER_CONTACT)));
        property.setAirCondition(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_AIR_CONDITION)) == 1);
        property.setWifi(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_WIFI)) == 1);
        property.setGarage(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_GARAGE)) == 1);
        property.setPool(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_POOL)) == 1);
        property.setKitchen(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_KITCHEN)) == 1);
        property.setSeaView(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_SEA_VIEW)) == 1);
        property.setTerrace(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_TERRACE)) == 1);
        property.setBathrooms(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_BATHROOMS)));
        property.setPhotos(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_PHOTOS)));
        property.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_DESCRIPTION)));
        property.setCreatedBy(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_CREATED_BY)));
        property.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_CREATED_AT)));
        property.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PROP_UPDATED_AT)));
        return property;
    }
}
