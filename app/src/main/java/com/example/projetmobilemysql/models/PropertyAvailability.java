package com.example.projetmobilemysql.models;

public class PropertyAvailability {
    private int id;
    private int propertyId;
    private String date; // Format: yyyy-MM-dd
    private String status; // available, pending, unavailable
    private String notes;
    private String createdAt;

    // Constructeur vide
    public PropertyAvailability() {
    }

    // Constructeur complet
    public PropertyAvailability(int id, int propertyId, String date, String status, String notes, String createdAt) {
        this.id = id;
        this.propertyId = propertyId;
        this.date = date;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    // Constructeur sans ID
    public PropertyAvailability(int propertyId, String date, String status) {
        this.propertyId = propertyId;
        this.date = date;
        this.status = status;
    }

    // Getters et Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPropertyId() {
        return propertyId;
    }

    public void setPropertyId(int propertyId) {
        this.propertyId = propertyId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    // Méthodes utilitaires
    public String getStatusLabel() {
        switch (status) {
            case "available":
                return "Disponible";
            case "pending":
                return "En attente";
            case "unavailable":
                return "Non disponible";
            default:
                return status;
        }
    }

    public int getStatusColor() {
        switch (status) {
            case "available":
                return 0xFF43A047; // Vert
            case "pending":
                return 0xFFFB8C00; // Orange
            case "unavailable":
                return 0xFFE53935; // Rouge
            default:
                return 0xFF757575; // Gris
        }
    }

    @Override
    public String toString() {
        return "PropertyAvailability{" +
                "id=" + id +
                ", propertyId=" + propertyId +
                ", date='" + date + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}