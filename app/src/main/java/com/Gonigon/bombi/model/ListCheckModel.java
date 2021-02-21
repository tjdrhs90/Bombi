package com.Gonigon.bombi.model;

public class ListCheckModel implements Comparable<ListCheckModel> {
//public class UserModel {

    public String email;
    public String userName;
    public String uid;
    public String area;
    public String age;
    public String sex;
    public String token;
    public String lastMessageTime;
    public String lastMessage;
    public Boolean read;
    public String chatBubbleWriter;

//    public UserModel(String email, String userName, String uid, String area, String age, String sex, String signupTime, String token, String lastMessageTime) {
//        this.email = email;
//        this.userName = userName;
//        this.uid = uid;
//        this.area = area;
//        this.age = age;
//        this.sex = sex;
//        this.signupTime = signupTime;
//        this.token = token;
//        this.lastMessageTime = lastMessageTime;
//    }
//
//    public UserModel() {
//
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public String getUserName() {
//        return userName;
//    }
//
//    public String getUid() {
//        return uid;
//    }
//
//    public String getArea() {
//        return area;
//    }
//
//    public String getAge() {
//        return age;
//    }
//
//    public String getSex() {
//        return sex;
//    }
//
//    public String getSignupTime() {
//        return signupTime;
//    }
//
//    public String getToken() {
//        return token;
//    }
//
//    public String getLastMessageTime() {
//        return lastMessageTime;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public void setUserName(String userName) {
//        this.userName = userName;
//    }
//
//    public void setUid(String uid) {
//        this.uid = uid;
//    }
//
//    public void setArea(String area) {
//        this.area = area;
//    }
//
//    public void setAge(String age) {
//        this.age = age;
//    }
//
//    public void setSex(String sex) {
//        this.sex = sex;
//    }
//
//    public void setSignupTime(String signupTime) {
//        this.signupTime = signupTime;
//    }
//
//    public void setToken(String token) {
//        this.token = token;
//    }
//
//    public void setLastMessageTime(String lastMessageTime) {
//        this.lastMessageTime = lastMessageTime;
//    }
//
    @Override
    public int compareTo(ListCheckModel listCheckModel) {

//        return lastMessageTime.compareTo(userModel.getLastMessageTime());
        return lastMessageTime.compareTo(listCheckModel.lastMessageTime);
    }


}
