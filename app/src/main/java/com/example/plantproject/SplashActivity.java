package com.example.plantproject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView gif_plant = (ImageView) findViewById(R.id.gif_plant);
        Glide.with(this).load(R.drawable.plantpot_gif).into(gif_plant);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("plant");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mFirebaseAuth.getCurrentUser() == null){
                    Log.d("SP로그인","현재없음");
                    Intent intent = new Intent(SplashActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d("SP로그인", "현재");
                    Intent intent = new Intent(SplashActivity.this, RegisterActivity.class);
                    mFirebaseAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        @Override
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                Log.d("SP현재 로그인", idToken);
                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot sh : snapshot.getChildren()) {
                                            mDatabaseRef.child(sh.getKey()).child("UserAccount").child("emailId").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.getResult().getValue().toString().equals(mFirebaseAuth.getCurrentUser().getEmail().toString())) {
                                                            intent.putExtra("qr", sh.getKey().toString());
                                                            Log.d("SPqr", intent.getStringExtra("qr"));

                                                            startActivity(intent);
                                                        } else {
                                                            Log.d("qr찾기 실패", "실패");
                                                        }
                                                    }
                                                }
                                            });
                                        }

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }

                        }
                    });
                }
            }
        },3000);
    }
}
