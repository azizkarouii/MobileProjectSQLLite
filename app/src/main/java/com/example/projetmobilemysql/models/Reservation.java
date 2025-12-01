package com.example.projetmobilemysql.models;


public class Reservation {
    private int id;
    private int propertyId;
    private int samsarId; // User ID du courtier
    private String startDate;
    private String endDate;
    private String status; // available/pending/reserved
    private String clientName;
    private String clientPhone;
    private double advanceAmount;
    private double totalAmount;
    private String notes;
    private String createdAt;
    private String updatedAt;

    // Constructeur vide
    public Reservation() {
    }

    // Constructeur complet
    public Reservation(int id, int propertyId, int samsarId, String startDate,
                       String endDate, String status, String clientName,
                       String clientPhone, double advanceAmount, double totalAmount,
                       String notes, String createdAt, String updatedAt) {
        this.id = id;
        this.propertyId = propertyId;
        this.samsarId = samsarId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.advanceAmount = advanceAmount;
        this.totalAmount = totalAmount;
        this.notes = notes;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Constructeur sans ID (pour insertion)
    public Reservation(int propertyId, int samsarId, String startDate,
                       String endDate, String clientName, String clientPhone,
                       double totalAmount) {
        this.propertyId = propertyId;
        this.samsarId = samsarId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.totalAmount = totalAmount;
        this.status = "pending";
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

    public int getSamsarId() {
        return samsarId;
    }

    public void setSamsarId(int samsarId) {
        this.samsarId = samsarId;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public double getAdvanceAmount() {
        return advanceAmount;
    }

    public void setAdvanceAmount(double advanceAmount) {
        this.advanceAmount = advanceAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
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

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Méthodes utilitaires
    public double getRemainingAmount() {
        return totalAmount - advanceAmount;
    }

    public String getStatusLabel() {
        switch (status) {
            case "pending":
                return "En attente";
            case "reserved":
                return "Réservé";
            case "available":
                return "Disponible";
            case "cancelled":
                return "Annulé";
            default:
                return status;
        }
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", propertyId=" + propertyId +
                ", clientName='" + clientName + '\'' +
                ", status='" + status + '\'' +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
