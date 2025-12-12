package com.example.projetmobilemysql.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.models.PropertyImage;

import java.io.File;
import java.util.List;

public class PropertyImageAdapter extends RecyclerView.Adapter<PropertyImageAdapter.ImageViewHolder> {

    private List<PropertyImage> imageList;
    private Context context;
    private OnImageActionListener listener;

    public interface OnImageActionListener {
        void onDeleteImage(PropertyImage image, int position);
        void onSetMainImage(PropertyImage image, int position);
        void onImageClick(PropertyImage image);
    }

    public PropertyImageAdapter(List<PropertyImage> imageList, Context context) {
        this.imageList = imageList;
        this.context = context;
    }

    public void setOnImageActionListener(OnImageActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_property_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        PropertyImage image = imageList.get(position);

        // Charger l'image
        try {
            File imgFile = new File(image.getImagePath());
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.imageView.setImageBitmap(bitmap);
            } else {
                holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
            }
        } catch (Exception e) {
            holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
        }

        // Badge "principale"
        if (image.isMain()) {
            holder.mainBadge.setVisibility(View.VISIBLE);
        } else {
            holder.mainBadge.setVisibility(View.GONE);
        }

        // Listeners
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteImage(image, position);
            }
        });

        holder.setMainButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSetMainImage(image, position);
            }
        });

        holder.imageView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onImageClick(image);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public void updateData(List<PropertyImage> newList) {
        this.imageList = newList;
        notifyDataSetChanged();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageButton deleteButton, setMainButton;
        TextView mainBadge;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.propertyImageView);
            deleteButton = itemView.findViewById(R.id.deleteImageButton);
            setMainButton = itemView.findViewById(R.id.setMainImageButton);
            mainBadge = itemView.findViewById(R.id.mainImageBadge);
        }
    }
}