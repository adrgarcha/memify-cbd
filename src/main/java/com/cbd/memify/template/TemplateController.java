package com.cbd.memify.template;

import com.cbd.memify.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<Template>> getTemplates() {
        return templateService.getTemplates();
    }

    @GetMapping("/search")
    public ResponseEntity<byte[]> getTemplateByName(@RequestParam String name) throws IOException {
        return templateService.getTemplateByName(name);
    }

    @PostMapping
    public ResponseEntity<Template> addTemplate(@RequestHeader("Authorization") String authHeader,
                                                @RequestPart String name,
                                                @RequestPart MultipartFile template) throws IOException {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        return templateService.addTemplate(username, name, template);
    }

}
