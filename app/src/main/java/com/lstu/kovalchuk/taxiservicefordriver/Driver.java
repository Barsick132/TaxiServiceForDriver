package com.lstu.kovalchuk.taxiservicefordriver;

public class Driver {
    private String Phone;
    private String FullName;
    private String BrandCar;
    private String ColorCar;
    private String NumberCar;
    private String AccountNumber;

    public Driver(String phone, String fullName, String brandCar, String colorCar, String numberCar, String accountNumber) {
        Phone = phone;
        FullName = fullName;
        BrandCar = brandCar;
        ColorCar = colorCar;
        NumberCar = numberCar;
        AccountNumber = accountNumber;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getBrandCar() {
        return BrandCar;
    }

    public void setBrandCar(String brandCar) {
        BrandCar = brandCar;
    }

    public String getColorCar() {
        return ColorCar;
    }

    public void setColorCar(String colorCar) {
        ColorCar = colorCar;
    }

    public String getNumberCar() {
        return NumberCar;
    }

    public void setNumberCar(String numberCar) {
        NumberCar = numberCar;
    }

    public String getAccountNumber() {
        return AccountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        AccountNumber = accountNumber;
    }
}
