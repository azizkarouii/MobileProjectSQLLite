package com.example.projetmobilemysql.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.models.Property;

import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private List<Property> propertyList;
    private Context context;
    private OnPropertyClickListener listener;
    private OnCalendarClickListener calendarListener;

    public interface OnPropertyClickListener {
        void onPropertyClick(Property property);
    }

    public interface OnCalendarClickListener {
        void onCalendarClick(Property property);
    }

    public PropertyAdapter(List<Property> propertyList, Context context) {
        this.propertyList = propertyList;
        this.context = context;
    }

    public void setOnPropertyClickListener(OnPropertyClickListener listener) {
        this.listener = listener;
    }

    public void setOnCalendarClickListener(OnCalendarClickListener listener) {
        this.calendarListener = listener;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = propertyList.get(position);

        holder.titleText.setText(property.getTitle());
        holder.typeText.setText(property.getType().equals("maison") ? "Maison" : "Appartement");
        holder.addressText.setText(property.getAddress());
        holder.configText.setText(property.getConfiguration());
        holder.capacityText.setText(property.getMaxCapacity() + " personnes");
        holder.bathroomsText.setText(property.getBathrooms() + " SDB");
        holder.equipmentText.setText(property.getEquipmentSummary());
        holder.priceText.setText(String.format("%.0f TND", property.getPricePerDay()));

        // Click listener sur la propriété
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPropertyClick(property);
            }
        });

        // Click listener sur le bouton calendrier
        holder.calendarButton.setOnClickListener(v -> {
            if (calendarListener != null) {
                calendarListener.onCalendarClick(property);
            }
        });
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    public void updateData(List<Property> newList) {
        this.propertyList = newList;
        notifyDataSetChanged();
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, typeText, addressText, configText;
        TextView capacityText, bathroomsText, equipmentText, priceText;
        ImageButton calendarButton;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.titleText);
            typeText = itemView.findViewById(R.id.typeText);
            addressText = itemView.findViewById(R.id.addressText);
            configText = itemView.findViewById(R.id.configText);
            capacityText = itemView.findViewById(R.id.capacityText);
            bathroomsText = itemView.findViewById(R.id.bathroomsText);
            equipmentText = itemView.findViewById(R.id.equipmentText);
            priceText = itemView.findViewById(R.id.priceText);
            calendarButton = itemView.findViewById(R.id.calendarButton);
        }
    }
}