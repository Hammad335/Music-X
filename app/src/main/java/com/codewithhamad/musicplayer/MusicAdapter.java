package com.codewithhamad.musicplayer;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MyViewHolder> {

    private Context mContext;
    static ArrayList<MusicFiles> mFiles;

    MusicAdapter(Context mContext, ArrayList<MusicFiles> mFiles){
        this.mContext= mContext;
        MusicAdapter.mFiles = mFiles;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.music_items, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int i) {

        myViewHolder.file_name.setText(mFiles.get(i).getTitle());
        byte[] image= getAlbumArt(mFiles.get(i).getPath());

        if(image != null){
            Glide.with(mContext)
                    .asBitmap()
                    .load(image)
                    .into(myViewHolder.album_art);
        }
        else{
            Glide.with(mContext)
                    .asBitmap()
                    .load(R.drawable.note)
                    .into(myViewHolder.album_art);
        }

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", i);
                mContext.startActivity(intent);
//                MyViewHolder.file_name.setTextColor(Color.GREEN);
            }
        });

        myViewHolder.parent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View view) {

                new AlertDialog.Builder(mContext).
                        setIcon(android.R.drawable.ic_delete).
                        setTitle("Are You Sure ?").
                        setMessage("Do You want to delete\n"+mFiles.get(i).getTitle()+" ?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int x) {
                                    deleteFile(i, view);
                            }
                        }).setNegativeButton("No", null).show();
                return true;
            }
        });

    }

    private void deleteFile(int i, View view) {
        Log.d("file", "deleteFile: started");
        Log.d("file", "deleteFile: "+ i);

        Uri contentUri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                Long.parseLong(mFiles.get(i).getId()));
        File file= new File(mFiles.get(i).getPath());
        boolean deleted= file.delete();  // delete your file

        if(deleted){

            mContext.getContentResolver().delete(contentUri, null, null);

            mFiles.remove(i);
            notifyItemRemoved(i);
            notifyItemRangeChanged(i, mFiles.size());
            Snackbar.make(view, "File Deleted Successfully.", Snackbar.LENGTH_SHORT).show();
        }
        else{
            Snackbar.make(view, "Deletion Failed!.", Snackbar.LENGTH_SHORT).show();

        }

    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{
        CardView parent;
        TextView file_name;
        ImageView album_art;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            file_name= itemView.findViewById(R.id.music_file_name);
            album_art= itemView.findViewById(R.id.music_file_img);
            parent= itemView.findViewById(R.id.music_items_cardview);
        }
    }

    private byte[] getAlbumArt(String uri){

        MediaMetadataRetriever retriever= new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art= retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

     void updateList(ArrayList<MusicFiles> musicFilesArrayList){
        mFiles= new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}
