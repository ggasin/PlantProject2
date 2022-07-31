package com.example.plantproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {
    //뒤로가기 두번 누르면 엑티비티 종료
    private final BackKeyHandler backKeyHandler = new BackKeyHandler(this);
    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed("'뒤로' 버튼을 두 번 누르면 종료됩니다.");
    }
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private Button loginButton,goRegisterButton;
    private EditText idText,pwdText;
    private TextView forgetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.login_btn);
        goRegisterButton = findViewById(R.id.goRegister_btn);
        forgetBtn = findViewById(R.id.forgetId_btn);
        idText = findViewById(R.id.login_edit_emailId);
        pwdText = findViewById(R.id.login_edit_pwd);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("plant");


        loginButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                    String strId = idText.getText().toString();
                    String strPwd = pwdText.getText().toString();
                    Intent intent = new Intent(LoginActivity.this , MainActivity.class);
                    mFirebaseAuth.signInWithEmailAndPassword(strId,strPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                //로그인 성공
                                mDatabaseRef.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for(DataSnapshot sh : snapshot.getChildren()){
                                            mDatabaseRef.child(sh.getKey()).child("UserAccount").child("emailId").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        if (task.getResult().getValue().toString().equals(idText.getText().toString())){
                                                            intent.putExtra("qr",sh.getKey().toString());
                                                            Toast.makeText(getApplicationContext(),"로그인에 성공하였습니다.",Toast.LENGTH_SHORT).show();
                                                            startActivity(intent);
                                                            finish();
                                                            Log.d("qr",intent.getStringExtra("qr"));

                                                        }
                                                        else{
                                                            Log.d("qr찾기 실패","실패");
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



                            }else {
                                Toast.makeText(getApplicationContext(),"아이디와 비밀번호를 다시 확인해주세요.",Toast.LENGTH_SHORT).show();
                            }

                        }
                    });


               }
            }
        );
        goRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this , RegisterActivity.class);
                startActivity(intent);

            }
        });
        forgetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"클릭", Toast.LENGTH_SHORT).show();
            }
        });


    }
}