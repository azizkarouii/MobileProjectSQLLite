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
import com.example.projetmobilemysql.activities.AddReservationActivity;
import com.example.projetmobilemysql.activities.PropertyDetailActivity;
import com.example.projetmobilemysql.activities.ReservationDetailActivity;
import com.example.projetmobilemysql.adapters.ReservationAdapter;
import com.example.projetmobilemysql.database.ReservationDAO;
import com.example.projetmobilemysql.models.Reservation;
import com.example.projetmobilemysql.utils.ReservationStatusUpdater;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ReservationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddReservation;
    private TabLayout tabLayout;
    private LinearLayout emptyView;
    private ProgressBar progressBar;

    private ReservationDAO reservationDAO;
    private List<Reservation> reservationList;
    private ReservationAdapter adapter;
    private int currentUserId;
    private String currentFilter = "all"; // all, pending, reserved

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reservations, container, false);

        // Initialiser DAO
        reservationDAO = new ReservationDAO(getContext());

        // Récupérer l'ID de l'utilisateur connecté
        SharedPreferences prefs = getActivity().getSharedPreferences("SamsaraPrefs", MODE_PRIVATE);
        currentUserId = prefs.getInt("user_id", -1);

        // Initialiser les vues
        recyclerView = view.findViewById(R.id.recyclerViewReservations);
        fabAddReservation = view.findViewById(R.id.fabAddReservation);
        tabLayout = view.findViewById(R.id.tabLayout);
        emptyView = view.findViewById(R.id.emptyView);
        progressBar = view.findViewById(R.id.progressBar);

        // Configuration RecyclerView
        reservationList = new ArrayList<>();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Créer et attacher l'adapter
        adapter = new ReservationAdapter(reservationList, getContext());
        recyclerView.setAdapter(adapter);

        // Click listener sur les réservations
        adapter.setOnReservationClickListener(reservation -> {
            Toast.makeText(getContext(),
                    "Réservation: " + reservation.getClientName(),
                    Toast.LENGTH_SHORT).show();
            // TODO: Ouvrir ReservationDetailActivity
            Intent intent = new Intent(getContext(), ReservationDetailActivity.class);
            intent.putExtra("reservation_id", reservation.getId());
            startActivity(intent);
        });

        // IMPORTANT: Charger les réservations APRÈS avoir configuré l'adapter
        loadReservations();

        // Listener FAB
        fabAddReservation.setOnClickListener(v -> {
            // Ouvrir AddReservationActivity
            Intent intent = new Intent(getContext(), AddReservationActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Toutes"));
        tabLayout.addTab(tabLayout.newTab().setText("En attente"));
        tabLayout.addTab(tabLayout.newTab().setText("Confirmées"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = "all";
                        break;
                    case 1:
                        currentFilter = "pending";
                        break;
                    case 2:
                        currentFilter = "reserved";
                        break;
                }
                loadReservations();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadReservations() {
        showLoading(true);

        // Charger en arrière-plan
        new Thread(() -> {
            try {
                List<Reservation> reservations;

                if (currentFilter.equals("all")) {
                    reservations = reservationDAO.getReservationsBySamsar(currentUserId);
                } else {
                    reservations = reservationDAO.getReservationsByStatus(currentUserId, currentFilter);
                }

                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    reservationList.clear();
                    reservationList.addAll(reservations);

                    if (reservationList.isEmpty()) {
                        showEmptyView(true);
                    } else {
                        showEmptyView(false);
                        adapter.notifyDataSetChanged();

                        Toast.makeText(getContext(),
                                reservationList.size() + " réservation(s) trouvée(s)",
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
        // Mettre à jour les statuts puis recharger
        new Thread(() -> {
            ReservationStatusUpdater updater = new ReservationStatusUpdater(getContext());
            updater.updateAllReservations();

            // Attendre un peu que la mise à jour se termine
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Recharger les réservations
            getActivity().runOnUiThread(() -> {
                loadReservations();
            });
        }).start();
    }
}