package com.example.nirbhay.Users;


public class User {
    private String email, userId;
    private String name, gender, age, address, phoneNumber;
    private String con_name, con_number;
    int badge;
    public User(){

    }
    public User(String userId, String email, String name, String gender, String age, String address, String con_name, String con_number, String phoneNumber){
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.con_name = con_name;
        this.con_number = con_number;
        this.badge = 0;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }



    public String getAddress() {
        return address;
    }

    public String getAge() {
        return age;
    }

    public String getCon_name() {
        return con_name;
    }

    public String getCon_number() {
        return con_number;
    }

    public String getGender() {
        return gender;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setCon_name(String con_name) {
        this.con_name = con_name;
    }

    public void setCon_number(String con_number) {
        this.con_number = con_number;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
