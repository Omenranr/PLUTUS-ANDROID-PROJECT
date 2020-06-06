package com.rdmn.plutus.helperapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rdmn.plutus.helperapp.Common.Common;
import com.rdmn.plutus.helperapp.Model.History;
import com.rdmn.plutus.helperapp.R;
import com.rdmn.plutus.helperapp.recyclerViewHistory.ClickListener;
import com.rdmn.plutus.helperapp.recyclerViewHistory.historyAdapter;

import java.util.ArrayList;

public class HelpHistory extends AppCompatActivity {
    FirebaseDatabase database;
    DatabaseReference blindHistory;
    RecyclerView.LayoutManager layoutManager;
    RecyclerView rvHistory;
    historyAdapter adapter;
    FirebaseAuth mAuth;
    ArrayList<History> listData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);
        initToolbar();
        initRecyclerView();

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        blindHistory = database.getReference(Common.history_helper);
        listData = new ArrayList<>();
        adapter = new historyAdapter(this, listData, new ClickListener() {
            @Override
            public void onClick(View view, int index) {

            }
        });
        rvHistory.setAdapter(adapter);
        getHistory();
    }
    private void getHistory(){
        blindHistory.child(Common.userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                    History history = postSnapshot.getValue(History.class);
                    listData.add(history);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void initRecyclerView(){
        rvHistory = findViewById(R.id.rvHistory);
        rvHistory.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        rvHistory.setLayoutManager(layoutManager);
        rvHistory.setItemAnimator(new DefaultItemAnimator());
        rvHistory.addItemDecoration(new DividerItemDecoration(getApplicationContext(),LinearLayoutManager.VERTICAL));
    }
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Help History");

        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }
}
