package com.example.plantproject;

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

import java.util.regex.Pattern;

public class MyPageActivity extends AppCompatActivity {
    private TextView myQrText, canUseIdText, myCheckPwdText, goLogoutTextbtn, myCheckOverSixPwdText, myCanUseNameText, myCanUseTelText;
    private Button modifyBtn, modifyCompleteBtn;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private EditText myEditEmailId, myEditPwd, myEditName, myEditTel, myEditCheckPwd;
    boolean isNameOK, isTelOK, isPwdOK, isPwdCheckOK = true;
    boolean beforeModify = true; // 처음 마이페이지에 들어가면 숨겨두었던 유효성검사 텍스트들이 떠서 이를 막기위한 변수. 수정전 상태면 true.
    int overLapCnt, childNum = 0;
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
        setContentView(R.layout.activity_mypage);

        myEditName = findViewById(R.id.my_name);
        myEditTel = findViewById(R.id.my_tel);
        myEditEmailId = findViewById(R.id.my_emailId);
        myEditPwd = findViewById(R.id.my_pwd);
        myEditCheckPwd = findViewById(R.id.my_CheckPwd);

        goLogoutTextbtn = findViewById(R.id.goLogoutText_btn);
        modifyBtn = findViewById(R.id.modify_btn);
        modifyCompleteBtn = findViewById(R.id.modify_complete_btn);


        myCanUseNameText = findViewById(R.id.myCanUseName_text);
        myCanUseTelText = findViewById(R.id.myCanUseTel_text);
        myCheckOverSixPwdText = findViewById(R.id.myCheckOverSixPwd_text);
        myCheckPwdText = findViewById(R.id.myCheckPwd_text);
        myQrText = findViewById(R.id.myQr_text);

        //파이어베이스 관련 변수
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("plant");

        //이메일형식인지 확인하는데 사용
        Pattern pattern = Patterns.EMAIL_ADDRESS;

        //MainActivity에서 마이페이지 버튼을 누르면서 intent에 "qr"을 키로 하는 값을 저장해뒀고, 이를 받아서 myQrText에 저장.
        Intent intent = getIntent();
        myQrText.setText(intent.getStringExtra("qr"));

        //현재 사용자에 해당하는 데이터베이스에 접근해서 editText에 현재 사용자의 정보를 넣어서 보여줌.
        mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.USERACCOUNT.label()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot sh : snapshot.getChildren()){
                    if(sh.getKey().equals("emailId")){
                        myEditEmailId.setText(sh.getValue().toString());
                    } else if(sh.getKey().equals("name")){
                        myEditName.setText(sh.getValue().toString());
                    } else if(sh.getKey().equals("pwd")){
                        myEditPwd.setText(sh.getValue().toString());
                        myEditCheckPwd.setText(sh.getValue().toString());
                    } else if(sh.getKey().equals("tel")){
                        myEditTel.setText(sh.getValue().toString());

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //수정하기 버튼. 버튼을 누르면 비활성화 되어 있던 버튼들이 활성화되고, "수정하기" 버튼은 사라지고 "수정완료" 버튼이 모습을 보임.
        modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beforeModify = false;
                myEditName.setEnabled(true);
                myEditTel.setEnabled(true);
                myEditPwd.setEnabled(true);
                myEditCheckPwd.setEnabled(true);
                modifyBtn.setVisibility(View.GONE);
                modifyCompleteBtn.setVisibility(View.VISIBLE);

            }
        });

        //수정완료 버튼. 수정된 내용을 해당 데이터베이스에 저장. 비밀번호도 업데이트 해줌. 그리고 다시 editText들 비활성화 해줌.
        modifyCompleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isNameOK&&isTelOK&&isPwdOK&&isPwdCheckOK){
                    mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.USERACCOUNT.label()).child(DbName.NAME.label()).setValue(myEditName.getText().toString());
                    mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.USERACCOUNT.label()).child(DbName.TEL.label()).setValue(myEditTel.getText().toString());
                    mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.USERACCOUNT.label()).child(DbName.PWD.label()).setValue(myEditPwd.getText().toString());
                    mFirebaseAuth.getCurrentUser().updatePassword(myEditPwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "수정 완료 되었습니다.", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "수정에 실패 했습니다.", Toast.LENGTH_LONG).show();

                            }
                        }
                    });
                    myEditName.setEnabled(false);
                    myEditTel.setEnabled(false);
                    myEditEmailId.setEnabled(false);
                    myEditPwd.setEnabled(false);
                    myEditCheckPwd.setEnabled(false);
                    modifyBtn.setVisibility(View.VISIBLE);
                    modifyCompleteBtn.setVisibility(View.GONE);
                    beforeModify = true;
                }
                else {
                    Toast.makeText(getApplicationContext(),"정보를 제대로 기입했는지 확인해주세요.",Toast.LENGTH_SHORT).show();
                    Log.d("이름,번호,비번,비번체크",String.valueOf(isNameOK)+","+String.valueOf(isTelOK)+","+String.valueOf(isPwdOK)+","+String.valueOf(isPwdOK));
                }
            }
        });

        //이름 editText 변경 이벤트
        myEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isNameOK = true;

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!beforeModify){
                    myCanUseNameText.setVisibility(View.VISIBLE);
                    if(myEditName.getText().length()<2){
                        myCanUseNameText.setText("이름을 두글자 이상 입력해 주세요.");
                        myCanUseNameText.setTextColor(Color.parseColor("#FF0000")); //빨간색
                        isNameOK = false;
                    }
                    else{
                        myCanUseNameText.setText("올바른 형식입니다.");
                        myCanUseNameText.setTextColor(Color.parseColor("#08A600")); //초록색
                        isNameOK = true;
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (myEditName.getText().toString().equals("")&&!beforeModify){
                    myCanUseNameText.setVisibility(View.GONE);
                    isNameOK = false;
                }
            }
        });

        //전화번호 editText 변경 이벤트
        myEditTel.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isTelOK = true;

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(!beforeModify){
                    myCanUseTelText.setVisibility(View.VISIBLE);
                    if(myEditTel.getText().length()<10){
                        myCanUseTelText.setText("번호를 다시 확인해 주세요");
                        myCanUseTelText.setTextColor(Color.parseColor("#FF0000")); //빨간색
                        isTelOK = false;
                    }
                    else{
                        myCanUseTelText.setText("올바른 형식입니다.");
                        myCanUseTelText.setTextColor(Color.parseColor("#08A600")); //초록색
                        isTelOK = true;
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (myEditTel.getText().toString().equals("")&&!beforeModify){
                    myCanUseTelText.setVisibility(View.GONE);
                    isTelOK = false;
                }
            }
        });



        //비밀번호 editText 변경 이벤트
        myEditPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isPwdOK = true;

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(!beforeModify){
                    myCheckOverSixPwdText.setVisibility(View.VISIBLE);
                    if (myEditPwd.getText().length() < 6) {
                        myCheckOverSixPwdText.setText("비밀번호는 6자리 이상이어야 합니다.");
                        myCheckOverSixPwdText.setTextColor(Color.parseColor("#FF0000"));
                        isPwdOK = false;
                    } else {
                        myCheckOverSixPwdText.setText("올바른 비밀번호 형식입니다.");
                        myCheckOverSixPwdText.setTextColor(Color.parseColor("#08A600"));
                        isPwdOK = true;
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (myEditPwd.getText().toString().equals("")&&!beforeModify) {
                    myCheckOverSixPwdText.setVisibility(View.GONE);
                    isPwdOK = false;
                }
            }
        });


        //비밀번호 확인 editText 변경이벤트
        myEditCheckPwd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isPwdCheckOK = true;

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(!beforeModify){
                    myCheckPwdText.setVisibility(View.VISIBLE);
                    if(myEditCheckPwd.getText().toString().equals(myEditPwd.getText().toString())){
                        myCheckPwdText.setText("비밀번호가 일치합니다.");
                        myCheckPwdText.setTextColor(Color.parseColor("#08A600"));
                        isPwdCheckOK = true;
                    }
                    else{
                        myCheckPwdText.setText("비밀번호가 일치하지 않습니다.");
                        myCheckPwdText.setTextColor(Color.parseColor("#FF0000"));
                        isPwdCheckOK = false;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (myEditCheckPwd.getText().toString().equals("")&&!beforeModify){
                    myCheckPwdText.setVisibility(View.GONE);
                    isPwdCheckOK = false;
                }
            }
        });



        //로그인 화면으로 돌아가기
        goLogoutTextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFirebaseAuth.signOut();
                Intent intent = new Intent(MyPageActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(),"로그아웃 하였습니다.",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
