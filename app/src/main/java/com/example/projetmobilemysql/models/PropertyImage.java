package com.example.projetmobilemysql.models;

public class PropertyImage {
    private int id;
    private int propertyId;
    private String imagePath;
    private boolean isMain;
    private int position;
    private String createdAt;

    // Constructeur vide
    public PropertyImage() {
    }

    // Constructeur avec paramètres
    public PropertyImage(int propertyId, String imagePath, boolean isMain, int position) {
        this.propertyId = propertyId;
        this.imagePath = imagePath;
        this.isMain = isMain;
        this.position = position;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isMain() {
        return isMain;
    }

    public void setMain(boolean main) {
        isMain = main;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "PropertyImage{" +
                "id=" + id +
                ", propertyId=" + propertyId +
                ", imagePath='" + imagePath + '\'' +
                ", isMain=" + isMain +
                ", position=" + position +
                '}';
    }
}