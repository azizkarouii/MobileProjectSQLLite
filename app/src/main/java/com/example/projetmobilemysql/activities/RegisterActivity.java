package com.example.projetmobilemysql.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.projetmobilemysql.R;


import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.projetmobilemysql.database.UserDAO;
import com.example.projetmobilemysql.models.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nameInput, phoneInput, emailInput, passwordInput, confirmPasswordInput;
    private MaterialButton registerButton;
    private ProgressBar progressBar;
    private TextView loginText;
    private ImageButton backButton;

    private UserDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialiser DAO
        userDAO = new UserDAO(this);

        // Initialiser les vues
        initViews();

        // Listeners
        registerButton.setOnClickListener(v -> registerUser());
        loginText.setOnClickListener(v -> finish());
        backButton.setOnClickListener(v -> finish());
    }

    private void initViews() {
        nameInput = findViewById(R.id.nameInput);
        phoneInput = findViewById(R.id.phoneInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);
        registerButton = findViewById(R.id.registerButton);
        progressBar = findViewById(R.id.progressBar);
        loginText = findViewById(R.id.loginText);
        backButton = findViewById(R.id.backButton);
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

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

        if (TextUtils.isEmpty(phone)) {
            phoneInput.setError("Téléphone requis");
            phoneInput.requestFocus();
            return;
        }

        if (phone.length() < 8) {
            phoneInput.setError("Numéro invalide");
            phoneInput.requestFocus();
            return;
        }

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

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Mots de passe différents");
            confirmPasswordInput.requestFocus();
            return;
        }

        // Afficher le chargement
        showLoading(true);

        // Inscription en arrière-plan
        new Thread(() -> {
            try {
                // Vérifier si l'email existe déjà
                if (userDAO.emailExists(email)) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        Toast.makeText(RegisterActivity.this,
                                "Cet email est déjà utilisé",
                                Toast.LENGTH_LONG).show();
                        emailInput.setError("Email déjà utilisé");
                        emailInput.requestFocus();
                    });
                    return;
                }

                // Créer l'utilisateur
                User user = new User(name, email, password, phone);
                long userId = userDAO.createUser(user);

                runOnUiThread(() -> {
                    showLoading(false);

                    if (userId > 0) {
                        // Inscription réussie
                        Toast.makeText(RegisterActivity.this,
                                "Inscription réussie ! Connectez-vous maintenant",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        // Inscription échouée
                        Toast.makeText(RegisterActivity.this,
                                "Erreur lors de l'inscription",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(RegisterActivity.this,
                            "Erreur: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            registerButton.setEnabled(false);
            registerButton.setText("");
        } else {
            progressBar.setVisibility(View.GONE);
            registerButton.setEnabled(true);
            registerButton.setText("S'inscrire");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Fermer la connexion à la base de données si nécessaire
    }
}