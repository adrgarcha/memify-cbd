package com.cbd.memify.meme;

import com.cbd.memify.user.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Data
@Builder
@Document
public class Meme {

    @Id
    private String id;

    @Indexed(unique = true)
    private String name;

    private String templateName;

    private String upperText;

    private String lowerText;

    @DocumentReference
    private User user;

}
