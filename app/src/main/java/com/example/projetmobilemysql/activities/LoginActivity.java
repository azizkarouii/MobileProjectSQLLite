package com.example.projetmobilemysql.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projetmobilemysql.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.projetmobilemysql.database.UserDAO;
import com.example.projetmobilemysql.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput, passwordInput;
    private MaterialButton loginButton;
    private ProgressBar progressBar;
    private TextView registerText, forgotPasswordText;

    private UserDAO userDAO;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialiser DAO
        userDAO = new UserDAO(this);
        sharedPreferences = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);

        // Vérifier si l'utilisateur est déjà connecté
        if (isUserLoggedIn()) {
            navigateToMain();
            return;
        }

        // Initialiser les vues
        initViews();

        // Listeners
        loginButton.setOnClickListener(v -> loginUser());
        registerText.setOnClickListener(v -> navigateToRegister());
        forgotPasswordText.setOnClickListener(v -> handleForgotPassword());
    }

    private void initViews() {
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);
        registerText = findViewById(R.id.registerText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email requis");
            emailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Email invalide");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Mot de passe requis");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Minimum 6 caractères");
            passwordInput.requestFocus();
            return;
        }

        // Afficher le chargement
        showLoading(true);

        // Connexion en arrière-plan
        new Thread(() -> {
            try {
                User user = userDAO.login(email, password);

                runOnUiThread(() -> {
                    showLoading(false);

                    if (user != null) {
                        // Connexion réussie
                        saveUserSession(user);
                        Toast.makeText(LoginActivity.this,
                                "Bienvenue " + user.getName() + " !",
                                Toast.LENGTH_SHORT).show();
                        navigateToMain();
                    } else {
                        // Connexion échouée
                        Toast.makeText(LoginActivity.this,
                                "Email ou mot de passe incorrect",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this,
                            "Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void handleForgotPassword() {
        String email = emailInput.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Entrez votre email d'abord", Toast.LENGTH_SHORT).show();
            emailInput.requestFocus();
            return;
        }

        // Vérifier si l'email existe
        new Thread(() -> {
            User user = userDAO.getUserByEmail(email);

            runOnUiThread(() -> {
                if (user != null) {
                    Toast.makeText(this,
                            "Contactez l'administrateur pour réinitialiser votre mot de passe",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this,
                            "Aucun compte avec cet email",
                            Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void saveUserSession(User user) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("user_id", user.getId());
        editor.putString("user_name", user.getName());
        editor.putString("user_email", user.getEmail());
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("is_logged_in", false);
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            loginButton.setEnabled(false);
            loginButton.setText("");
        } else {
            progressBar.setVisibility(View.GONE);
            loginButton.setEnabled(true);
            loginButton.setText("Se connecter");
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fermer la connexion à la base de données si nécessaire
    }
}