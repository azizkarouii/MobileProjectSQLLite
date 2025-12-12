package com.example.projetmobilemysql.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.projetmobilemysql.models.PropertyImage;

import java.util.ArrayList;
import java.util.List;

public class PropertyImageDAO {
    private DatabaseHelper dbHelper;
    private static final String TAG = "PropertyImageDAO";

    public PropertyImageDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Ajouter une image à une propriété
     */
    public long addImage(PropertyImage image) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_IMG_PROPERTY_ID, image.getPropertyId());
        values.put(DatabaseHelper.COLUMN_IMG_PATH, image.getImagePath());
        values.put(DatabaseHelper.COLUMN_IMG_IS_MAIN, image.isMain() ? 1 : 0);
        values.put(DatabaseHelper.COLUMN_IMG_POSITION, image.getPosition());

        long id = db.insert(DatabaseHelper.TABLE_PROPERTY_IMAGES, null, values);
        db.close();

        Log.d(TAG, "Image ajoutée avec ID: " + id);
        return id;
    }

    /**
     * Récupérer toutes les images d'une propriété
     */
    public List<PropertyImage> getPropertyImages(int propertyId) {
        List<PropertyImage> images = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_IMG_PROPERTY_ID + " = ?";
        String[] selectionArgs = {String.valueOf(propertyId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PROPERTY_IMAGES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                DatabaseHelper.COLUMN_IMG_POSITION + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                images.add(cursorToImage(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return images;
    }

    /**
     * Récupérer l'image principale d'une propriété
     */
    public PropertyImage getMainImage(int propertyId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_IMG_PROPERTY_ID + " = ? AND "
                + DatabaseHelper.COLUMN_IMG_IS_MAIN + " = 1";
        String[] selectionArgs = {String.valueOf(propertyId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_PROPERTY_IMAGES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        PropertyImage image = null;
        if (cursor != null && cursor.moveToFirst()) {
            image = cursorToImage(cursor);
            cursor.close();
        }

        db.close();
        return image;
    }

    /**
     * Définir une image comme principale
     */
    public boolean setMainImage(int imageId, int propertyId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // D'abord, retirer le flag principal de toutes les images
        ContentValues resetValues = new ContentValues();
        resetValues.put(DatabaseHelper.COLUMN_IMG_IS_MAIN, 0);

        String whereClause = DatabaseHelper.COLUMN_IMG_PROPERTY_ID + " = ?";
        String[] whereArgs = {String.valueOf(propertyId)};

        db.update(DatabaseHelper.TABLE_PROPERTY_IMAGES, resetValues, whereClause, whereArgs);

        // Ensuite, définir la nouvelle image principale
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IMG_IS_MAIN, 1);

        whereClause = DatabaseHelper.COLUMN_IMG_ID + " = ?";
        whereArgs = new String[]{String.valueOf(imageId)};

        int rows = db.update(DatabaseHelper.TABLE_PROPERTY_IMAGES, values, whereClause, whereArgs);
        db.close();

        return rows > 0;
    }

    /**
     * Supprimer une image
     */
    public int deleteImage(int imageId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_IMG_ID + " = ?";
        String[] whereArgs = {String.valueOf(imageId)};

        int rows = db.delete(DatabaseHelper.TABLE_PROPERTY_IMAGES, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Image supprimée: " + rows + " ligne(s)");
        return rows;
    }

    /**
     * Supprimer toutes les images d'une propriété
     */
    public int deletePropertyImages(int propertyId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_IMG_PROPERTY_ID + " = ?";
        String[] whereArgs = {String.valueOf(propertyId)};

        int rows = db.delete(DatabaseHelper.TABLE_PROPERTY_IMAGES, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "Images supprimées: " + rows + " ligne(s)");
        return rows;
    }

    /**
     * Mettre à jour la position d'une image
     */
    public boolean updateImagePosition(int imageId, int newPosition) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_IMG_POSITION, newPosition);

        String whereClause = DatabaseHelper.COLUMN_IMG_ID + " = ?";
        String[] whereArgs = {String.valueOf(imageId)};

        int rows = db.update(DatabaseHelper.TABLE_PROPERTY_IMAGES, values, whereClause, whereArgs);
        db.close();

        return rows > 0;
    }

    /**
     * Compter le nombre d'images d'une propriété
     */
    public int getImageCount(int propertyId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PROPERTY_IMAGES
                + " WHERE " + DatabaseHelper.COLUMN_IMG_PROPERTY_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(propertyId)});

        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        db.close();
        return count;
    }

    /**
     * Convertir un Cursor en objet PropertyImage
     */
    private PropertyImage cursorToImage(Cursor cursor) {
        PropertyImage image = new PropertyImage();
        image.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMG_ID)));
        image.setPropertyId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMG_PROPERTY_ID)));
        image.setImagePath(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMG_PATH)));
        image.setMain(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMG_IS_MAIN)) == 1);
        image.setPosition(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMG_POSITION)));
        image.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMG_CREATED_AT)));
        return image;
    }
}