package com.cbd.memify.meme;

import com.cbd.memify.config.JwtService;
import com.cbd.memify.template.TemplateService;
import com.cbd.memify.user.User;
import com.cbd.memify.user.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/memes")
@SecurityRequirement(name = "Authorization")
@AllArgsConstructor
public class MemeController {

    private final MemeService memeService;
    private final UserService userService;
    private final JwtService jwtService;
    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<List<MemeResponse>> getAllMemes() {
        return ResponseEntity.ok(memeService.getAllMemes());
    }

    @GetMapping("/{name}")
    public ResponseEntity<byte[]> getMemeByName(@PathVariable String name) throws IOException {

        byte[] meme = memeService.getMemeImageByName(name);
        if(meme == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Meme not found");

        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(meme);
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<MemeResponse>> getMemesByUsername(@PathVariable String username) {

        User user = userService.getUserByUsername(username);
        if(user == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");

        return ResponseEntity.ok(memeService.getMemesByUserId(user.getId()));
    }

    @PostMapping
    public ResponseEntity<MemeResponse> addMeme(@RequestHeader("Authorization") String authHeader, @RequestBody MemeRequest meme) throws IOException {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        User user = userService.getUserByUsername(username);
        byte[] template = templateService.getTemplateByName(meme.getTemplateName());

        if(meme.getName().contains(" "))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meme name cannot contain spaces");

        if(memeService.getMemeByName(meme.getName()) != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Meme with this name already exists");

        if(template == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found");

        return ResponseEntity.status(HttpStatus.CREATED).body(memeService.addMeme(meme, user, template));
    }

    @DeleteMapping("/{memeName}")
    public ResponseEntity<Void> deleteMemeByName(@RequestHeader("Authorization") String authHeader, @PathVariable String memeName) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        Meme meme = memeService.getMemeByName(memeName);

        if(meme == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Meme not found");

        if(!meme.getUser().getUsername().equals(username))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete your own memes");

        memeService.deleteMemeByName(memeName);
        return ResponseEntity.noContent().build();
    }

}
