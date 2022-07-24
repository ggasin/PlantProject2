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
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {
    private IntentIntegrator qrScan;
    private TextView qrResultText,canUseIdText,checkPwdText, goLoginTextbtn,checkOverSixPwdText,canUseNameText, canUseTelText;
    private Button qrScanBtn,registerBtn;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private EditText editEmailId,regEditPwd,editName,editTel,checkEditPwd;
    boolean isNameOK,isTelOK,isEmailIdOK,isPwdOK,isPwdCheckOK,isQrOK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editName = findViewById(R.id.edit_name);
        editTel = findViewById(R.id.edit_tel);
        editEmailId = findViewById(R.id.reg_edit_emailId);
        canUseIdText = findViewById(R.id.canUseIdText);
        regEditPwd = findViewById(R.id.reg_edit_pwd);
        checkEditPwd = findViewById(R.id.edit_CheckPwd);
        checkPwdText = findViewById(R.id.checkPwdText);
        goLoginTextbtn = findViewById(R.id.goLoginText_btn);
        registerBtn = findViewById(R.id.register_btn);
        canUseNameText = findViewById(R.id.canUseNameText);
        canUseTelText = findViewById(R.id.canUseTelText);
        checkOverSixPwdText = findViewById(R.id.check_over_six_pwd);

        //qr스캔 관련 변수
        qrScanBtn = (Button)findViewById(R.id.qrScan_btn);
        qrResultText = (TextView) findViewById(R.id.qrCodeResult);
        qrScan = new IntentIntegrator(this);

        //파이어베이스 관련 변수
        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("plant");

        //이메일형식인지 확인하는데 사용
        Pattern pattern = Patterns.EMAIL_ADDRESS;


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
                canUseIdText.setVisibility(View.VISIBLE);
                if(editEmailId.getText().toString().equals("tmdals028@naver.com")||
                        !pattern.matcher(editEmailId.getText().toString()).matches()){
                    canUseIdText.setText("사용 불가한 아이디입니다.");
                    canUseIdText.setTextColor(Color.parseColor("#FF0000")); //빨간색
                    isEmailIdOK = false;
                }
                else{
                    canUseIdText.setText("사용 가능한 아이디입니다.");
                    canUseIdText.setTextColor(Color.parseColor("#08A600")); //초록색
                    isEmailIdOK = true;
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

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                checkOverSixPwdText.setVisibility(View.VISIBLE);
                if(regEditPwd.getText().length()<6){
                    checkOverSixPwdText.setText("비밀번호는 6자리 이상이어야 합니다.");
                    checkOverSixPwdText.setTextColor(Color.parseColor("#FF0000"));
                    isPwdOK = false;
                }
                else{
                    checkOverSixPwdText.setText("올바른 비밀번호 형식입니다.");
                    checkOverSixPwdText.setTextColor(Color.parseColor("#08A600"));
                    isPwdOK = true;
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (regEditPwd.getText().toString().equals("")){
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
                String strEmailId = editEmailId.getText().toString();
                String strTel = editTel.getText().toString();
                String strPwd = regEditPwd.getText().toString();
                String strQr = qrResultText.getText().toString();

                mFirebaseAuth.createUserWithEmailAndPassword(strEmailId,strPwd).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if(isEmailIdOK && isNameOK && isTelOK && isPwdOK && isPwdCheckOK && isQrOK){
                            if(task.isSuccessful()){
                                FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                                UserAccount account = new UserAccount();
                                HashMap<Object,Integer> sensors_child = new HashMap<>();
                                sensors_child.put("Hum",0);
                                sensors_child.put("Temp",0);
                                sensors_child.put("soil_hum",0);
                                HashMap<Object,String>  auto_nonauto_child= new HashMap<>();
                                auto_nonauto_child.put("LED","OFF");
                                auto_nonauto_child.put("cooler","OFF");
                                auto_nonauto_child.put("water","OFF");
                                account.setEmailId(firebaseUser.getEmail()); // 로그인을 하는 정확한 이메일이 필요하기 때문에 firebaseUser에서 정보를 가져옴
                                account.setPwd(strPwd);
                                account.setName(strName);
                                account.setTel(strTel);
                                account.setQr(strQr);
                                mDatabaseRef.child(account.getQr()).child("UserAccount").setValue(account);
                                mDatabaseRef.child(account.getQr()).child("operation").child("sensors").setValue(sensors_child);
                                mDatabaseRef.child(account.getQr()).child("operation").child("button").child("auto").setValue(auto_nonauto_child);
                                mDatabaseRef.child(account.getQr()).child("operation").child("button").child("nonauto").setValue(auto_nonauto_child);
                                Toast.makeText(getApplicationContext(),"가입성공",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this , LoginActivity.class);
                                startActivity(intent);

                            }
                            else{
                                Toast.makeText(getApplicationContext(),"가입이 왜 안될까",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else{
                            Toast.makeText(getApplicationContext(),"가입실패",Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });

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