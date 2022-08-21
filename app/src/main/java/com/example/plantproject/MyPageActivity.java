package com.example.plantproject;

import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.regex.Pattern;

public class MyPageActivity extends AppCompatActivity {
    private TextView myQrText, myCheckPwdText, goLogoutTextbtn, deleteIdTextBtn,myCheckOverSixPwdText, myCanUseNameText, myCanUseTelText,myCanUseNickNameText;
    private Button modifyBtn, modifyCompleteBtn;
    private ImageButton goBackBtn;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private EditText myEditEmailId, myEditPwd, myEditName, myEditNickName,myEditTel, myEditCheckPwd;
    boolean isNameOK, isNickNameOK,isTelOK, isPwdOK, isPwdCheckOK = true;
    boolean beforeModify = true; // 처음 마이페이지에 들어가면 숨겨두었던 유효성검사 텍스트들이 떠서 이를 막기위한 변수. 수정전 상태면 true.
    int overLapCnt, childNum = 0;
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
        setContentView(R.layout.activity_mypage);

        myEditName = findViewById(R.id.my_name);
        myEditTel = findViewById(R.id.my_tel);
        myEditEmailId = findViewById(R.id.my_emailId);
        myEditPwd = findViewById(R.id.my_pwd);
        myEditCheckPwd = findViewById(R.id.my_CheckPwd);
        myEditNickName = findViewById(R.id.my_nick_name);

        goLogoutTextbtn = findViewById(R.id.goLogoutText_btn);
        deleteIdTextBtn = findViewById(R.id.deleteIdText_btn);
        modifyBtn = findViewById(R.id.modify_btn);
        modifyCompleteBtn = findViewById(R.id.modify_complete_btn);
        goBackBtn = findViewById(R.id.myPage_goBack_btn);

        myCanUseNameText = findViewById(R.id.myCanUseName_text);
        myCanUseNickNameText = findViewById(R.id.myCanUseNickName_text);
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
                    if(sh.getKey().equals(DbName.EMAILID.label())){
                        myEditEmailId.setText(sh.getValue().toString());
                    } else if(sh.getKey().equals(DbName.NICKNAME.label())){
                        myEditNickName.setText(sh.getValue().toString());
                    } else if(sh.getKey().equals(DbName.NAME.label())){
                        myEditName.setText(sh.getValue().toString());
                    } else if(sh.getKey().equals(DbName.PWD.label())){
                        myEditPwd.setText(sh.getValue().toString());
                        myEditCheckPwd.setText(sh.getValue().toString());
                    } else if(sh.getKey().equals(DbName.TEL.label())){
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
                AuthCredential credential = EmailAuthProvider.getCredential(mFirebaseAuth.getCurrentUser().getEmail(),myEditPwd.getText().toString());
                mFirebaseAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Log.d("My로그인 재인증 성공", "로그인 재인증 완료");
                        } else {
                            Log.d("My로그인 재인증 실패", "로그인 재인증 실패");
                        }

                    }
                });
                beforeModify = false;
                myEditName.setEnabled(true);
                myEditNickName.setEnabled(true);
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
                if(isNameOK&&isNickNameOK&&isTelOK&&isPwdOK&&isPwdCheckOK){
                    mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.USERACCOUNT.label()).child(DbName.NAME.label()).setValue(myEditName.getText().toString());
                    mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.USERACCOUNT.label()).child(DbName.NICKNAME.label()).setValue(myEditNickName.getText().toString());
                    mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.USERACCOUNT.label()).child(DbName.TEL.label()).setValue(myEditTel.getText().toString());
                    mDatabaseRef.child(intent.getStringExtra("qr")).child(DbName.USERACCOUNT.label()).child(DbName.PWD.label()).setValue(myEditPwd.getText().toString());
                    mFirebaseAuth.getCurrentUser().updatePassword(myEditPwd.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(), "수정 완료 되었습니다.", Toast.LENGTH_SHORT).show();
                                Log.d("현재 사용자",mFirebaseAuth.getCurrentUser().getEmail());
                                Log.d("비밀번호",myEditPwd.getText().toString());
                            }else{
                                Toast.makeText(getApplicationContext(), "수정에 실패 했습니다.", Toast.LENGTH_SHORT).show();
                                Log.d("현재 사용자",mFirebaseAuth.getCurrentUser().getEmail());
                                Log.d("비밀번호",myEditPwd.getText().toString());
                            }
                        }
                    });
                    myEditName.setEnabled(false);
                    myEditNickName.setEnabled(false);
                    myEditTel.setEnabled(false);
                    myEditEmailId.setEnabled(false);
                    myEditPwd.setEnabled(false);
                    myEditCheckPwd.setEnabled(false);
                    modifyBtn.setVisibility(View.VISIBLE);
                    modifyCompleteBtn.setVisibility(View.GONE);
                    myCanUseNameText.setVisibility(View.GONE);
                    myCanUseNickNameText.setVisibility(View.GONE);
                    myCanUseTelText.setVisibility(View.GONE);
                    myCheckPwdText.setVisibility(View.GONE);
                    myCheckOverSixPwdText.setVisibility(View.GONE);
                    beforeModify = true;
                }
                else {
                    Toast.makeText(getApplicationContext(),"정보를 제대로 기입했는지 확인해주세요.",Toast.LENGTH_SHORT).show();
                    Log.d("이름,닉네임,번호,비번,비번체크",String.valueOf(isNameOK)+","+String.valueOf(isNickNameOK)+","+String.valueOf(isTelOK)+","+String.valueOf(isPwdOK)+","+String.valueOf(isPwdOK));
                }
            }
        });

        //이름 editText 변경 이벤트
        myEditName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isNameOK = true;
                //여기 true를 안해주면 아무것도 변경하지 않고 수정완료를 눌렀을 때 afterTextChanged에서 false값을 넘겨줘서
                //beforeTextChanged에 각 boolean값마다 true를 넣어주니 해결됨.
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
        //별명 editText 변경 이벤트
        myEditNickName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isNickNameOK = true;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(!beforeModify){
                    myCanUseNickNameText.setVisibility(View.VISIBLE);
                   if(myEditNickName.getText().length()>4){
                        myCanUseNickNameText.setText("별명은 네글자 이하로 작성해주세요.");
                        myCanUseNickNameText.setTextColor(Color.parseColor("#FF0000")); //빨간색
                        isNickNameOK = false;
                    }
                    else{
                        myCanUseNickNameText.setText("사용 가능한 별명입니다.");
                        myCanUseNickNameText.setTextColor(Color.parseColor("#08A600")); //초록색
                        isNickNameOK = true;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (myEditNickName.getText().toString().equals("")&&!beforeModify){
                    myCanUseNickNameText.setVisibility(View.GONE);
                    isNickNameOK = false;
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
                myEditCheckPwd.setText("");
                isPwdCheckOK=false;
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

        //로그아웃 버튼
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
        //회원 탈퇴 버튼
        deleteIdTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MyPageActivity.this);
                builder.setTitle("회원탈퇴");
                builder.setMessage("정말 탈퇴 하시겠습니까?");
                builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFirebaseAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(),"탈퇴가 완료되었습니다.",Toast.LENGTH_SHORT).show();
                                    mDatabaseRef.child(intent.getStringExtra("qr")).removeValue();
                                    Intent intent = new Intent(MyPageActivity.this,LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(getApplicationContext(),"오류로 인해 탈퇴에 실패했습니다.",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
                builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        //뒤로가기 버튼
        goBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }
}
