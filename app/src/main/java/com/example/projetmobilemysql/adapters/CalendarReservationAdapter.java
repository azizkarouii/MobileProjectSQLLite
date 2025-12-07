package com.example.projetmobilemysql.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.models.Reservation;

import java.util.List;

public class CalendarReservationAdapter extends RecyclerView.Adapter<CalendarReservationAdapter.ViewHolder> {

    private List<Reservation> reservationList;
    private Context context;

    public CalendarReservationAdapter(List<Reservation> reservationList, Context context) {
        this.reservationList = reservationList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_calendar_reservation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);

        holder.clientNameText.setText(reservation.getClientName());
        holder.dateRangeText.setText(reservation.getStartDate() + " → " + reservation.getEndDate());
        holder.statusText.setText(reservation.getStatusLabel());

        // Couleur selon le statut
        if (reservation.getStatus().equals("reserved")) {
            holder.statusText.setTextColor(Color.parseColor("#4CAF50")); // Vert
        } else if (reservation.getStatus().equals("pending")) {
            if (reservation.getAdvanceAmount() > 0) {
                holder.statusText.setTextColor(Color.parseColor("#FB8C00")); // Orange foncé
                holder.advanceText.setVisibility(View.VISIBLE);
                holder.advanceText.setText("Avance reçue");
            } else {
                holder.statusText.setTextColor(Color.parseColor("#FFA726")); // Orange clair
                holder.advanceText.setVisibility(View.VISIBLE);
                holder.advanceText.setText("Sans avance");
            }
        } else {
            holder.statusText.setTextColor(Color.parseColor("#F44336")); // Rouge
        }
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public void updateData(List<Reservation> newList) {
        this.reservationList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView clientNameText, dateRangeText, statusText, advanceText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            clientNameText = itemView.findViewById(R.id.clientNameText);
            dateRangeText = itemView.findViewById(R.id.dateRangeText);
            statusText = itemView.findViewById(R.id.statusText);
            advanceText = itemView.findViewById(R.id.advanceText);
        }
    }
}