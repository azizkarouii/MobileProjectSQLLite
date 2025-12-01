package com.example.projetmobilemysql.utils;


import android.content.Context;
import android.util.Log;

import com.example.projetmobilemysql.database.PropertyDAO;
import com.example.projetmobilemysql.database.ReservationDAO;
import com.example.projetmobilemysql.database.UserDAO;
import com.example.projetmobilemysql.models.Property;
import com.example.projetmobilemysql.models.Reservation;
import com.example.projetmobilemysql.models.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class TestDataHelper {
    private static final String TAG = "TestDataHelper";

    private UserDAO userDAO;
    private PropertyDAO propertyDAO;
    private ReservationDAO reservationDAO;
    private Context context;

    public TestDataHelper(Context context) {
        this.context = context;
        this.userDAO = new UserDAO(context);
        this.propertyDAO = new PropertyDAO(context);
        this.reservationDAO = new ReservationDAO(context);
    }

    /**
     * Créer des données de test complètes
     */
    public void createTestData() {
        // Créer des utilisateurs de test
        int userId1 = createTestUser("Mohamed Samsar", "mohamed@samsara.com", "123456", "98123456");
        int userId2 = createTestUser("Fatma Courtier", "fatma@samsara.com", "123456", "98765432");

        // Créer des propriétés de test
        int prop1 = createTestProperty1(userId1);
        int prop2 = createTestProperty2(userId1);
        int prop3 = createTestProperty3(userId2);

        // Ajouter des courtiers aux propriétés
        propertyDAO.addSamsarToProperty(prop1, userId1);
        propertyDAO.addSamsarToProperty(prop2, userId1);
        propertyDAO.addSamsarToProperty(prop3, userId2);

        // Créer des réservations de test
        createTestReservations(prop1, userId1);
        createTestReservations(prop2, userId1);

        Log.d(TAG, "✅ Données de test créées avec succès!");
    }

    private int createTestUser(String name, String email, String password, String phone) {
        User user = new User(name, email, password, phone);
        long userId = userDAO.createUser(user);
        Log.d(TAG, "User créé: " + name + " (ID: " + userId + ")");
        return (int) userId;
    }

    private int createTestProperty1(int userId) {
        Property property = new Property();
        property.setTitle("Villa Les Palmiers - La Marsa");
        property.setType("maison");
        property.setConfiguration("S+3");
        property.setPricePerDay(350);
        property.setPricePerWeek(2000);
        property.setPricePerMonth(7000);
        property.setDistanceBeach(200);
        property.setMaxCapacity(8);
        property.setAddress("La Marsa, Tunis");
        property.setOwnerContact("+216 98 111 222");
        property.setDescription("Magnifique villa avec vue mer panoramique, piscine privée et jardin luxuriant. Idéale pour familles.");
        property.setBathrooms(3);
        property.setPool(true);
        property.setWifi(true);
        property.setAirCondition(true);
        property.setSeaView(true);
        property.setTerrace(true);
        property.setKitchen(true);
        property.setGarage(true);
        property.setPhotos("villa1_1.jpg,villa1_2.jpg,villa1_3.jpg");
        property.setCreatedBy(userId);

        long propId = propertyDAO.createProperty(property);
        Log.d(TAG, "Propriété créée: " + property.getTitle() + " (ID: " + propId + ")");
        return (int) propId;
    }

    private int createTestProperty2(int userId) {
        Property property = new Property();
        property.setTitle("Appartement Centre Ville");
        property.setType("appartement");
        property.setConfiguration("S+2");
        property.setPricePerDay(150);
        property.setPricePerWeek(900);
        property.setPricePerMonth(3000);
        property.setDistanceBeach(5000);
        property.setMaxCapacity(5);
        property.setAddress("Avenue Habib Bourguiba, Tunis");
        property.setOwnerContact("+216 98 333 444");
        property.setDescription("Appartement moderne au cœur de la ville, proche de tous les commerces et transports.");
        property.setBathrooms(2);
        property.setPool(false);
        property.setWifi(true);
        property.setAirCondition(true);
        property.setSeaView(false);
        property.setTerrace(true);
        property.setKitchen(true);
        property.setGarage(false);
        property.setPhotos("appt1_1.jpg,appt1_2.jpg");
        property.setCreatedBy(userId);

        long propId = propertyDAO.createProperty(property);
        Log.d(TAG, "Propriété créée: " + property.getTitle() + " (ID: " + propId + ")");
        return (int) propId;
    }

    private int createTestProperty3(int userId) {
        Property property = new Property();
        property.setTitle("Maison de Plage Hammamet");
        property.setType("maison");
        property.setConfiguration("S+4");
        property.setPricePerDay(400);
        property.setPricePerWeek(2500);
        property.setPricePerMonth(8500);
        property.setDistanceBeach(50);
        property.setMaxCapacity(10);
        property.setAddress("Yasmine Hammamet");
        property.setOwnerContact("+216 98 555 666");
        property.setDescription("Grande maison familiale directement sur la plage. Parfaite pour grandes familles ou groupes d'amis.");
        property.setBathrooms(4);
        property.setPool(true);
        property.setWifi(true);
        property.setAirCondition(true);
        property.setSeaView(true);
        property.setTerrace(true);
        property.setKitchen(true);
        property.setGarage(true);
        property.setPhotos("beach1_1.jpg,beach1_2.jpg,beach1_3.jpg,beach1_4.jpg");
        property.setCreatedBy(userId);

        long propId = propertyDAO.createProperty(property);
        Log.d(TAG, "Propriété créée: " + property.getTitle() + " (ID: " + propId + ")");
        return (int) propId;
    }

    private void createTestReservations(int propertyId, int samsarId) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();

        // Réservation 1 - En cours
        Reservation res1 = new Reservation();
        res1.setPropertyId(propertyId);
        res1.setSamsarId(samsarId);
        res1.setStartDate(sdf.format(calendar.getTime()));
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        res1.setEndDate(sdf.format(calendar.getTime()));
        res1.setStatus("reserved");
        res1.setClientName("Ahmed Ben Ali");
        res1.setClientPhone("+216 98 777 888");
        res1.setAdvanceAmount(500);
        res1.setTotalAmount(2450);
        res1.setNotes("Client préfère arriver après 14h");

        long resId1 = reservationDAO.createReservation(res1);
        Log.d(TAG, "Réservation créée: " + resId1);

        // Réservation 2 - Future
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        Reservation res2 = new Reservation();
        res2.setPropertyId(propertyId);
        res2.setSamsarId(samsarId);
        res2.setStartDate(sdf.format(calendar.getTime()));
        calendar.add(Calendar.DAY_OF_MONTH, 14);
        res2.setEndDate(sdf.format(calendar.getTime()));
        res2.setStatus("pending");
        res2.setClientName("Salma Trabelsi");
        res2.setClientPhone("+216 98 999 000");
        res2.setAdvanceAmount(300);
        res2.setTotalAmount(2100);
        res2.setNotes("Demande lit bébé");

        long resId2 = reservationDAO.createReservation(res2);
        Log.d(TAG, "Réservation créée: " + resId2);
    }

    /**
     * Supprimer toutes les données de test
     */
    public void clearAllData() {
        // Note: Vous devrez ajouter des méthodes deleteAll dans les DAOs
        Log.d(TAG, "Données supprimées");
    }
}
