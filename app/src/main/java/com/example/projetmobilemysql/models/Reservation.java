package com.example.projetmobilemysql.models;

public class Reservation {
    private int id;
    private int propertyId;
    private int samsarId;
    private String startDate;
    private String endDate;
    private String checkInTime;  // NOUVEAU: Heure d'entrée (format HH:mm)
    private String checkOutTime; // NOUVEAU: Heure de sortie (format HH:mm)
    private String status; // pending, reserved, active, cancelled
    private String clientName;
    private String clientPhone;
    private double advanceAmount;
    private double totalAmount;
    private String notes;
    private String createdAt;
    private String updatedAt;
    private String propertyTitle; // NOUVEAU: pour affichage

    // Constructeur vide
    public Reservation() {
    }

    // Constructeur complet
    public Reservation(int id, int propertyId, int samsarId, String startDate,
                       String endDate, String checkInTime, String checkOutTime,
                       String status, String clientName, String clientPhone,
                       double advanceAmount, double totalAmount, String notes,
                       String createdAt, String updatedAt) {
        this.id = id;
        this.propertyId = propertyId;
        this.samsarId = samsarId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.status = status;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.advanceAmount = advanceAmount;
        this.totalAmount = totalAmount;
        this.notes = notes;
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

    public String getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(String checkInTime) {
        this.checkInTime = checkInTime;
    }

    public String getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(String checkOutTime) {
        this.checkOutTime = checkOutTime;
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

    public String getPropertyTitle() {
        return propertyTitle;
    }

    public void setPropertyTitle(String propertyTitle) {
        this.propertyTitle = propertyTitle;
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
            case "active":
                return "En cours";
            case "cancelled":
                return "Annulé";
            default:
                return status;
        }
    }

    public int getStatusColor() {
        switch (status) {
            case "pending":
                return 0xFFFB8C00; // Orange
            case "reserved":
                return 0xFFE53935; // Rouge
            case "active":
                return 0xFF43A047; // Vert
            case "cancelled":
                return 0xFF757575; // Gris
            default:
                return 0xFF757575;
        }
    }

    /**
     * Vérifie si la réservation devrait être active maintenant
     */
    public boolean shouldBeActive() {
        try {
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault());
            java.util.Date now = new java.util.Date();

            String startDateTime = startDate + " " + (checkInTime != null ? checkInTime : "00:00");
            String endDateTime = endDate + " " + (checkOutTime != null ? checkOutTime : "23:59");

            java.util.Date start = dateFormat.parse(startDateTime);
            java.util.Date end = dateFormat.parse(endDateTime);

            return now.after(start) && now.before(end);
        } catch (Exception e) {
            return false;
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