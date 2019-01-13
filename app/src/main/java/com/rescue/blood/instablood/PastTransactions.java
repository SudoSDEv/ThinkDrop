package com.rescue.blood.instablood;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.model.PastTransaction;

import java.util.ArrayList;

/**
 * Created by sayan07 on 6/5/18.
 */

public class PastTransactions extends AppCompatActivity {
    ProgressBar pb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.past_transactions);

        pb=findViewById(R.id.pb);
        pb.setVisibility(View.VISIBLE);
        final RecyclerView recyclerView=findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(new MyAdapter(recyclerView,new ArrayList<PastTransaction>()));

        FirebaseDatabase.getInstance().getReference(Common.PAST_TRANSACTIONS+"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()==null) {
                            pb.setVisibility(View.GONE);
                            Toast.makeText(PastTransactions.this, "No past transactions", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        FirebaseDatabase.getInstance().getReference(Common.PAST_TRANSACTIONS+"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                            pb.setVisibility(View.GONE);
                            PastTransaction x = dataSnapshot.getValue(PastTransaction.class);
                            ((MyAdapter) recyclerView.getAdapter()).add(x);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

        RecyclerView recyclerView;
        ArrayList<PastTransaction> items;

        public MyAdapter(RecyclerView recyclerView, ArrayList<PastTransaction> pastTransactions) {
            this.recyclerView=recyclerView;
            items=pastTransactions;
        }

        public void add(PastTransaction x)
        {
            items.add(x);
            notifyDataSetChanged();
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(getApplicationContext()).inflate(R.layout.past_trans_card,parent,false));
        }

        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {

            if(holder!=null){
                PastTransaction x=items.get(position);
                holder.name.setText(x.getName());
                holder.phone.setText(x.getPhone());
                holder.email.setText(x.getEmail());
            }

        }

        @Override
        public int getItemCount() {
            return items.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder{

            ImageView dp;
            TextView name,phone,email;
            Button delete;

            public ViewHolder(final View itemView) {
                super(itemView);
                name=itemView.findViewById(R.id.uName);
                phone=itemView.findViewById(R.id.phone);
                email=itemView.findViewById(R.id.email);
                dp=itemView.findViewById(R.id.dp);
                delete=itemView.findViewById(R.id.delete);

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final int pos=recyclerView.getChildAdapterPosition(itemView);
                        String key=items.get(pos).getFirebaseID();
                        FirebaseDatabase.getInstance().getReference(Common.PAST_TRANSACTIONS+"/"+ FirebaseAuth.getInstance().getCurrentUser().getUid()).child(key)
                                .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                items.remove(pos);
                                notifyDataSetChanged();
                                Toast.makeText(PastTransactions.this, "Deleted", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PastTransactions.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });

            }
        }
    }
}
