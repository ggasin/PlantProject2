package com.example.plantproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {
    private IntentIntegrator qrScan;
    private TextView qrResultText,canUseIdText,checkPwdText, goLoginTextbtn,checkOverSixPwdText,canUseNameText, canUseTelText,canUseNickNameText;
    private Button qrScanBtn,registerBtn, idOverLapBtn;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private EditText editEmailId,regEditPwd,editName,editNickName,editTel,checkEditPwd;
    boolean isNameOK,isNickNameOK,isTelOK,isEmailIdOK,isPwdOK,isPwdCheckOK,isQrOK = false;
    int overLapCnt, childNum =0;
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
        setContentView(R.layout.activity_register);
        //editText 변수
        editName = findViewById(R.id.edit_name);
        editTel = findViewById(R.id.edit_tel);
        editEmailId = findViewById(R.id.reg_edit_emailId);
        editNickName = findViewById(R.id.edit_nickName);
        regEditPwd = findViewById(R.id.reg_edit_pwd);
        checkEditPwd = findViewById(R.id.edit_CheckPwd);
        //textView 변수
        canUseIdText = findViewById(R.id.canUseId_text);
        canUseNameText = findViewById(R.id.canUseName_text);
        canUseTelText = findViewById(R.id.canUseTel_text);
        canUseNickNameText = findViewById(R.id.canUseNickName_text);
        checkPwdText = findViewById(R.id.checkPwd_text);
        checkOverSixPwdText = findViewById(R.id.checkOverSixPwd_text);
        //Button 변수
        goLoginTextbtn = findViewById(R.id.go_login_text_btn);
        registerBtn = findViewById(R.id.register_btn);
        idOverLapBtn = findViewById(R.id.idOverlap_btn);
        //qr스캔 관련 변수
        qrScanBtn = (Button)findViewById(R.id.qrScan_btn);
        qrResultText = (TextView) findViewById(R.id.qrResult_text);
        qrScan = new IntentIntegrator(this);
        //파이어베이스 관련 변수
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("plant");
        //이메일형식인지 확인하는데 사용
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        //qr스캔 버튼 클릭 이벤트
        qrScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qrScan.setPrompt("Scanning...");
                qrScan.initiateScan();
            }
        });

        //이름 editText 변경 이벤트
        editName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                canUseNameText.setVisibility(View.VISIBLE);
                if(editName.getText().length()<2){
                    canUseNameText.setText("이름을 두글자 이상 입력해 주세요.");
                    canUseNameText.setTextColor(Color.parseColor("#FF0000")); //빨간색
                    isNameOK = false;
                }
                else{
                    canUseNameText.setText("올바른 형식입니다.");
                    canUseNameText.setTextColor(Color.parseColor("#08A600")); //초록색
                    isNameOK = true;
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editName.getText().toString().equals("")){
                    canUseNameText.setVisibility(View.GONE);
                    isNameOK = false;
                }
            }
        });
        //식물별명 editText 변경 이벤트
        editNickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                canUseNickNameText.setVisibility(View.VISIBLE);
                if(editNickName.getText().length()<1){
                    canUseNickNameText.setText("별명을 한글자 이상 입력해 주세요.");
                    canUseNickNameText.setTextColor(Color.parseColor("#FF0000")); //빨간색
                    isNickNameOK = false;
                }
                else if(editNickName.getText().length()>4){
                    canUseNickNameText.setText("별명은 네글자 이하로 작성해주세요.");
                    canUseNickNameText.setTextColor(Color.parseColor("#FF0000")); //초록색
                    isNickNameOK = false;
                } else {
                    canUseNickNameText.setText("사용가능한 별명입니다.");
                    canUseNickNameText.setTextColor(Color.parseColor("#08A600")); //초록색
                    isNickNameOK = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editNickName.getText().toString().equals("")){
                    canUseNickNameText.setVisibility(View.GONE);
                    isNickNameOK = false;
                }
            }
        });
        //전화번호 editText 변경 이벤트
        editTel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                canUseTelText.setVisibility(View.VISIBLE);
                if(editTel.getText().length()<10){
                    canUseTelText.setText("번호를 다시 확인해 주세요");
                    canUseTelText.setTextColor(Color.parseColor("#FF0000")); //빨간색
                    isTelOK = false;
                }
                else{
                    canUseTelText.setText("올바른 형식입니다.");
                    canUseTelText.setTextColor(Color.parseColor("#08A600")); //초록색
                    isTelOK = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editTel.getText().toString().equals("")){
                    canUseTelText.setVisibility(View.GONE);
                    isTelOK = false;
                }
            }
        });


        //아이디 editText 변경 이벤트
        editEmailId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!pattern.matcher(editEmailId.getText().toString()).matches()){
                    canUseIdText.setVisibility(View.VISIBLE);
                    canUseIdText.setText("이메일 형식을 확인해주세요.");
                    canUseIdText.setTextColor(Color.parseColor("#FF0000")); //빨간색
                    idOverLapBtn.setClickable(false);
                }
                else{
                    canUseIdText.setVisibility(View.GONE);
                    idOverLapBtn.setClickable(true);
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editEmailId.getText().toString().equals("")){
                    canUseIdText.setVisibility(View.GONE);
                    isEmailIdOK = false;
                }
            }
        });


        //비밀번호 editText 변경 이벤트
        regEditPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkEditPwd.setText("");
                isPwdCheckOK=false;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkOverSixPwdText.setVisibility(View.VISIBLE);
                if (regEditPwd.getText().length() < 6) {
                    checkOverSixPwdText.setText("비밀번호는 6자리 이상이어야 합니다.");
                    checkOverSixPwdText.setTextColor(Color.parseColor("#FF0000"));
                    isPwdOK = false;
                } else {
                    checkOverSixPwdText.setText("올바른 비밀번호 형식입니다.");
                    checkOverSixPwdText.setTextColor(Color.parseColor("#08A600"));
                    isPwdOK = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (regEditPwd.getText().toString().equals("")) {
                    checkOverSixPwdText.setVisibility(View.GONE);
                    isPwdOK = false;
                }
            }
        });


        //비밀번호 확인 editText 변경이벤트
        checkEditPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkPwdText.setVisibility(View.VISIBLE);
                if(checkEditPwd.getText().toString().equals(regEditPwd.getText().toString())){
                    checkPwdText.setText("비밀번호가 일치합니다.");
                    checkPwdText.setTextColor(Color.parseColor("#08A600"));
                    isPwdCheckOK = true;
                }
                else{
                    checkPwdText.setText("비밀번호가 일치하지 않습니다.");
                    checkPwdText.setTextColor(Color.parseColor("#FF0000"));
                    isPwdCheckOK = false;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (checkEditPwd.getText().toString().equals("")){
                    checkPwdText.setVisibility(View.GONE);
                    isPwdCheckOK = false;
                }
            }
        });

        //회원가입 버튼 클릭 후 가입 진행 이벤트
        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strName = editName.getText().toString();
                String strNickName = editNickName.getText().toString();
                String strEmailId = editEmailId.getText().toString();
                String strTel = editTel.getText().toString();
                String strPwd = regEditPwd.getText().toString();
                String strQr = qrResultText.getText().toString();
                Log.d("이메일OK", String.valueOf(isEmailIdOK));
                Log.d("닉네임OK", String.valueOf(isNickNameOK));
                Log.d("이름OK", String.valueOf(isNameOK));
                Log.d("번호OK", String.valueOf(isTelOK));
                Log.d("비밀OK", String.valueOf(isPwdOK));
                Log.d("비밀확인OK", String.valueOf(isPwdCheckOK));
                Log.d("큐알OK", String.valueOf(isQrOK));
                if (isEmailIdOK & isNickNameOK & isNameOK & isTelOK & isPwdOK & isPwdCheckOK & isQrOK) {
                    mFirebaseAuth.createUserWithEmailAndPassword(strEmailId, strPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                //회원가입 성공
                                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();

                                UserAccount account = new UserAccount();

                                HashMap<Object, Integer> sensors_child = new HashMap<>();
                                sensors_child.put(DbName.HUM.label(), 0);
                                sensors_child.put(DbName.TEMP.label(), 0);
                                sensors_child.put(DbName.SOILHUM.label(), 0);

                                HashMap<Object, String> auto_nonauto_child = new HashMap<>();
                                auto_nonauto_child.put(DbName.LED.label(), "OFF");
                                auto_nonauto_child.put(DbName.COOLER.label(), "OFF");
                                auto_nonauto_child.put(DbName.WATER.label(), "OFF");

                                HashMap<Object,String> auto_standard_child = new HashMap<>();
                                auto_standard_child.put(DbName.SOILHUM.label(),"20");
                                auto_standard_child.put(DbName.TEMP.label(),"30");


                                account.setEmailId(firebaseUser.getEmail()); // 로그인을 하는 정확한 이메일이 필요하기 때문에 firebaseUser에서 정보를 가져옴
                                account.setPwd(strPwd);
                                account.setName(strName);
                                account.setNickName(strNickName);
                                account.setTel(strTel);
                                account.setQr(strQr);
                                mDatabaseRef.child(account.getQr()).child(DbName.USERACCOUNT.label()).setValue(account);
                                mDatabaseRef.child(account.getQr()).child(DbName.OPERATION.label()).child(DbName.SENSORS.label()).setValue(sensors_child);
                                mDatabaseRef.child(account.getQr()).child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.AUTO.label()).setValue(auto_nonauto_child);
                                mDatabaseRef.child(account.getQr()).child(DbName.OPERATION.label()).child(DbName.BUTTON.label()).child(DbName.NONAUTO.label()).setValue(auto_nonauto_child);
                                mDatabaseRef.child(account.getQr()).child(DbName.OPERATION.label()).child(DbName.AUTOSTANDARD.label()).setValue(auto_standard_child);
                                Toast.makeText(getApplicationContext(), "가입성공", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish(); //현재 엑티비티 파괴
                            } else {
                                //회원가입 실패
                                Toast.makeText(getApplicationContext(), "회원가입 실패", Toast.LENGTH_SHORT).show();
                            }
                        }

                    });
                } else {
                    Toast.makeText(getApplicationContext(), "정보를 모두 기입했는지, qr스캔,아이디 중복확인을 했는지 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

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

        //중복버튼 클릭 이벤트
        idOverLapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overLapCnt =0;
                mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot sh : snapshot.getChildren()){
                                mDatabaseRef.child(sh.getKey()).child(DbName.USERACCOUNT.label()).child(DbName.EMAILID.label()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().getValue().toString().equals(editEmailId.getText().toString())) {
                                                Toast.makeText(getApplicationContext(), "중복된 아이디입니다.", Toast.LENGTH_SHORT).show();
                                                canUseIdText.setVisibility(View.VISIBLE);
                                                canUseIdText.setText("사용 불가한 아이디입니다.");
                                                canUseIdText.setTextColor(Color.parseColor("#FF0000")); //빨간색
                                                isEmailIdOK = false;
                                                overLapCnt--; //중복이 되면 이 값을 마이너스 함
                                                Log.d("중복된 값",task.getResult().getValue().toString());
                                            }
                                            else{
                                                overLapCnt++;
                                                Log.d("중복 아닐때 overLapCnt 값",String.valueOf(overLapCnt));
                                                Log.d("중복 아닐때 childNum 값",String.valueOf(childNum));
                                                if(overLapCnt == childNum){ //child를 훑었을 때 중복이 없었다면 overLapCnt와 자식들 수는 똑같을거고 아니라면 중복이 있었다는 뜻
                                                    Toast.makeText(getApplicationContext(), "사용가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                                                    canUseIdText.setVisibility(View.VISIBLE);
                                                    canUseIdText.setText("사용 가능한 아이디입니다.");
                                                    canUseIdText.setTextColor(Color.parseColor("#08A600")); //초록색
                                                    isEmailIdOK = true;
                                                }

                                            }
                                        }
                                    }
                                });
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "사용가능한 아이디입니다.", Toast.LENGTH_SHORT).show();
                            canUseIdText.setVisibility(View.VISIBLE);
                            canUseIdText.setText("사용 가능한 아이디입니다.");
                            canUseIdText.setTextColor(Color.parseColor("#08A600")); //초록색
                            isEmailIdOK = true;
                        }

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });

        }});

        //로그인 화면으로 돌아가기
        goLoginTextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this , LoginActivity.class);
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
                Toast.makeText(RegisterActivity.this, "취소!", Toast.LENGTH_SHORT).show();
                isQrOK = false;
            } else {
                //qrcode 결과가 있으면
                Toast.makeText(RegisterActivity.this, "스캔완료!", Toast.LENGTH_SHORT).show();
                try {
                    JSONObject obj = new JSONObject(result.getContents());
                    qrResultText.setText(obj.getString("qr"));
                    isQrOK = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                    qrResultText.setText(result.getContents());
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}