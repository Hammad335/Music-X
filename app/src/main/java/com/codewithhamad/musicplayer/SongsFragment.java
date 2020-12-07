package com.codewithhamad.musicplayer;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.Comparator;

public class SongsFragment extends Fragment {

    static RecyclerView recyclerView;
    static MusicAdapter musicAdapter;
    SwipeRefreshLayout layout;

    public SongsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_songs, container, false);

        recyclerView= view.findViewById(R.id.recyclerView1);
        recyclerView.setHasFixedSize(true);


        if(!(MainActivity.musicFiles.size() < 1)){

            musicAdapter= new MusicAdapter(getContext(), MainActivity.musicFiles);
            recyclerView.setAdapter(musicAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

            Comparator<MusicFiles> newItems= new Comparator<MusicFiles>() {
                @Override
                public int compare(MusicFiles musicFiles, MusicFiles t1) {
                    return Integer.parseInt(musicFiles.getId()) - Integer.parseInt(t1.getId());
                }
            };

        }

        layout= view.findViewById(R.id.refreshLayout);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                musicAdapter.notifyDataSetChanged();
                layout.setRefreshing(false);
            }
        });

        return view;

    }


}