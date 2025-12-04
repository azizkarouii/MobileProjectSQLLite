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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.fragments.ProfileFragment;
import com.example.projetmobilemysql.fragments.PropertiesFragment;
import com.example.projetmobilemysql.fragments.ReservationsFragment;
import com.example.projetmobilemysql.utils.TestDataHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialiser SharedPreferences
        sharedPreferences = getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);

        // Vérifier si l'utilisateur est connecté
        if (!isUserLoggedIn()) {
            navigateToLogin();
            return;
        }

        // Initialiser les vues
        toolbar = findViewById(R.id.toolbar);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Configurer la toolbar
        setSupportActionBar(toolbar);

        // Afficher le nom de l'utilisateur dans le titre
        String userName = sharedPreferences.getString("user_name", "Courtier");
        toolbar.setSubtitle("Bienvenue, " + userName);

        // Charger le fragment par défaut
        if (savedInstanceState == null) {
            loadFragment(new PropertiesFragment());
        }

        // Listener pour la navigation
        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_properties) {
                fragment = new PropertiesFragment();
            } else if (itemId == R.id.nav_reservations) {
                fragment = new ReservationsFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            return loadFragment(fragment);
        });
//        Button btnCreateTestData = new Button(this);
//        btnCreateTestData.setText("Créer données de test");
//        btnCreateTestData.setOnClickListener(v -> {
//            new Thread(() -> {
//                TestDataHelper testDataHelper = new TestDataHelper(this);
//                testDataHelper.createTestData();
//
//                runOnUiThread(() -> {
//                    Toast.makeText(this, "Données de test créées!", Toast.LENGTH_SHORT).show();
//                });
//            }).start();
//        });
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_logout) {
            showLogoutDialog();
            return true;
        } else if (itemId == R.id.action_search) {
            // TODO: Implémenter la recherche
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Déconnexion")
                .setMessage("Voulez-vous vraiment vous déconnecter ?")
                .setPositiveButton("Oui", (dialog, which) -> logout())
                .setNegativeButton("Non", null)
                .show();
    }

    private void logout() {
        // Effacer la session
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Retourner à l'écran de connexion
        navigateToLogin();
    }

    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean("is_logged_in", false);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}