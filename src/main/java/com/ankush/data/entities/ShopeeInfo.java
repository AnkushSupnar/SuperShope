package com.ankush.data.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
public class ShopeeInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String shopeeContact;
    private String shopeeName;
    private String shopeeOwnerName;
    private String address;

    public ShopeeInfo() {
    }

    public ShopeeInfo(String shopeeContact, String shopeeName, String shopeeOwnerName,String address) {
        this.shopeeContact = shopeeContact;
        this.shopeeName = shopeeName;
        this.shopeeOwnerName = shopeeOwnerName;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public String getShopeeContact() {
        return shopeeContact;
    }

    public String getShopeeName() {
        return shopeeName;
    }

    public String getShopeeOwnerName() {
        return shopeeOwnerName;
    }
    public String getAddress(){
        return address;
    }

    public void setShopeeContact(String shopeeContact) {
        this.shopeeContact = shopeeContact;
    }

    public void setShopeeName(String shopeeName) {
        this.shopeeName = shopeeName;
    }

    public void setShopeeOwnerName(String shopeeOwnerName) {
        this.shopeeOwnerName = shopeeOwnerName;
    }
    public void setAddress(String address){
        this.address = address;
    }
}
