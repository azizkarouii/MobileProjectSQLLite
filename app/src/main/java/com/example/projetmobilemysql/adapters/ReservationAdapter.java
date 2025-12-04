package com.example.projetmobilemysql.adapters;


import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.models.Reservation;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ReservationViewHolder> {

    private List<Reservation> reservationList;
    private Context context;
    private OnReservationClickListener listener;

    public interface OnReservationClickListener {
        void onReservationClick(Reservation reservation);
    }

    public ReservationAdapter(List<Reservation> reservationList, Context context) {
        this.reservationList = reservationList;
        this.context = context;
    }

    public void setOnReservationClickListener(OnReservationClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_reservation, parent, false);
        return new ReservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationViewHolder holder, int position) {
        Reservation reservation = reservationList.get(position);

        holder.clientNameText.setText(reservation.getClientName());
        holder.clientPhoneText.setText(reservation.getClientPhone());
        holder.startDateText.setText(reservation.getStartDate());
        holder.endDateText.setText(reservation.getEndDate());
        holder.totalAmountText.setText(String.format("%.0f TND", reservation.getTotalAmount()));
        holder.advanceAmountText.setText(String.format("%.0f TND", reservation.getAdvanceAmount()));

        // Statut avec couleur
        String status = reservation.getStatus();
        holder.statusText.setText(reservation.getStatusLabel());

        if (status.equals("reserved")) {
            holder.statusText.setTextColor(Color.parseColor("#4CAF50")); // Vert
        } else if (status.equals("pending")) {
            holder.statusText.setTextColor(Color.parseColor("#FF9800")); // Orange
        } else {
            holder.statusText.setTextColor(Color.parseColor("#F44336")); // Rouge
        }

        // Notes (si présentes)
        if (!TextUtils.isEmpty(reservation.getNotes())) {
            holder.notesText.setVisibility(View.VISIBLE);
            holder.notesText.setText("Note: " + reservation.getNotes());
        } else {
            holder.notesText.setVisibility(View.GONE);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReservationClick(reservation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservationList.size();
    }

    public void updateData(List<Reservation> newList) {
        this.reservationList = newList;
        notifyDataSetChanged();
    }

    static class ReservationViewHolder extends RecyclerView.ViewHolder {
        TextView clientNameText, clientPhoneText, statusText;
        TextView startDateText, endDateText;
        TextView totalAmountText, advanceAmountText, notesText;

        public ReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            clientNameText = itemView.findViewById(R.id.clientNameText);
            clientPhoneText = itemView.findViewById(R.id.clientPhoneText);
            statusText = itemView.findViewById(R.id.statusText);
            startDateText = itemView.findViewById(R.id.startDateText);
            endDateText = itemView.findViewById(R.id.endDateText);
            totalAmountText = itemView.findViewById(R.id.totalAmountText);
            advanceAmountText = itemView.findViewById(R.id.advanceAmountText);
            notesText = itemView.findViewById(R.id.notesText);
        }
    }
}
