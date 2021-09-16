package com.ankush.data.entities;

import lombok.*;

import javax.persistence.*;
import java.util.List;

@Table(name = "purchase_invoice")
@Entity

public class PurchaseInvoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "purchaseparty_id")
    private PurchaseParty purchasePart;

    private float nettotal;
    private float transporting;
    private float wages;
    private float other;
    private float discount;
    @Transient
    private float grandtotal;
    private float paid;
    @ManyToOne
    @JoinColumn(name = "bank_id")
    private Bank bank;
    @ManyToOne
    @JoinColumn(name = "login_id")
    private Login login;
    @OneToMany(mappedBy = "invoice",cascade = CascadeType.ALL,fetch = FetchType.EAGER)
    private List<PurchaseTransaction> transactions;

    public PurchaseInvoice() {
        super();
    }

    public PurchaseInvoice(PurchaseParty purchasePart, float nettotal, float transporting, float wages, float other, float discount, float paid, Bank bank, Login login, List<PurchaseTransaction> transactions) {
        this.purchasePart = purchasePart;
        this.nettotal = nettotal;
        this.transporting = transporting;
        this.wages = wages;
        this.other = other;
        this.discount = discount;
        this.paid = paid;
        this.bank = bank;
        this.login = login;
        this.transactions = transactions;
        this.grandtotal = (this.nettotal-this.discount)+this.transporting+this.other+this.wages;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PurchaseParty getPurchasePart() {
        return purchasePart;
    }

    public void setPurchasePart(PurchaseParty purchasePart) {
        this.purchasePart = purchasePart;
    }

    public float getNettotal() {
        return nettotal;
    }

    public void setNettotal(float nettotal) {
        this.nettotal = nettotal;
    }

    public float getTransporting() {
        return transporting;
    }

    public void setTransporting(float transporting) {
        this.transporting = transporting;
    }

    public float getWages() {
        return wages;
    }

    public void setWages(float wages) {
        this.wages = wages;
    }

    public float getOther() {
        return other;
    }

    public void setOther(float other) {
        this.other = other;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public float getGrandtotal() {
        return (this.nettotal-this.discount)+this.transporting+this.other+this.wages;
    }

    public void setGrandtotal(float grandtotal) {
        this.grandtotal = grandtotal;
    }

    public float getPaid() {
        return paid;
    }

    public void setPaid(float paid) {
        this.paid = paid;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public Login getLogin() {
        return login;
    }

    public void setLogin(Login login) {
        this.login = login;
    }

    public List<PurchaseTransaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<PurchaseTransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "PurchaseInvoice{" +
                "id=" + id +
                ", purchasePart=" + purchasePart +
                ", nettotal=" + nettotal +
                ", transporting=" + transporting +
                ", wages=" + wages +
                ", other=" + other +
                ", discount=" + discount +
                ", grandtotal=" + grandtotal +
                ", paid=" + paid +
                ", bank=" + bank +
                ", login=" + login +
                ", transactions=" + transactions +
                '}';
    }
}