package com.cbd.memify.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse {

    private String id;
    private String username;
    private String email;
    private Role role;

}
