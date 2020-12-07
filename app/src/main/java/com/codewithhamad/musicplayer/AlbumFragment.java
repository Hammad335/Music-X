package com.codewithhamad.musicplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AlbumFragment extends Fragment {

    static RecyclerView recyclerView;
    AlbumAdapter albumAdapter;
    int x=1;

    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_album, container, false);

        recyclerView= view.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        if(!(MainActivity.albums.size() < 1)){

                albumAdapter= new AlbumAdapter(getContext(), MainActivity.albums);
                recyclerView.setAdapter(albumAdapter);
                recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        }


        return view;
    }
}