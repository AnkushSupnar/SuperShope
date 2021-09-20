package com.ankush.data.service;
import com.ankush.data.entities.PurchaseParty;
import com.ankush.data.repository.PurchasePartyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchasePartyService {
    @Autowired
    private PurchasePartyRepository repository;
    public List<PurchaseParty>getAllPurchaseParty()
    {
        return repository.findAll();
    }
    public int saveParty(PurchaseParty party){
        System.out.println(party.getId());
        if(party.getId()==null)
        {
            repository.save(party);
            return 1;
        }
        else
        {
            repository.save(party);
            return 2;
        }

    }
    public List<String>getAllPurchasePartyNames() {
        return repository.getAllPurchasePartyNames();
    }
    public PurchaseParty getPartyByName(String name)
    {
        return repository.getByName(name);
    }
}
