package com.example.projetmobilemysql.activities;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.database.UserDAO;
import com.example.projetmobilemysql.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText nameInput, emailInput, phoneInput;
    private MaterialButton saveButton, changePasswordButton, deleteAccountButton;
    private MaterialToolbar toolbar;

    private UserDAO userDAO;
    private int currentUserId;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialiser DAO
        userDAO = new UserDAO(this);

        // Récupérer l'ID de l'utilisateur connecté
        SharedPreferences prefs = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        // Initialiser les vues
        initViews();

        // Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        // Charger les informations de l'utilisateur
        loadUserInfo();

        // Listeners
        saveButton.setOnClickListener(v -> saveProfile());
        changePasswordButton.setOnClickListener(v -> showChangePasswordDialog());
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        nameInput = findViewById(R.id.nameInput);
        emailInput = findViewById(R.id.emailInput);
        phoneInput = findViewById(R.id.phoneInput);
        saveButton = findViewById(R.id.saveButton);
        changePasswordButton = findViewById(R.id.changePasswordButton);
        deleteAccountButton = findViewById(R.id.deleteAccountButton);
    }

    private void loadUserInfo() {
        new Thread(() -> {
            try {
                currentUser = userDAO.getUserById(currentUserId);

                runOnUiThread(() -> {
                    if (currentUser != null) {
                        nameInput.setText(currentUser.getName());
                        emailInput.setText(currentUser.getEmail());
                        phoneInput.setText(currentUser.getPhone());
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void saveProfile() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Validations
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Nom requis");
            nameInput.requestFocus();
            return;
        }

        if (name.length() < 3) {
            nameInput.setError("Nom trop court");
            nameInput.requestFocus();
            return;
        }

        // Mettre à jour l'utilisateur
        currentUser.setName(name);
        currentUser.setPhone(phone);

        saveButton.setEnabled(false);
        saveButton.setText("Enregistrement...");

        new Thread(() -> {
            try {
                int rows = userDAO.updateUser(currentUser);

                runOnUiThread(() -> {
                    saveButton.setEnabled(true);
                    saveButton.setText("Enregistrer les modifications");

                    if (rows > 0) {
                        // Mettre à jour SharedPreferences
                        SharedPreferences prefs = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("user_name", name);
                        editor.apply();

                        Toast.makeText(this, "Profil mis à jour avec succès!",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de la mise à jour",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    saveButton.setEnabled(true);
                    saveButton.setText("Enregistrer les modifications");
                    Toast.makeText(this, "Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showChangePasswordDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        TextInputEditText oldPasswordInput = dialogView.findViewById(R.id.oldPasswordInput);
        TextInputEditText newPasswordInput = dialogView.findViewById(R.id.newPasswordInput);
        TextInputEditText confirmPasswordInput = dialogView.findViewById(R.id.confirmPasswordInput);

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Changer le mot de passe")
                .setPositiveButton("Changer", (dialog, which) -> {
                    String oldPassword = oldPasswordInput.getText().toString().trim();
                    String newPassword = newPasswordInput.getText().toString().trim();
                    String confirmPassword = confirmPasswordInput.getText().toString().trim();

                    if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                        Toast.makeText(this, "Remplissez tous les champs",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(this, "Minimum 6 caractères",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(this, "Mots de passe différents",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    changePassword(oldPassword, newPassword);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void changePassword(String oldPassword, String newPassword) {
        new Thread(() -> {
            try {
                boolean success = userDAO.changePassword(currentUserId, oldPassword, newPassword);

                runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(this, "Mot de passe changé avec succès",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Ancien mot de passe incorrect",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer le compte")
                .setMessage("Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.")
                .setPositiveButton("Supprimer", (dialog, which) -> deleteAccount())
                .setNegativeButton("Annuler", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteAccount() {
        new Thread(() -> {
            try {
                int rows = userDAO.deleteUser(currentUserId);

                runOnUiThread(() -> {
                    if (rows > 0) {
                        // Effacer la session
                        SharedPreferences prefs = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
                        prefs.edit().clear().apply();

                        Toast.makeText(this, "Compte supprimé", Toast.LENGTH_SHORT).show();

                        // Retourner à l'écran de connexion
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Erreur lors de la suppression",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}