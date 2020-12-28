package com.codewithhamad.musicplayer;

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

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.MyHolder>{

    private Context mContext;
    static ArrayList<MusicFiles> albumFiles;

    public AlbumDetailsAdapter(Context mContext, ArrayList<MusicFiles> albumFiles) {
        this.mContext = mContext;
        AlbumDetailsAdapter.albumFiles = albumFiles;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.music_items, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {

        holder.file_name.setText(albumFiles.get(position).getTitle());
        byte[] image= getAlbumArt(albumFiles.get(position).getPath());

        if(image != null){
            Glide.with(mContext)
                    .asBitmap()
                    .load(image)
                    .into(holder.album_art);
        }
        else{
            Log.d("N", "onBindViewHolder: isNull");
            Glide.with(mContext)
                    .asBitmap()
                    .load(R.drawable.note)
                    .into(holder.album_art);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "albumDetails");
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount() {
        return albumFiles.size();
    }

    public class MyHolder extends RecyclerView.ViewHolder {

        ImageView album_art;
        TextView file_name;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            album_art= itemView.findViewById(R.id.music_file_img);
            file_name= itemView.findViewById(R.id.music_file_name);

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
