package com.example.asb.smartwallet;

public class Transaction {
    public Integer amount,type, id;
    public String timestamp;
    public Transaction(){
        id = 0;
        amount = 0;
        type = -1;
        timestamp = null;
    }
    public Transaction(Integer amt, Integer typ, String timstp, Integer id){
        this.id = id;
        this.amount = amt;
        this.type = typ;
        this.timestamp = timstp;
    }
    public Transaction(Integer amt, Integer typ, String timstp){
        this.id = 0;
        this.amount = amt;
        this.type = typ;
        this.timestamp = timstp;
    }

}
