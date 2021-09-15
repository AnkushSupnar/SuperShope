package com.ankush.common;

import com.ankush.data.entities.Login;
import org.springframework.stereotype.Component;

@Component
public class CommonData {
    public static Login loginUser;

    public static Login getLoginUser() {
        return loginUser;
    }

    public static void setLoginUser(Login loginUser) {
        CommonData.loginUser = loginUser;
    }
}
