package com.example.plantproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingAutoActivity extends AppCompatActivity {
    private Button doneBtn,cancelBtn;
    private EditText soilHumEdit,tempEdit;
    private DatabaseReference mDatabaseRef;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_auto);
        doneBtn = findViewById(R.id.auto_setting_done_btn);
        cancelBtn = findViewById(R.id.auto_setting_cancel_btn);
        soilHumEdit = findViewById(R.id.auto_setting_soilHum_edit);
        tempEdit = findViewById(R.id.auto_setting_temp_edit);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("plant");
        Intent intent = getIntent();

        //데이터베이스에서 현재 자동기능을 사용할 때의 토양습도 기준값을 불러옴
        mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.OPERATION.label()).child(DbName.AUTOSTANDARD.label()).child(DbName.SOILHUM.label())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.getValue(String.class);
                soilHumEdit.setText(data);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //데이터베이스에서 현재 자동기능을 사용할 때의 온도 기준값을 불러옴
        mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.OPERATION.label()).child(DbName.AUTOSTANDARD.label()).child(DbName.TEMP.label())
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.getValue(String.class);
                tempEdit.setText(data);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //수정 완료 버튼 이벤트
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.OPERATION.label())
                        .child(DbName.AUTOSTANDARD.label()).child(DbName.SOILHUM.label()).setValue(soilHumEdit.getText().toString());
                mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.OPERATION.label())
                        .child(DbName.AUTOSTANDARD.label()).child(DbName.TEMP.label()).setValue(tempEdit.getText().toString());
                Toast.makeText(getApplicationContext(),"수정 완료하였습니다.",Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        //취소 버튼
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        //안드로이드 백버튼 막기
        return;
    }
}
