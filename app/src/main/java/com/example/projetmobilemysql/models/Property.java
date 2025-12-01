package com.example.projetmobilemysql.models;
public class Property {
    private int id;
    private String title;
    private String type; // maison/appartement
    private String configuration; // S+1, S+2...
    private double pricePerDay;
    private double pricePerWeek;
    private double pricePerMonth;
    private double distanceBeach;
    private int maxCapacity;
    private String address;
    private String ownerContact;

    // Équipements (stockés en JSON ou colonnes séparées)
    private boolean airCondition;
    private boolean wifi;
    private boolean garage;
    private boolean pool;
    private boolean kitchen;
    private boolean seaView;
    private boolean terrace;
    private int bathrooms;

    private String photos; // URLs séparées par virgule
    private String description;
    private int createdBy; // User ID du créateur
    private String createdAt;
    private String updatedAt;

    // Constructeur vide
    public Property() {
    }

    // Constructeur complet
    public Property(int id, String title, String type, String configuration,
                    double pricePerDay, double pricePerWeek, double pricePerMonth,
                    double distanceBeach, int maxCapacity, String address,
                    String ownerContact, boolean airCondition, boolean wifi,
                    boolean garage, boolean pool, boolean kitchen, boolean seaView,
                    boolean terrace, int bathrooms, String photos, String description,
                    int createdBy, String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.configuration = configuration;
        this.pricePerDay = pricePerDay;
        this.pricePerWeek = pricePerWeek;
        this.pricePerMonth = pricePerMonth;
        this.distanceBeach = distanceBeach;
        this.maxCapacity = maxCapacity;
        this.address = address;
        this.ownerContact = ownerContact;
        this.airCondition = airCondition;
        this.wifi = wifi;
        this.garage = garage;
        this.pool = pool;
        this.kitchen = kitchen;
        this.seaView = seaView;
        this.terrace = terrace;
        this.bathrooms = bathrooms;
        this.photos = photos;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public double getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(double pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    public double getPricePerWeek() {
        return pricePerWeek;
    }

    public void setPricePerWeek(double pricePerWeek) {
        this.pricePerWeek = pricePerWeek;
    }

    public double getPricePerMonth() {
        return pricePerMonth;
    }

    public void setPricePerMonth(double pricePerMonth) {
        this.pricePerMonth = pricePerMonth;
    }

    public double getDistanceBeach() {
        return distanceBeach;
    }

    public void setDistanceBeach(double distanceBeach) {
        this.distanceBeach = distanceBeach;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getOwnerContact() {
        return ownerContact;
    }

    public void setOwnerContact(String ownerContact) {
        this.ownerContact = ownerContact;
    }

    public boolean isAirCondition() {
        return airCondition;
    }

    public void setAirCondition(boolean airCondition) {
        this.airCondition = airCondition;
    }

    public boolean isWifi() {
        return wifi;
    }

    public void setWifi(boolean wifi) {
        this.wifi = wifi;
    }

    public boolean isGarage() {
        return garage;
    }

    public void setGarage(boolean garage) {
        this.garage = garage;
    }

    public boolean isPool() {
        return pool;
    }

    public void setPool(boolean pool) {
        this.pool = pool;
    }

    public boolean isKitchen() {
        return kitchen;
    }

    public void setKitchen(boolean kitchen) {
        this.kitchen = kitchen;
    }

    public boolean isSeaView() {
        return seaView;
    }

    public void setSeaView(boolean seaView) {
        this.seaView = seaView;
    }

    public boolean isTerrace() {
        return terrace;
    }

    public void setTerrace(boolean terrace) {
        this.terrace = terrace;
    }

    public int getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(int bathrooms) {
        this.bathrooms = bathrooms;
    }

    public String getPhotos() {
        return photos;
    }

    public void setPhotos(String photos) {
        this.photos = photos;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Méthodes utilitaires
    public String getEquipmentSummary() {
        StringBuilder summary = new StringBuilder();
        if (wifi) summary.append("WiFi • ");
        if (pool) summary.append("Piscine • ");
        if (airCondition) summary.append("Clim • ");
        if (seaView) summary.append("Vue mer • ");

        String result = summary.toString();
        return result.isEmpty() ? "Aucun équipement" : result.substring(0, result.length() - 2);
    }

    public int getEquipmentCount() {
        int count = 0;
        if (airCondition) count++;
        if (wifi) count++;
        if (garage) count++;
        if (pool) count++;
        if (kitchen) count++;
        if (seaView) count++;
        if (terrace) count++;
        return count;
    }

    @Override
    public String toString() {
        return "Property{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", pricePerDay=" + pricePerDay +
                ", address='" + address + '\'' +
                '}';
    }
}
