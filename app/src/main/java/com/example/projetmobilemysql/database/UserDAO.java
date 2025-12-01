package com.example.projetmobilemysql.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.projetmobilemysql.models.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private DatabaseHelper dbHelper;
    private static final String TAG = "UserDAO";

    public UserDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    /**
     * Créer un nouvel utilisateur
     */
    public long createUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_USER_NAME, user.getName());
        values.put(DatabaseHelper.COLUMN_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD, hashPassword(user.getPassword()));
        values.put(DatabaseHelper.COLUMN_USER_PHONE, user.getPhone());
        values.put(DatabaseHelper.COLUMN_USER_PHOTO, user.getPhotoUrl());

        long id = db.insert(DatabaseHelper.TABLE_USERS, null, values);
        db.close();

        Log.d(TAG, "User créé avec ID: " + id);
        return id;
    }

    /**
     * Connexion utilisateur
     */
    public User login(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String hashedPassword = hashPassword(password);

        String selection = DatabaseHelper.COLUMN_USER_EMAIL + " = ? AND "
                + DatabaseHelper.COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, hashedPassword};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        db.close();
        Log.d(TAG, "Login: " + (user != null ? "Succès" : "Échec"));
        return user;
    }

    /**
     * Récupérer un utilisateur par ID
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] selectionArgs = {String.valueOf(userId)};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        db.close();
        return user;
    }

    /**
     * Récupérer un utilisateur par email
     */
    public User getUserByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }

        db.close();
        return user;
    }

    /**
     * Mettre à jour un utilisateur
     */
    public int updateUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(DatabaseHelper.COLUMN_USER_NAME, user.getName());
        values.put(DatabaseHelper.COLUMN_USER_PHONE, user.getPhone());
        values.put(DatabaseHelper.COLUMN_USER_PHOTO, user.getPhotoUrl());

        String whereClause = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(user.getId())};

        int rows = db.update(DatabaseHelper.TABLE_USERS, values, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "User mis à jour: " + rows + " ligne(s)");
        return rows;
    }

    /**
     * Changer le mot de passe
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Vérifier l'ancien mot de passe
        User user = getUserById(userId);
        if (user == null || !user.getPassword().equals(hashPassword(oldPassword))) {
            db.close();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_USER_PASSWORD, hashPassword(newPassword));

        String whereClause = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};

        int rows = db.update(DatabaseHelper.TABLE_USERS, values, whereClause, whereArgs);
        db.close();

        return rows > 0;
    }

    /**
     * Supprimer un utilisateur
     */
    public int deleteUser(int userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String whereClause = DatabaseHelper.COLUMN_USER_ID + " = ?";
        String[] whereArgs = {String.valueOf(userId)};

        int rows = db.delete(DatabaseHelper.TABLE_USERS, whereClause, whereArgs);
        db.close();

        Log.d(TAG, "User supprimé: " + rows + " ligne(s)");
        return rows;
    }

    /**
     * Récupérer tous les utilisateurs
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                null,
                null,
                null,
                null,
                DatabaseHelper.COLUMN_USER_NAME + " ASC"
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                users.add(cursorToUser(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return users;
    }

    /**
     * Vérifier si un email existe déjà
     */
    public boolean emailExists(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String selection = DatabaseHelper.COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_ID},
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
     * Convertir un Cursor en objet User
     */
    private User cursorToUser(Cursor cursor) {
        User user = new User();
        user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_ID)));
        user.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME)));
        user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_EMAIL)));
        user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PASSWORD)));
        user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHONE)));
        user.setPhotoUrl(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_PHOTO)));
        user.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_CREATED_AT)));
        return user;
    }

    /**
     * Hasher un mot de passe avec SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Erreur hashage password", e);
            return password; // Fallback (pas sécurisé)
        }
    }
}
