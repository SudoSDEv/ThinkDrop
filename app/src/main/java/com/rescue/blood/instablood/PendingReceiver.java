package com.rescue.blood.instablood;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rescue.blood.instablood.common.Common;
import com.rescue.blood.instablood.model.Request;

import java.util.ArrayList;

/**
 * Created by sayan07 on 8/4/18.
 */

public class PendingReceiver extends AppCompatActivity{

    ProgressBar pb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view);
        final RecyclerView rv=findViewById(R.id.rv2);
        pb=findViewById(R.id.pb);
        pb.setVisibility(View.VISIBLE);

        FirebaseFirestore.getInstance().collection("PendingReceivers").document(Common.user.getId()).collection("Requests")
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.getResult().size()==0) {
                    pb.setVisibility(View.GONE);
                    Toast.makeText(PendingReceiver.this, "You have no pending receivers.", Toast.LENGTH_SHORT).show();
                }
                else
                for(DocumentSnapshot document: task.getResult())
                {
                    pb.setVisibility(View.GONE);
                    Request x=document.toObject(Request.class);
                    if(!Common.pendingReceivers.contains(x)) {
                        Common.pendingReceivers.add(x);
                        ((RecieverAdapter) rv.getAdapter()).add(x);
                    }
                }
            }
        });


        Log.d("Tits",Common.pendingReceivers.size()+"");
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(new RecieverAdapter(PendingReceiver.this,rv, Common.pendingReceivers));

    }
}
