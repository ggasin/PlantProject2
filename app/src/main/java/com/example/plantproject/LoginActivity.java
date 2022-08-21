package com.example.plantproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
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

import java.util.regex.Pattern;

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
    private TextView forgetIdBtn,forgetPwdBtn;
    //db이름 enum으로 저장. 나중에 변경 용이하도록
    public enum DbName {
        BUTTON("button"), OPERATION("operation"), USERACCOUNT("UserAccount"),
        EMAILID("emailId"),NAME("name"),NICKNAME("nickName"),TEL("tel"),PWD("pwd"),
        QR("qr"),AUTO("auto"),NONAUTO("nonauto"),LED("LED"),WATER("water"), COOLER("cooler"),
        SENSORS("sensors"),HUM("Hum"),TEMP("Temp"),SOILHUM("soil_hum"),AUTOSTANDARD("autoStandard");
        private final String label;
        DbName(String label){
            this.label = label;
        }
        public String label() {
            return label;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        loginButton = findViewById(R.id.login_btn);
        goRegisterButton = findViewById(R.id.goRegister_btn);
        forgetIdBtn = findViewById(R.id.forgetId_text_btn);
        forgetPwdBtn = findViewById(R.id.forgetPwd_text_btn);
        idText = findViewById(R.id.login_edit_emailId);
        pwdText = findViewById(R.id.login_edit_pwd);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("plant");
        Pattern pattern = Patterns.EMAIL_ADDRESS;

        //로그인 버튼 리스너
        loginButton.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   String strId = idText.getText().toString();
                   String strPwd = pwdText.getText().toString();
                   Intent intent = new Intent(LoginActivity.this , MainActivity.class);
                   if(pattern.matcher(strId).matches()&&strPwd.length()>5){
                       mFirebaseAuth.signInWithEmailAndPassword(strId,strPwd).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                           @Override
                           public void onComplete(@NonNull Task<AuthResult> task) {
                               if(task.isSuccessful()){
                                   //로그인 성공
                                   mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                       @Override
                                       public void onDataChange(@NonNull DataSnapshot snapshot) {
                                           for(DataSnapshot sh : snapshot.getChildren()){
                                               mDatabaseRef.child(sh.getKey()).child("UserAccount").child("emailId").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                                   @Override
                                                   public void onComplete(@NonNull Task<DataSnapshot> task) {
                                                       if (task.isSuccessful()) {
                                                           if (task.getResult().getValue().toString().equals(idText.getText().toString())){
                                                               intent.putExtra("qr",sh.getKey().toString());
                                                               mDatabaseRef.child(sh.getKey()).child("UserAccount").child("pwd").setValue(pwdText.getText().toString());
                                                               //비밀번호 재설정 이후에 데이터베이스에 재설정된 비밀번호를 넣을 방법을 찾지 못해서 로그인할 때 db내 pwd를 재설정하는 방식을 채택.
                                                               Toast.makeText(getApplicationContext(),"로그인에 성공하였습니다.",Toast.LENGTH_SHORT).show();
                                                               Log.d("qr",intent.getStringExtra("qr"));
                                                               startActivity(intent);
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
                                   Toast.makeText(getApplicationContext(),"존재하지 않는 아이디거나 비밀번호가 틀렸습니다.",Toast.LENGTH_SHORT).show();
                               }
                           }
                       });
                   } else {
                       Toast.makeText(getApplicationContext(),"아이디와 비밀번호를 다시 확인해주세요.",Toast.LENGTH_SHORT).show();
                   }
               }
           }
        );
        //회원가입 버튼 리스너
        goRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mFirebaseAuth.getCurrentUser()==null){
                    Log.d("LO현재 로그인","null");
                }
                Intent intent = new Intent(LoginActivity.this , RegisterActivity.class);
                startActivity(intent);

            }
        });
        //아이디 찾기 (아직 구현 x)
        forgetIdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,FindIdActivity.class);
                startActivity(intent);
            }
        });
        forgetPwdBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,FindPwdActivity.class);
                startActivity(intent);
            }
        });



    }
}