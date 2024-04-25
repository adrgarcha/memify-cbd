package com.cbd.memify.meme;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserMemeDTO {

    private String name;
    private String templateName;
    private String username;

}
