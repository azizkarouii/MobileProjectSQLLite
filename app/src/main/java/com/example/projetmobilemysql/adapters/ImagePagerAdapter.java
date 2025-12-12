package com.example.projetmobilemysql.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projetmobilemysql.R;
import com.example.projetmobilemysql.models.PropertyImage;

import java.io.File;
import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

    private List<PropertyImage> imageList;
    private Context context;

    public ImagePagerAdapter(List<PropertyImage> imageList, Context context) {
        this.imageList = imageList;
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fullscreen_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        PropertyImage image = imageList.get(position);

        // Charger l'image en arrière-plan
        new Thread(() -> {
            try {
                File imgFile = new File(image.getImagePath());
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                    holder.imageView.post(() -> {
                        holder.imageView.setImageBitmap(bitmap);
                    });
                } else {
                    holder.imageView.post(() -> {
                        holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
                    });
                }
            } catch (Exception e) {
                holder.imageView.post(() -> {
                    holder.imageView.setImageResource(R.drawable.ic_image_placeholder);
                });
            }
        }).start();
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.fullscreenImageView);
        }
    }
}