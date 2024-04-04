package com.cbd.memify.template;

import com.cbd.memify.config.JwtService;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;
    private final JwtService jwtService;

    @GetMapping
    public ResponseEntity<List<Template>> getTemplates() {
        return ResponseEntity.ok(templateService.getTemplates());
    }

    @GetMapping("/search")
    public ResponseEntity<byte[]> getTemplateByName(@RequestParam String name) throws IOException {

        GridFSFile gridFsFile = templateService.findFileByName(name);
        if (Objects.isNull(gridFsFile)) {
            return ResponseEntity.notFound().build();
        }

        if (Objects.isNull(gridFsFile.getMetadata())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String contentType = gridFsFile.getMetadata().getString("_contentType");

        return ResponseEntity.ok().contentType(MediaType.valueOf(contentType)).body(templateService.getTemplateByName(name));
    }

    @PostMapping
    public ResponseEntity<Template> addTemplate(@RequestHeader("Authorization") String authHeader,
                                                @RequestPart String name,
                                                @RequestPart MultipartFile template) throws IOException {

        if (name.isBlank() || template.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (templateService.findFileByName(name) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        if (template.getContentType() != null && !template.getContentType().startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.addTemplate(username, name, template));
    }

}
