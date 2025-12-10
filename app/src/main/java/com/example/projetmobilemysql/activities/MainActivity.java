package com.example.projetmobilemysql.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.fragments.ProfileFragment;
import com.example.projetmobilemysql.fragments.PropertiesFragment;
import com.example.projetmobilemysql.fragments.ReservationsFragment;
import com.example.projetmobilemysql.utils.ReservationStatusUpdater;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private BottomNavigationView bottomNavigation;
    private SharedPreferences sharedPreferences;
    private Fragment currentFragment;
    private MenuItem searchMenuItem;

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

        // Mettre à jour automatiquement les statuts des réservations
        new ReservationStatusUpdater(this).updateAllReservations();

        // Charger le fragment par défaut
        if (savedInstanceState == null) {
            currentFragment = new PropertiesFragment();
            loadFragment(currentFragment);
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
    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            currentFragment = fragment;
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();

            // Mettre à jour la visibilité du bouton de recherche
            updateSearchVisibility();

            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        searchMenuItem = menu.findItem(R.id.action_search);

        // Configurer le SearchView
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("Rechercher...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Recherche en temps réel
                if (newText.length() >= 2) {
                    performSearch(newText);
                } else if (newText.isEmpty()) {
                    // Réinitialiser la recherche
                    resetSearch();
                }
                return true;
            }
        });

        // Mettre à jour la visibilité initiale
        updateSearchVisibility();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_logout) {
            showLogoutDialog();
            return true;
        } else if (itemId == R.id.action_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Mettre à jour la visibilité du bouton de recherche selon le fragment actif
     */
    private void updateSearchVisibility() {
        if (searchMenuItem != null) {
            if (currentFragment instanceof ProfileFragment) {
                // Cacher le bouton de recherche dans le profil
                searchMenuItem.setVisible(false);
            } else {
                // Afficher le bouton de recherche dans Propriétés et Réservations
                searchMenuItem.setVisible(true);
            }
        }
    }

    /**
     * Effectuer la recherche selon le fragment actif
     */
    private void performSearch(String query) {
        if (currentFragment instanceof PropertiesFragment) {
            ((PropertiesFragment) currentFragment).searchProperties(query);
        } else if (currentFragment instanceof ReservationsFragment) {
            ((ReservationsFragment) currentFragment).searchReservations(query);
        }
    }

    /**
     * Réinitialiser la recherche
     */
    private void resetSearch() {
        if (currentFragment instanceof PropertiesFragment) {
            ((PropertiesFragment) currentFragment).resetSearch();
        } else if (currentFragment instanceof ReservationsFragment) {
            ((ReservationsFragment) currentFragment).resetSearch();
        }
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