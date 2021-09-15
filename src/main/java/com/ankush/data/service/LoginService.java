package com.ankush.data.service;

import com.ankush.data.entities.Login;
import com.ankush.data.repository.LoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginService {
    @Autowired
    LoginRepository repository;
    public List<String>getAllUserNames()
    {
        return repository.getAllUserNames();
    }
    public int saveLogin(Login login){
        repository.save(login);
        return 1;
    }
    public int validate(String username,String password)
    {
        Login l = repository.getByUsername(username);
        if(l.getPassword().equals(password))
            return 1;
        else
            return 0;
    }
    public Login getLoginByUserName(String userName)
    {
        return repository.getByUsername(userName);
    }
}
