package com.example.merabills;

public class DataModel {
    int cost;
    String bankName;
    String refrence;

    public DataModel(int cost, String bankName, String refrence) {
        this.cost = cost;
        this.bankName = bankName;
        this.refrence = refrence;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }


    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getRefrence() {
        return refrence;
    }

    public void setRefrence(String refrence) {
        this.refrence = refrence;
    }
}
