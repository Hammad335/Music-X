package com.codewithhamad.musicplayer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.MyHolder>{

    private Context mContext;
    private ArrayList<MusicFiles> albumFiles;
    private int previousPosition= 0;

    public AlbumAdapter(Context mContext, ArrayList<MusicFiles> albumFiles) {
        this.mContext = mContext;
        this.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.album_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {

        holder.albumName.setText(albumFiles.get(position).getAlbum());
        byte[] image= getAlbumArt(albumFiles.get(position).getPath());

        if(image != null){
            Log.d("N", "onBindViewHolder: notNull");
            Glide.with(mContext)
                    .asBitmap()
                    .load(image)
                    .into(holder.albumImage);
        }
        else{
            Log.d("N", "onBindViewHolder: isNull");
            Glide.with(mContext)
                    .asBitmap()
                    .load(R.drawable.note)
                    .into(holder.albumImage);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(mContext, AlbumDetails.class);
                intent.putExtra("albumName", albumFiles.get(position).getAlbum());
                mContext.startActivity(intent);
            }
        });

        if(position > previousPosition){
            // going from top to botton
            animate(holder, true);

        }
        else{
            // going from bottom to top
            animate(holder, false);
        }
        previousPosition= position;
    }

    // added animations to recyclerView items
    private void animate(MyHolder myHolder, boolean goesDown){

        ObjectAnimator animatorTranslateY= ObjectAnimator.ofFloat(myHolder.itemView, "translationY",
                goesDown ?150:-150, 0);
        animatorTranslateY.setDuration(1000);
        animatorTranslateY.start();
    }


    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageView albumImage;
        TextView albumName;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            albumImage= itemView.findViewById(R.id.album_image);
            albumName= itemView.findViewById(R.id.album_name);

        }
    }

    private byte[] getAlbumArt(String uri){

        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art= retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
