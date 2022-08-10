package com.example.plantproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

public class FindIdActivity extends AppCompatActivity {
    private IntentIntegrator qrScan;
    private Button qrScanBtn,nextBtn,goLogin;
    private TextView qrResult,warningText,yourIdText,myIdText,notExistIdText;
    private DatabaseReference mDatabaseRef;
    boolean isQrOK;
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
        setContentView(R.layout.activity_find_id);

        qrScanBtn = findViewById(R.id.findId_qrScan_btn);
        nextBtn = findViewById(R.id.findId_next_btn);
        goLogin = findViewById(R.id.findId_goLogin_btn);
        qrScan = new IntentIntegrator(this);
        qrResult = findViewById(R.id.findIdQrResult);
        warningText = findViewById(R.id.findId_warning_text); // 기기에 부착된 qr코드를 스캔해주세요
        yourIdText = findViewById(R.id.findId_YourId_Text);// 당신의 아이디는
        myIdText = findViewById(R.id.findId_myId_Text); // 내 아이디 입력될 textView
        notExistIdText = findViewById(R.id.findId_notExist_id_text); // 아이디가 존재하지 않습니다.
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("plant");
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
                cnt=0;
                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("snap exists", String.valueOf(snapshot.exists()));
                        if (snapshot.exists()) {
                            for (DataSnapshot sh : snapshot.getChildren()) {
                                Log.d("둘이 같애?", String.valueOf(sh.getKey().toString().equals(qrResult.getText().toString())));
                                if (sh.getKey().toString().equals(qrResult.getText().toString())) {
                                    mDatabaseRef.child(qrResult.getText().toString()).child("UserAccount").child("emailId").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                cnt--;
                                                myIdText.setText(task.getResult().getValue().toString());
                                                yourIdText.setVisibility(View.VISIBLE);
                                                myIdText.setVisibility(View.VISIBLE);
                                                goLogin.setVisibility(View.VISIBLE);
                                                nextBtn.setVisibility(View.GONE);
                                            } else {
                                                Log.d("아이디찾기 에러", "에러");
                                            }
                                        }
                                    });
                                    Log.d("cnt1", String.valueOf(cnt));
                                } else {
                                    cnt++;
                                    Log.d("cnt2", String.valueOf(cnt));
                                    if (childNum == cnt) {
                                        Log.d("cnt3", String.valueOf(cnt));
                                        notExistIdText.setVisibility(View.VISIBLE);
                                        goLogin.setVisibility(View.VISIBLE);
                                        nextBtn.setVisibility(View.GONE);
                                    }
                                }
                            }
                        } else {
                            nextBtn.setVisibility(View.GONE);
                            notExistIdText.setVisibility(View.VISIBLE);
                            goLogin.setVisibility(View.VISIBLE);

                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                qrScanBtn.setVisibility(View.GONE);
                warningText.setVisibility(View.GONE);
            }
        });
        goLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FindIdActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    //qr스캔 함수
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //qrcode 가 없으면
            if (result.getContents() == null) {
                Toast.makeText(FindIdActivity.this, "취소!", Toast.LENGTH_SHORT).show();
                isQrOK = false;
            } else {
                //qrcode 결과가 있으면
                Toast.makeText(FindIdActivity.this, "스캔완료!", Toast.LENGTH_SHORT).show();
                try {
                    JSONObject obj = new JSONObject(result.getContents());
                    qrResult.setText(obj.getString("qr"));
                    isQrOK = true;
                    nextBtn.setVisibility(View.VISIBLE);

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
