package com.example.plantproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {
    // 뒤로가기 이벤트 핸들러 변수
    private final BackKeyHandler backKeyHandler = new BackKeyHandler(this);
    //뒤로가기 두번 누르면 종료
    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed("'뒤로' 버튼을 두 번 누르면 종료됩니다.");
    }
    //센서데이터 변수
    int soil_hum=1;
    int hum=1;
    int temp=1;
    String qr;
    //자동 on/off 버튼 boolean 변수
    boolean auto_water_on = false; // false면 꺼져있는 상태
    boolean auto_light_on = false;
    boolean auto_wind_on = false;
    //수동
    boolean water_on = false;
    boolean light_on = false; // 조명이 켜져있는지 꺼져있는지 알기 위한 변수. false는 꺼져있는 상태
    boolean wind_on = false; // 공기순환 모터가 꺼져있는지 알기 위한 변수.
    //데이터베이스
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth mFirebaseAuth;
    //xml 변수
    private ToggleButton waterToggle,lightToggle,windToggle;
    private TextView waterText,lightText,windText,sh_data_tv,h_data_tv,t_data_tv,plant_text;
    private Button handWaterBtn,handLightBtn,handWindBtn;
    //db이름 enum으로 저장. 나중에 변경 용이하도록
    public enum DbName {
        BUTTON("button"), OPERATION("operation"), USERACCOUNT("UserAccount"),
        EMAILID("emailId"),NAME("name"),TEL("tel"),PWD("pwd"),
        QR("qr"),AUTO("auto"),NONAUTO("nonauto"),LED("LED"),WATER("water"), COOLER("cooler"),
        SENSORS("sensors"),HUM("Hum"),TEMP("Temp"),SOILHUM("soil_hum");
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
        setContentView(R.layout.activity_main);

        //자동 ON/OFF 버튼 및 텍스트 변수
        waterToggle = findViewById(R.id.water_auto_btn);
        lightToggle = findViewById(R.id.light_auto_btn);
        windToggle = findViewById(R.id.wind_auto_btn);
        waterText = findViewById(R.id.water_auto_text);
        lightText = findViewById(R.id.light_auto_text);
        windText = findViewById(R.id.wind_auto_text);

        //센서 데이터 텍스트뷰 변수
        sh_data_tv = findViewById(R.id.soil_humanity_data);
        h_data_tv = findViewById(R.id.humanity_data);
        t_data_tv = findViewById(R.id.temperature_data);
        plant_text = findViewById(R.id.plant_text);

        //수동 버튼 변수
        handWaterBtn = findViewById(R.id.hand_water_btn);
        handLightBtn = findViewById(R.id.hand_light_btn);
        handWindBtn = findViewById(R.id.hand_wind_btn);
        Intent intent = getIntent();
        //데이터베이스 변수
        database = FirebaseDatabase.getInstance();
        mDatabaseRef = database.getReference("plant").child(intent.getStringExtra("qr"));
        mFirebaseAuth = FirebaseAuth.getInstance();

        TextView test = findViewById(R.id.login_id_test);
        TextView test2 = findViewById(R.id.textView2);
        test.setText(mFirebaseAuth.getCurrentUser().getEmail().toString());




        //앱이 실행하면서 한번만 처리. 데이터베이스의 값에 맞게 앱 상태 초기화
        //자동 물 조작 버튼 초기화
       mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.WATER.label()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String db_water_auto = snapshot.getValue(String.class);
                if(db_water_auto.equals("ON")){
                    waterToggle.setChecked(true);
                    waterText.setText("물 ON");
                } else {
                    waterToggle.setChecked(false);
                    waterText.setText("물 OFF");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
       //수동 물조작 버튼 초기화
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.WATER.label()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String db_water = snapshot.getValue(String.class);
                if(db_water.equals("ON")){
                    waterToggle.setEnabled(false);
                    handWaterBtn.setText("물 끄기");
                    handWaterBtn.setTextColor(Color.WHITE);
                    handWaterBtn.setBackground(getResources().getDrawable(R.drawable.hand_on));
                } else {
                    waterToggle.setEnabled(true);
                    handWaterBtn.setText("물 주기");
                    handWaterBtn.setTextColor(Color.BLACK);
                    handWaterBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //자동 조명 조작버튼 초기화
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.LED.label()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String db_light_auto = snapshot.getValue(String.class);
                if(db_light_auto.equals("ON")){
                    lightToggle.setChecked(true);
                    lightText.setText("조명 ON");
                } else {
                    lightToggle.setChecked(false);
                    lightText.setText("조명 OFF");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //수동 조명 조작버튼 초기화
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.LED.label()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String db_light = snapshot.getValue(String.class);
                if(db_light.equals("ON")){
                    lightToggle.setEnabled(false);
                    handLightBtn.setText("조명 끄기");
                    handLightBtn.setTextColor(Color.WHITE);
                    handLightBtn.setBackground(getResources().getDrawable(R.drawable.hand_on));
                } else {
                    lightToggle.setEnabled(true);
                    handLightBtn.setText("조명 켜기");
                    handLightBtn.setTextColor(Color.BLACK);
                    handLightBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //자동 쿨러 조작버튼 초기화
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.COOLER.label()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String db_wind_auto = snapshot.getValue(String.class);
                if(db_wind_auto.equals("ON")){
                    windToggle.setChecked(true);
                    windText.setText("바람 ON");
                } else {
                    windToggle.setChecked(false);
                    windText.setText("바람 OFF");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //수동 쿨러 조작버튼 초기화
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.COOLER.label()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String db_wind = snapshot.getValue(String.class);
                if(db_wind.equals("ON")){
                    windToggle.setEnabled(false);
                    handWindBtn.setText("공기 순환 종료");
                    handWindBtn.setTextColor(Color.WHITE);
                    handWindBtn.setBackground(getResources().getDrawable(R.drawable.hand_on));
                } else {
                    windToggle.setEnabled(true);
                    handWindBtn.setText("공기 순환 실행");
                    handWindBtn.setTextColor(Color.BLACK);
                    handWindBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //이부분 필요한지 체크하기 8/1
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.SENSORS.label()).child(DbName.SOILHUM.label()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                soil_hum =(int)snapshot.getValue(Integer.class);
                setSpeechBubble(plant_text);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //이부분 필요한지 체크하기 8/1
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.SENSORS.label()).child(DbName.TEMP.label()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                temp =(int)snapshot.getValue(Integer.class);
                setSpeechBubble(plant_text);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //토양습도 데이터를 데이터베이스로부터 가져오기
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.SENSORS.label()).child(DbName.SOILHUM.label()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                soil_hum = (int)snapshot.getValue(Integer.class);
                sh_data_tv.setText(soil_hum +"%");
                setSpeechBubble(plant_text);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //습도 데이터를 데이터베이스로부터 가져오기
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.SENSORS.label()).child(DbName.HUM.label()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hum = (int)snapshot.getValue(Integer.class);
                h_data_tv.setText(hum +"%");
                setSpeechBubble(plant_text);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //온도 데이터를 데이터베이스로부터 가져오기
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.SENSORS.label()).child(DbName.TEMP.label()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                temp = (int)snapshot.getValue(Integer.class);
                t_data_tv.setText(temp +"º");
                setSpeechBubble(plant_text);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //자동 ON/OFF 토글버튼 이벤트 코드
        waterToggle.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean ischecked) {
                        String waterAutoText;
                        if(ischecked && auto_water_on == false){
                            waterAutoText = "물 ON";
                            auto_water_on = true;
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.WATER.label()).setValue("ON");
                            waterText.setText(waterAutoText);
                        } else {
                            waterAutoText = "물 OFF";
                            auto_water_on = false;
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.WATER.label()).setValue("OFF");
                            waterText.setText(waterAutoText);
                        }
                    }
                }
        );
        lightToggle.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean ischecked) {
                        String lightAutoText;
                        if(ischecked && auto_light_on == false){
                            lightAutoText = "조명 ON";
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.LED.label()).setValue("ON");
                            lightText.setText(lightAutoText);
                            auto_light_on = true;
                        }
                        else {
                            lightAutoText = "조명 OFF";
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.LED.label()).setValue("OFF");
                            lightText.setText(lightAutoText);
                            auto_light_on = false;
                        }
                    }
                }
        );

        windToggle.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean ischecked) {
                        String windAutoText;
                        if(ischecked && auto_wind_on == false){
                            windAutoText = "바람 ON";
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.COOLER.label()).setValue("ON");
                            windText.setText(windAutoText);
                            auto_wind_on = true;
                        }
                        else {
                            windAutoText = "바람 OFF";
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.COOLER.label()).setValue("OFF");
                            windText.setText(windAutoText);
                            auto_wind_on = false;
                        }
                    }
                }
        );

        //수동 버튼 이벤트
        handWaterBtn.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        //만약 물이 안 나오고 있으면
                        if(water_on==false ){
                            if(auto_water_on == true){
                                Toast.makeText(getApplicationContext(),"자동 물주기를 해제하고 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"물주기를 실행합니다.", Toast.LENGTH_LONG).show();
                                water_on=true;
                                waterToggle.setEnabled(false);
                                mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.WATER.label()).setValue("ON");
                                handWaterBtn.setText("물 끄기");
                                handWaterBtn.setTextColor(Color.WHITE);
                                handWaterBtn.setBackground(getResources().getDrawable(R.drawable.hand_on));
                            }
                        }
                        //만약 물이 나오고 있으면
                        else {
                            Toast.makeText(getApplicationContext(),"물주기를 종료합니다.", Toast.LENGTH_LONG).show();
                            water_on=false;
                            waterToggle.setEnabled(true);
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.WATER.label()).setValue("OFF");
                            handWaterBtn.setText("물 주기");
                            handWaterBtn.setTextColor(Color.BLACK);
                            handWaterBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                        }
                    }
                }
        );
        handLightBtn.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        //만약 불이 꺼져있는 상태라면
                        if(light_on==false){
                            if(auto_light_on == true){
                                Toast.makeText(getApplicationContext(),"자동 조명기능을 해제하고 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(getApplicationContext(),"조명을 켰습니다.", Toast.LENGTH_LONG).show();
                                light_on=true;
                                lightToggle.setEnabled(false);
                                mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.LED.label()).setValue("ON");
                                handLightBtn.setText("조명 끄기");
                                handLightBtn.setTextColor(Color.WHITE);
                                handLightBtn.setBackground(getResources().getDrawable(R.drawable.hand_on));
                            }
                        }
                        //만약 불이 켜져있는 상태라면
                        else {
                            Toast.makeText(getApplicationContext(),"조명을 껐습니다.", Toast.LENGTH_LONG).show();
                            light_on=false;
                            lightToggle.setEnabled(true);
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.LED.label()).setValue("OFF");
                            handLightBtn.setText("조명 켜기");
                            handLightBtn.setTextColor(Color.BLACK);
                            handLightBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                        }

                    }
                }
        );
        handWindBtn.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        //만약 공기순환이 꺼져있는 상태라면
                        if(wind_on==false){
                            if(auto_wind_on == true){
                                Toast.makeText(getApplicationContext(),"자동 공기순환기능을 해제하고 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                            } else{
                            Toast.makeText(getApplicationContext(),"공기순환을 실행합니다.", Toast.LENGTH_LONG).show();
                                wind_on=true;
                                windToggle.setEnabled(false);
                                mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.COOLER.label()).setValue("ON");
                                handWindBtn.setText("공기 순환 종료");
                                handWindBtn.setTextColor(Color.WHITE);
                                handWindBtn.setBackground(getResources().getDrawable(R.drawable.hand_on));
                            }
                        }
                        //만약 공기순환이 켜져있는 상태라면
                        else {
                            Toast.makeText(getApplicationContext(),"공기순환을 종료합니다.", Toast.LENGTH_LONG).show();
                            wind_on=false;
                            windToggle.setEnabled(true);
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.COOLER.label()).setValue("OFF");
                            handWindBtn.setText("공기 순환 실행");
                            handWindBtn.setTextColor(Color.BLACK);
                            handWindBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                        }

                    }
                }
        );

    }
    //센서값에 따라 말풍선 말 바꾸는 함수
    public void setSpeechBubble(TextView x){
        if(soil_hum>=40 && soil_hum<60){
            if(temp>=20 && temp <25){
                x.setText("지금은 기분이 좋아요!");
            } else if(temp <20){
                x.setText("추워요..");
            } else if(temp >=25){
                x.setText("더워요..");
            }
        } else if(soil_hum<40){
            if(temp>=20 && temp <25){
                x.setText("목이 말라요!");
            } else if(temp <20){
                x.setText("목이 마르고 추워요..");
            } else if(temp >=25){
                x.setText("목이 마르고 더워요..");
            }
        } else if(soil_hum>=60){
            if(temp>=20 && temp <25){
                x.setText("축축해요!");
            } else if(temp <20){
                x.setText("축축하고 추워요..");
            } else if(temp >=25){
                x.setText("축축하고 더워요..");
            }
        }
    }


}