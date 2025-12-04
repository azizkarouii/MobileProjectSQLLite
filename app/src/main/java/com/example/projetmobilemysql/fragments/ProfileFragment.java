package com.example.projetmobilemysql.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.activities.EditProfileActivity;
import com.example.projetmobilemysql.database.PropertyDAO;
import com.example.projetmobilemysql.database.ReservationDAO;
import com.example.projetmobilemysql.database.UserDAO;
import com.example.projetmobilemysql.models.User;
import com.google.android.material.button.MaterialButton;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private TextView nameText, emailText, phoneText;
    private TextView propertiesCount, reservationsCount, revenueAmount;
    private MaterialButton editButton, changePasswordButton;

    private UserDAO userDAO;
    private PropertyDAO propertyDAO;
    private ReservationDAO reservationDAO;
    private int currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialiser DAOs
        userDAO = new UserDAO(getContext());
        propertyDAO = new PropertyDAO(getContext());
        reservationDAO = new ReservationDAO(getContext());

        // Récupérer l'ID de l'utilisateur connecté
        SharedPreferences prefs = getActivity().getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        // Initialiser les vues
        nameText = view.findViewById(R.id.nameText);
        emailText = view.findViewById(R.id.emailText);
        phoneText = view.findViewById(R.id.phoneText);
        propertiesCount = view.findViewById(R.id.propertiesCount);
        reservationsCount = view.findViewById(R.id.reservationsCount);
        revenueAmount = view.findViewById(R.id.revenueAmount);
        editButton = view.findViewById(R.id.editButton);
        changePasswordButton = view.findViewById(R.id.changePasswordButton);

        // Charger les informations
        loadUserProfile();
        loadStatistics();

        // Listeners
        editButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        changePasswordButton.setOnClickListener(v -> {
            // TODO: Implémenter changement de mot de passe
            showChangePasswordDialog();
        });

        return view;
    }

    private void loadUserProfile() {
        new Thread(() -> {
            try {
                User user = userDAO.getUserById(currentUserId);

                getActivity().runOnUiThread(() -> {
                    if (user != null) {
                        nameText.setText(user.getName());
                        emailText.setText(user.getEmail());
                        phoneText.setText(user.getPhone() != null ? user.getPhone() : "Non renseigné");
                    }
                });
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void loadStatistics() {
        new Thread(() -> {
            try {
                // Compter les propriétés
                int propCount = propertyDAO.getPropertiesBySamsar(currentUserId).size();

                // Compter les réservations
                int resCount = reservationDAO.getReservationsBySamsar(currentUserId).size();

                // Calculer le revenu total
                double totalRevenue = reservationDAO.getTotalRevenue(currentUserId);

                getActivity().runOnUiThread(() -> {
                    propertiesCount.setText(String.valueOf(propCount));
                    reservationsCount.setText(String.valueOf(resCount));
                    revenueAmount.setText(String.format("%.2f TND", totalRevenue));
                });
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Erreur statistiques: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showChangePasswordDialog() {
        // Créer un dialog personnalisé pour changer le mot de passe
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);

        com.google.android.material.textfield.TextInputEditText oldPasswordInput =
                dialogView.findViewById(R.id.oldPasswordInput);
        com.google.android.material.textfield.TextInputEditText newPasswordInput =
                dialogView.findViewById(R.id.newPasswordInput);
        com.google.android.material.textfield.TextInputEditText confirmPasswordInput =
                dialogView.findViewById(R.id.confirmPasswordInput);

        builder.setView(dialogView)
                .setTitle("Changer le mot de passe")
                .setPositiveButton("Changer", (dialog, which) -> {
                    String oldPassword = oldPasswordInput.getText().toString().trim();
                    String newPassword = newPasswordInput.getText().toString().trim();
                    String confirmPassword = confirmPasswordInput.getText().toString().trim();

                    if (oldPassword.isEmpty() || newPassword.isEmpty()) {
                        Toast.makeText(getContext(), "Remplissez tous les champs", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newPassword.length() < 6) {
                        Toast.makeText(getContext(), "Minimum 6 caractères", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(getContext(), "Mots de passe différents", Toast.LENGTH_SHORT).show();
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

                getActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(),
                                "Mot de passe changé avec succès",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                "Ancien mot de passe incorrect",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(),
                            "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les stats quand on revient au fragment
        loadStatistics();
    }
}
