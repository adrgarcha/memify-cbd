package com.cbd.memify.auth;

import lombok.Data;

@Data
public class LoginRequest {

    private String username;
    private String password;

}
