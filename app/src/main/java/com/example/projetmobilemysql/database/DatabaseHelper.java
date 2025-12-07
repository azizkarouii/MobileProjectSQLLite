package com.example.projetmobilemysql.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Nom et version de la base de données
    private static final String DATABASE_NAME = "samsara.db";
    private static final int DATABASE_VERSION = 2; // Incrémenté pour la nouvelle table

    // Table USERS
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_PHONE = "phone";
    public static final String COLUMN_USER_PHOTO = "photo_url";
    public static final String COLUMN_USER_CREATED_AT = "created_at";

    // Table PROPERTIES
    public static final String TABLE_PROPERTIES = "properties";
    public static final String COLUMN_PROP_ID = "id";
    public static final String COLUMN_PROP_TITLE = "title";
    public static final String COLUMN_PROP_TYPE = "type";
    public static final String COLUMN_PROP_CONFIG = "configuration";
    public static final String COLUMN_PROP_PRICE_DAY = "price_per_day";
    public static final String COLUMN_PROP_PRICE_WEEK = "price_per_week";
    public static final String COLUMN_PROP_PRICE_MONTH = "price_per_month";
    public static final String COLUMN_PROP_DISTANCE_BEACH = "distance_beach";
    public static final String COLUMN_PROP_MAX_CAPACITY = "max_capacity";
    public static final String COLUMN_PROP_ADDRESS = "address";
    public static final String COLUMN_PROP_OWNER_CONTACT = "owner_contact";
    public static final String COLUMN_PROP_AIR_CONDITION = "air_condition";
    public static final String COLUMN_PROP_WIFI = "wifi";
    public static final String COLUMN_PROP_GARAGE = "garage";
    public static final String COLUMN_PROP_POOL = "pool";
    public static final String COLUMN_PROP_KITCHEN = "kitchen";
    public static final String COLUMN_PROP_SEA_VIEW = "sea_view";
    public static final String COLUMN_PROP_TERRACE = "terrace";
    public static final String COLUMN_PROP_BATHROOMS = "bathrooms";
    public static final String COLUMN_PROP_PHOTOS = "photos";
    public static final String COLUMN_PROP_DESCRIPTION = "description";
    public static final String COLUMN_PROP_CREATED_BY = "created_by";
    public static final String COLUMN_PROP_CREATED_AT = "created_at";
    public static final String COLUMN_PROP_UPDATED_AT = "updated_at";

    // Table RESERVATIONS
    public static final String TABLE_RESERVATIONS = "reservations";
    public static final String COLUMN_RES_ID = "id";
    public static final String COLUMN_RES_PROPERTY_ID = "property_id";
    public static final String COLUMN_RES_SAMSAR_ID = "samsar_id";
    public static final String COLUMN_RES_START_DATE = "start_date";
    public static final String COLUMN_RES_END_DATE = "end_date";
    public static final String COLUMN_RES_STATUS = "status";
    public static final String COLUMN_RES_CLIENT_NAME = "client_name";
    public static final String COLUMN_RES_CLIENT_PHONE = "client_phone";
    public static final String COLUMN_RES_ADVANCE_AMOUNT = "advance_amount";
    public static final String COLUMN_RES_TOTAL_AMOUNT = "total_amount";
    public static final String COLUMN_RES_NOTES = "notes";
    public static final String COLUMN_RES_CREATED_AT = "created_at";
    public static final String COLUMN_RES_UPDATED_AT = "updated_at";

    // Table PROPERTY_SAMSARS (relation many-to-many)
    public static final String TABLE_PROPERTY_SAMSARS = "property_samsars";
    public static final String COLUMN_PS_PROPERTY_ID = "property_id";
    public static final String COLUMN_PS_SAMSAR_ID = "samsar_id";

    // Table PROPERTY_AVAILABILITY (nouvelle table)
    public static final String TABLE_PROPERTY_AVAILABILITY = "property_availability";
    public static final String COLUMN_AVAIL_ID = "id";
    public static final String COLUMN_AVAIL_PROPERTY_ID = "property_id";
    public static final String COLUMN_AVAIL_DATE = "date";
    public static final String COLUMN_AVAIL_STATUS = "status"; // available, pending, unavailable
    public static final String COLUMN_AVAIL_NOTES = "notes";
    public static final String COLUMN_AVAIL_CREATED_AT = "created_at";

    // Requêtes de création des tables
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_USER_NAME + " TEXT NOT NULL, "
            + COLUMN_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
            + COLUMN_USER_PASSWORD + " TEXT NOT NULL, "
            + COLUMN_USER_PHONE + " TEXT, "
            + COLUMN_USER_PHOTO + " TEXT, "
            + COLUMN_USER_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_PROPERTIES = "CREATE TABLE " + TABLE_PROPERTIES + " ("
            + COLUMN_PROP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_PROP_TITLE + " TEXT NOT NULL, "
            + COLUMN_PROP_TYPE + " TEXT NOT NULL, "
            + COLUMN_PROP_CONFIG + " TEXT, "
            + COLUMN_PROP_PRICE_DAY + " REAL, "
            + COLUMN_PROP_PRICE_WEEK + " REAL, "
            + COLUMN_PROP_PRICE_MONTH + " REAL, "
            + COLUMN_PROP_DISTANCE_BEACH + " REAL, "
            + COLUMN_PROP_MAX_CAPACITY + " INTEGER, "
            + COLUMN_PROP_ADDRESS + " TEXT, "
            + COLUMN_PROP_OWNER_CONTACT + " TEXT, "
            + COLUMN_PROP_AIR_CONDITION + " INTEGER DEFAULT 0, "
            + COLUMN_PROP_WIFI + " INTEGER DEFAULT 0, "
            + COLUMN_PROP_GARAGE + " INTEGER DEFAULT 0, "
            + COLUMN_PROP_POOL + " INTEGER DEFAULT 0, "
            + COLUMN_PROP_KITCHEN + " INTEGER DEFAULT 0, "
            + COLUMN_PROP_SEA_VIEW + " INTEGER DEFAULT 0, "
            + COLUMN_PROP_TERRACE + " INTEGER DEFAULT 0, "
            + COLUMN_PROP_BATHROOMS + " INTEGER, "
            + COLUMN_PROP_PHOTOS + " TEXT, "
            + COLUMN_PROP_DESCRIPTION + " TEXT, "
            + COLUMN_PROP_CREATED_BY + " INTEGER, "
            + COLUMN_PROP_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + COLUMN_PROP_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY(" + COLUMN_PROP_CREATED_BY + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
            + ")";

    private static final String CREATE_TABLE_RESERVATIONS = "CREATE TABLE " + TABLE_RESERVATIONS + " ("
            + COLUMN_RES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_RES_PROPERTY_ID + " INTEGER NOT NULL, "
            + COLUMN_RES_SAMSAR_ID + " INTEGER NOT NULL, "
            + COLUMN_RES_START_DATE + " TEXT NOT NULL, "
            + COLUMN_RES_END_DATE + " TEXT NOT NULL, "
            + COLUMN_RES_STATUS + " TEXT DEFAULT 'pending', "
            + COLUMN_RES_CLIENT_NAME + " TEXT NOT NULL, "
            + COLUMN_RES_CLIENT_PHONE + " TEXT, "
            + COLUMN_RES_ADVANCE_AMOUNT + " REAL DEFAULT 0, "
            + COLUMN_RES_TOTAL_AMOUNT + " REAL NOT NULL, "
            + COLUMN_RES_NOTES + " TEXT, "
            + COLUMN_RES_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + COLUMN_RES_UPDATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + "FOREIGN KEY(" + COLUMN_RES_PROPERTY_ID + ") REFERENCES " + TABLE_PROPERTIES + "(" + COLUMN_PROP_ID + "), "
            + "FOREIGN KEY(" + COLUMN_RES_SAMSAR_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
            + ")";

    private static final String CREATE_TABLE_PROPERTY_SAMSARS = "CREATE TABLE " + TABLE_PROPERTY_SAMSARS + " ("
            + COLUMN_PS_PROPERTY_ID + " INTEGER NOT NULL, "
            + COLUMN_PS_SAMSAR_ID + " INTEGER NOT NULL, "
            + "PRIMARY KEY(" + COLUMN_PS_PROPERTY_ID + ", " + COLUMN_PS_SAMSAR_ID + "), "
            + "FOREIGN KEY(" + COLUMN_PS_PROPERTY_ID + ") REFERENCES " + TABLE_PROPERTIES + "(" + COLUMN_PROP_ID + "), "
            + "FOREIGN KEY(" + COLUMN_PS_SAMSAR_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_USER_ID + ")"
            + ")";

    private static final String CREATE_TABLE_PROPERTY_AVAILABILITY = "CREATE TABLE " + TABLE_PROPERTY_AVAILABILITY + " ("
            + COLUMN_AVAIL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_AVAIL_PROPERTY_ID + " INTEGER NOT NULL, "
            + COLUMN_AVAIL_DATE + " TEXT NOT NULL, "
            + COLUMN_AVAIL_STATUS + " TEXT DEFAULT 'available', "
            + COLUMN_AVAIL_NOTES + " TEXT, "
            + COLUMN_AVAIL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
            + "UNIQUE(" + COLUMN_AVAIL_PROPERTY_ID + ", " + COLUMN_AVAIL_DATE + "), "
            + "FOREIGN KEY(" + COLUMN_AVAIL_PROPERTY_ID + ") REFERENCES " + TABLE_PROPERTIES + "(" + COLUMN_PROP_ID + ") ON DELETE CASCADE"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Créer toutes les tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_PROPERTIES);
        db.execSQL(CREATE_TABLE_RESERVATIONS);
        db.execSQL(CREATE_TABLE_PROPERTY_SAMSARS);
        db.execSQL(CREATE_TABLE_PROPERTY_AVAILABILITY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Ajouter la table property_availability
            db.execSQL(CREATE_TABLE_PROPERTY_AVAILABILITY);
        }
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Activer les clés étrangères
        db.setForeignKeyConstraintsEnabled(true);
    }
}