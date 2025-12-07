package com.example.projetmobilemysql.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.activities.AddPropertyActivity;
import com.example.projetmobilemysql.adapters.PropertyAdapter;
import com.example.projetmobilemysql.database.PropertyDAO;
import com.example.projetmobilemysql.models.Property;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class PropertiesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddProperty;
    private LinearLayout emptyView;
    private ProgressBar progressBar;

    private PropertyDAO propertyDAO;
    private List<Property> propertyList;
    private PropertyAdapter adapter;
    private int currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_properties, container, false);

        // Initialiser DAO
        propertyDAO = new PropertyDAO(getContext());

        // Récupérer l'ID de l'utilisateur connecté
        SharedPreferences prefs = getActivity().getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        // Initialiser les vues
        recyclerView = view.findViewById(R.id.recyclerViewProperties);
        fabAddProperty = view.findViewById(R.id.fabAddProperty);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);

        // Configuration RecyclerView
        propertyList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Créer et attacher l'adapter
        adapter = new PropertyAdapter(propertyList, getContext());
        recyclerView.setAdapter(adapter);

        // Click listener sur les propriétés
        adapter.setOnPropertyClickListener(property -> {
            Toast.makeText(getContext(),
                    "Propriété: " + property.getTitle(),
                    Toast.LENGTH_SHORT).show();
            // TODO: Ouvrir PropertyDetailActivity
        });

        // IMPORTANT: Charger les propriétés APRÈS avoir configuré l'adapter
        loadProperties();

        // Listener FAB
        fabAddProperty.setOnClickListener(v -> {
            // Ouvrir AddPropertyActivity
            Intent intent = new Intent(getContext(), AddPropertyActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void loadProperties() {
        showLoading(true);

        // Charger en arrière-plan
        new Thread(() -> {
            try {
                // Récupérer toutes les propriétés (ou par courtier)
                List<Property> properties = propertyDAO.getAllProperties();

                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    propertyList.clear();
                    propertyList.addAll(properties);

                    if (propertyList.isEmpty()) {
                        showEmptyView(true);
                    } else {
                        showEmptyView(false);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(getContext(),
                                propertyList.size() + " propriétés trouvées",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(),
                            "Erreur: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void showEmptyView(boolean show) {
        if (show) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recharger les propriétés quand on revient au fragment
        loadProperties();
    }
}