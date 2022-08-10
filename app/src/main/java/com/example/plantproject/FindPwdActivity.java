package com.example.plantproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class FindPwdActivity extends AppCompatActivity {
    private IntentIntegrator qrScan;
    private Button qrScanBtn,nextBtn,goLogin;
    private TextView qrResult,warningText,emailText,notExistIdText,mailCheckText;
    private EditText editEmail,editPwd,editCheckPwd;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mFirebaseAuth;
    boolean isQrOK,isPwdOK,isPwdCheckOK;
    int cnt,childNum =0;
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
        setContentView(R.layout.activity_find_pwd);
        qrScanBtn = findViewById(R.id.findPwd_qrScan_btn);
        nextBtn = findViewById(R.id.findPwd_next_btn);
        goLogin = findViewById(R.id.findPwd_goLogin_btn);

        qrResult = findViewById(R.id.findPwdQrResult);
        warningText = findViewById(R.id.findPwd_warning_text); // 기기에 부착된 qr코드를 스캔해주세요
        emailText = findViewById(R.id.findPwd_email_text);// editText 옆 '이메일' 텍스트
        editEmail = findViewById(R.id.findPwd_email_editText);
        notExistIdText = findViewById(R.id.findPwd_notExist_id_text); // 아이디가 존재하지 않습니다.
        mailCheckText = findViewById(R.id.findPwd_checkMail_text);
        editPwd = findViewById(R.id.findPwd_pwd_editText); // 비밀번호 입력란
        editCheckPwd = findViewById(R.id.findPwd_check_pwd_editText); //비밀번호 확인 입력란


        qrScan = new IntentIntegrator(this);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("plant");
        mFirebaseAuth = FirebaseAuth.getInstance();

        //plant 자식 수만큼 childNum을 플러스. 자식수를 알기 위한 이벤트
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot sh : snapshot.getChildren()){
                    childNum++;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        editEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                nextBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        qrScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qrScan.setPrompt("Scanning...");
                qrScan.initiateScan();
            }
        });

        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot sh : snapshot.getChildren()){
                                if(sh.getKey().equals(qrResult.getText().toString())){
                                    mDatabaseRef.child(qrResult.getText().toString()).child("UserAccount").child("emailId").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if(task.isSuccessful()){
                                                cnt--;
                                                //사용자가 스캔한 qr을 부모로하는 UserAccount안에 있는 email 정보가 사용자가 입력한 email과 같으면
                                                if(task.getResult().getValue().toString().equals(editEmail.getText().toString())){
                                                    mFirebaseAuth.sendPasswordResetEmail(editEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                Log.d("재설정 성공","성공");
                                                            } else {
                                                                Toast.makeText(getApplicationContext(),"재설정 실패",Toast.LENGTH_SHORT).show();
                                                                Log.d("재설정 실패","실패");
                                                            }
                                                        }
                                                    });
                                                    afterSuccessGone();
                                                } else {
                                                    afterFailGone();
                                                }
                                            } else {
                                                Toast.makeText(getApplicationContext(),"에러",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    cnt++;
                                    if (childNum == cnt) {
                                        afterFailGone();
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(getApplicationContext(),"등록되지 않은 QR코드입니다.",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
        });
        goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindPwdActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

    }
    public void afterSuccessGone(){
        warningText.setVisibility(View.GONE);
        qrScanBtn.setVisibility(View.GONE);
        emailText.setVisibility(View.GONE);
        editEmail.setVisibility(View.GONE);
        nextBtn.setVisibility(View.GONE);
        goLogin.setVisibility(View.VISIBLE);
        mailCheckText.setVisibility(View.VISIBLE);
    }
    public void afterFailGone(){
        warningText.setVisibility(View.GONE);
        qrScanBtn.setVisibility(View.GONE);
        emailText.setVisibility(View.GONE);
        editEmail.setVisibility(View.GONE);
        nextBtn.setVisibility(View.GONE);
        notExistIdText.setVisibility(View.VISIBLE);
        goLogin.setVisibility(View.VISIBLE);
    }
    //qr스캔 함수
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //qrcode 가 없으면
            if (result.getContents() == null) {
                Toast.makeText(FindPwdActivity.this, "취소!", Toast.LENGTH_SHORT).show();
                isQrOK = false;
            } else {
                //qrcode 결과가 있으면
                Toast.makeText(FindPwdActivity.this, "스캔완료!", Toast.LENGTH_SHORT).show();
                try {
                    JSONObject obj = new JSONObject(result.getContents());
                    qrResult.setText(obj.getString("qr"));
                    isQrOK = true;
                    emailText.setVisibility(View.VISIBLE);
                    editEmail.setVisibility(View.VISIBLE);
                } catch (JSONException e) {
                    e.printStackTrace();
                    qrResult.setText(result.getContents());
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
