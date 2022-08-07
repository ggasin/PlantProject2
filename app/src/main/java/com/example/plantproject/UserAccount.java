package com.example.plantproject;

public class UserAccount {
    private String idToken; //firebase UID 고유 토큰 정보
    private String name;
    private String nickName;
    private String tel;
    private String emailId;
    private String pwd;
    private String qr;
    public UserAccount() { }

    public String getNickName() {return nickName;}

    public void setNickName(String nickName) {this.nickName = nickName;}

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }


}
