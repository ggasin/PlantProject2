package com.example.plantproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.auth.PhoneAuthProvider;
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
    String qr;
    //센서값
    int soil_hum=0;
    int hum=0;
    int temp=0;
    //자동 on/off 버튼 boolean 변수
    boolean autoWaterOn = false; // false면 꺼져있는 상태
    boolean autoLedOn = false;
    boolean autoCoolerOn = false;
    //수동
    boolean handWaterOn = false;
    boolean handLedOn = false; // 조명이 켜져있는지 꺼져있는지 알기 위한 변수. false는 꺼져있는 상태
    boolean handCoolerOn = false; // 공기순환 모터가 꺼져있는지 알기 위한 변수.
    //데이터베이스
    private FirebaseDatabase database;
    private DatabaseReference mDatabaseRef;
    //xml 변수
    private ToggleButton waterToggle, ledToggle, coolerToggle;
    private TextView waterText, ledText, coolerText, shDataText, hDataText, tDataText, plantText,nickNameText;
    private Button handWaterBtn, handLedBtn, handCoolerBtn;
    private ImageButton goMyPageBtn,goSettingBtn,goHelpBtn;

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
        setContentView(R.layout.activity_main);

        //자동 ON/OFF 버튼 및 텍스트 변수
        waterToggle = findViewById(R.id.water_auto_btn);
        ledToggle = findViewById(R.id.led_auto_btn);
        coolerToggle = findViewById(R.id.cooler_auto_btn);
        waterText = findViewById(R.id.water_auto_text);
        ledText = findViewById(R.id.led_auto_text);
        coolerText = findViewById(R.id.cooler_auto_text);

        //센서 데이터 텍스트뷰 변수
        shDataText = findViewById(R.id.soil_humanity_data);
        hDataText = findViewById(R.id.humanity_data);
        tDataText = findViewById(R.id.temperature_data);
        plantText = findViewById(R.id.plant_text);

        //닉네임 텍스트뷰
        nickNameText = findViewById(R.id.nickName_text);

        //수동 버튼 변수
        handWaterBtn = findViewById(R.id.hand_water_btn);
        handLedBtn = findViewById(R.id.hand_led_btn);
        handCoolerBtn = findViewById(R.id.hand_cooler_btn);
        goMyPageBtn = findViewById(R.id.mypage_btn);
        goSettingBtn = findViewById(R.id.setting_btn);
        goHelpBtn = findViewById(R.id.help_btn);

        //데이터베이스 변수
        database = FirebaseDatabase.getInstance();
        Intent intent = getIntent();
        qr=intent.getStringExtra("qr"); // 다른 액티비티로부터 "qr"이라는 키로 기록된 qr값 가져옴. 사용자의 고유qr값 기록
        mDatabaseRef = database.getReference("plant").child(intent.getStringExtra("qr"));

        //앱이 실행하면서 한번만 처리. 데이터베이스의 값에 맞게 앱 상태 초기화
        //닉네임 가져오기
        mDatabaseRef.child(DbName.USERACCOUNT.label()).child(DbName.NICKNAME.label()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String nickName = snapshot.getValue(String.class);
                nickNameText.setText(nickName);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        //자동 물 조작 버튼 초기화
        setAutoEqualDatabase(DbName.WATER.label());
        setAutoEqualDatabase(DbName.LED.label());
        setAutoEqualDatabase(DbName.COOLER.label());

        //수동 조명 조작버튼 초기화
        setNonAutoEqualDatabase(DbName.WATER.label());
        setNonAutoEqualDatabase(DbName.COOLER.label());
        setNonAutoEqualDatabase(DbName.LED.label());

        //센서값 데이터베이스로부터 가져오기
        getSensorData(DbName.SOILHUM.label());
        getSensorData(DbName.HUM.label());
        getSensorData(DbName.TEMP.label());

        //자동 ON/OFF 토글버튼 이벤트 코드
        autoToggleBtnListener(waterToggle,"waterToggle");
        autoToggleBtnListener(ledToggle,"ledToggle");
        autoToggleBtnListener(coolerToggle,"coolerToggle");

        //수동 버튼 이벤트
        nonAutoBtnListener(handWaterBtn,"handWaterBtn");
        nonAutoBtnListener(handLedBtn,"handLedBtn");
        nonAutoBtnListener(handCoolerBtn,"handCoolerBtn");

        //마이페이지 버튼 클릭 이벤트
        goMyPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,MyPageActivity.class);
                intent.putExtra("qr",qr);
                startActivity(intent);
            }
        });

        //도움말 버튼 클릭 이벤트
        goHelpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,HelpPopupActivity.class);
                startActivity(intent);

            }
        });

        //세팅(설정) 버튼 클릭 이벤트
        goSettingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup= new PopupMenu(getApplicationContext(), view);//v는 클릭된 뷰를 의미
                getMenuInflater().inflate(R.menu.option_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.autoValue_setting_menu:
                                Intent intent = new Intent(MainActivity.this,SettingAutoActivity.class);
                                intent.putExtra("qr",qr);
                                startActivity(intent);
                                break;
                            case R.id.alert_setting_menu:
                                Toast.makeText(getApplication(),"메뉴1",Toast.LENGTH_SHORT).show();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                popup.show();//Popup Menu 보이기
            }
        });


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
    //앱 실행시 자동버튼 데이터베이스 상태와 맞추는 함수
    public void setAutoEqualDatabase(String child){
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(child).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.getValue().toString(); //데이터
                if(child==DbName.WATER.label()){
                    if(data.equals("ON")){
                        waterToggle.setChecked(true);
                        waterText.setText("물 ON");
                    } else {
                        waterToggle.setChecked(false);
                        waterText.setText("물 OFF");
                    }
                } else if(child == DbName.LED.label()){
                    if(data.equals("ON")){
                        ledToggle.setChecked(true);
                        ledText.setText("조명 ON");
                    } else {
                        ledToggle.setChecked(false);
                        ledText.setText("조명 OFF");
                    }
                } else if(child == DbName.COOLER.label()){
                    if(data.equals("ON")){
                        coolerToggle.setChecked(true);
                        coolerText.setText("바람 ON");
                    } else {
                        coolerToggle.setChecked(false);
                        coolerText.setText("바람 OFF");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    //앱 실행시 수동버튼을 데이터베이스와 맞추는 함수
    public void setNonAutoEqualDatabase(String child){
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(child).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String data = snapshot.getValue(String.class);
                if(child==DbName.WATER.label()){
                    if(data.equals("ON")){
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
                }else if(child==DbName.COOLER.label()){
                    if(data.equals("ON")){
                        coolerToggle.setEnabled(false);
                        handCoolerBtn.setText("공기 순환 종료");
                        handCoolerBtn.setTextColor(Color.WHITE);
                        handCoolerBtn.setBackground(getResources().getDrawable(R.drawable.hand_on));
                    } else {
                        coolerToggle.setEnabled(true);
                        handCoolerBtn.setText("공기 순환 실행");
                        handCoolerBtn.setTextColor(Color.BLACK);
                        handCoolerBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                    }
                }else if(child==DbName.LED.label()){
                    if(data.equals("ON")){
                        ledToggle.setEnabled(false);
                        handLedBtn.setText("조명 끄기");
                        handLedBtn.setTextColor(Color.WHITE);
                        handLedBtn.setBackground(getResources().getDrawable(R.drawable.hand_on));
                    } else {
                        ledToggle.setEnabled(true);
                        handLedBtn.setText("조명 켜기");
                        handLedBtn.setTextColor(Color.BLACK);
                        handLedBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    //센서값 변동시 데이터베이스로부터 센서값을 받아오는 함수.
    public void getSensorData(String child){
        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.SENSORS.label()).child(child).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int data = (int)snapshot.getValue(Integer.class);
                if(child==DbName.SOILHUM.label()){ //토양습도면
                    shDataText.setText(data+"%"); //토양습도 텍스트뷰에 데이터 표시
                    soil_hum = data;
                    setSpeechBubble(plantText); //변경된 토양습도에 따른 말풍선 변경
                }else if(child == DbName.HUM.label()){
                    hDataText.setText(data+"%");
                    hum=data;
                    setSpeechBubble(plantText);
                }else if(child == DbName.TEMP.label()){
                    tDataText.setText(data +"º");
                    temp=data;
                    setSpeechBubble(plantText);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

    }
    //자동 토글버튼 리스너 함수
    public void autoToggleBtnListener(ToggleButton toggle,String toggleName){
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean ischecked) {
                if(toggleName=="waterToggle"){ //자동 물 조작이라면
                    String waterAutoText;
                    if(ischecked && autoWaterOn == false){ //버튼이 체크가 됐고, 자동 기능이 꺼져있다면
                        waterAutoText = "물 ON";
                        autoWaterOn = true;   //auto_water_on 불린값을 true로 바꿔줌으로써 자동기능이 실행중이라는 사실을 저장.
                        //데이터베이스에 있는 자동 물 상태를 OFF에서 ON으로 변경해줌.
                        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.WATER.label()).setValue("ON");
                        waterText.setText(waterAutoText); //버튼 아래 있는 텍스트 OFF->ON 변경
                    } else {
                        waterAutoText = "물 OFF";
                        autoWaterOn = false;
                        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.WATER.label()).setValue("OFF");
                        waterText.setText(waterAutoText);
                    }

                }else if(toggleName=="ledToggle"){
                    String ledAutoText;
                    if(ischecked && autoLedOn == false){
                        ledAutoText = "조명 ON";
                        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.LED.label()).setValue("ON");
                        ledText.setText(ledAutoText);
                        autoLedOn = true;
                    }
                    else {
                        ledAutoText = "조명 OFF";
                        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.LED.label()).setValue("OFF");
                        ledText.setText(ledAutoText);
                        autoLedOn = false;
                    }
                }else if(toggleName=="coolerToggle"){
                    String coolerAutoText;
                    if(ischecked && autoCoolerOn == false){
                        coolerAutoText = "바람 ON";
                        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.COOLER.label()).setValue("ON");
                        coolerText.setText(coolerAutoText);
                        autoCoolerOn = true;
                    }
                    else {
                        coolerAutoText = "바람 OFF";
                        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).child(DbName.COOLER.label()).setValue("OFF");
                        coolerText.setText(coolerAutoText);
                        autoCoolerOn = false;
                    }
                }
            }
        });

    }
    //수동 버튼 클릭 이벤트 리스너 함수
    public void nonAutoBtnListener(Button btn,String btnName){
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(btnName=="handWaterBtn"){
                    //만약 물이 안 나오고 있으면
                    if(handWaterOn ==false ){
                        if(autoWaterOn == true){
                            Toast.makeText(getApplicationContext(),"자동 물주기를 해제하고 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"물주기를 실행합니다.", Toast.LENGTH_LONG).show();
                            handWaterOn =true;
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
                        handWaterOn =false;
                        waterToggle.setEnabled(true);
                        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.WATER.label()).setValue("OFF");
                        handWaterBtn.setText("물 주기");
                        handWaterBtn.setTextColor(Color.BLACK);
                        handWaterBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                    }
                }else if(btnName=="handLedBtn"){
                    //만약 불이 꺼져있는 상태라면
                    if(handLedOn ==false){
                        if(autoLedOn == true){
                            Toast.makeText(getApplicationContext(),"자동 조명기능을 해제하고 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"조명을 켰습니다.", Toast.LENGTH_LONG).show();
                            handLedOn =true;
                            ledToggle.setEnabled(false);
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.LED.label()).setValue("ON");
                            handLedBtn.setText("조명 끄기");
                            handLedBtn.setTextColor(Color.WHITE);
                            handLedBtn.setBackground(getResources().getDrawable(R.drawable.hand_on));
                        }
                    }
                    //만약 불이 켜져있는 상태라면
                    else {
                        Toast.makeText(getApplicationContext(),"조명을 껐습니다.", Toast.LENGTH_LONG).show();
                        handLedOn =false;
                        ledToggle.setEnabled(true);
                        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.LED.label()).setValue("OFF");
                        handLedBtn.setText("조명 켜기");
                        handLedBtn.setTextColor(Color.BLACK);
                        handLedBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                    }

                }else if(btnName=="handCoolerBtn"){
                    //만약 공기순환이 꺼져있는 상태라면
                    if(handCoolerOn ==false){
                        if(autoCoolerOn == true){
                            Toast.makeText(getApplicationContext(),"자동 공기순환기능을 해제하고 다시 시도해주세요", Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(getApplicationContext(),"공기순환을 실행합니다.", Toast.LENGTH_LONG).show();
                            handCoolerOn =true;
                            coolerToggle.setEnabled(false);
                            mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.COOLER.label()).setValue("ON");
                            handCoolerBtn.setText("공기 순환 종료");
                            handCoolerBtn.setTextColor(Color.WHITE);
                            handCoolerBtn.setBackground(getResources().getDrawable(R.drawable.hand_on));
                        }
                    }
                    //만약 공기순환이 켜져있는 상태라면
                    else {
                        Toast.makeText(getApplicationContext(),"공기순환을 종료합니다.", Toast.LENGTH_LONG).show();
                        handCoolerOn =false;
                        coolerToggle.setEnabled(true);
                        mDatabaseRef.child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).child(DbName.COOLER.label()).setValue("OFF");
                        handCoolerBtn.setText("공기 순환 실행");
                        handCoolerBtn.setTextColor(Color.BLACK);
                        handCoolerBtn.setBackground(getResources().getDrawable(R.drawable.btn_hand));
                    }
                }
            }
        });
    }
}