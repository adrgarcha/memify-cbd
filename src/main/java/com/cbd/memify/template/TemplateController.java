package com.cbd.memify.template;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/templates")
@SecurityRequirement(name = "Authorization")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    public ResponseEntity<List<Template>> getTemplates() {
        return ResponseEntity.ok(templateService.getTemplates());
    }

    @GetMapping("/{templateName}")
    public ResponseEntity<byte[]> getTemplateByName(@PathVariable String templateName) throws IOException {

        byte[] template = templateService.getTemplateByName(templateName);
        if (Objects.isNull(template))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found");

        Document templateMetadata = templateService.getTemplateMetadataByName(templateName);
        if (Objects.isNull(templateMetadata))
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Metadata not found");

        String contentType = templateMetadata.getString("_contentType");

        return ResponseEntity.ok().contentType(MediaType.valueOf(contentType)).body(template);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Template> addTemplate(@RequestPart String name,
                                                @RequestPart MultipartFile template) throws IOException {

        if (name.isBlank() || template.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name and template are required");

        if (name.contains(" "))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template name cannot contain spaces");

        if (templateService.getTemplateByName(name) != null)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Template with this name already exists");

        if (template.getContentType() != null && !template.getContentType().startsWith("image/"))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only images are supported");

        if (template.getContentType().startsWith("image/gif"))
            throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "GIFs are not supported");

        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.addTemplate(name, template));
    }

    @DeleteMapping("/{templateName}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable String templateName) throws IOException {

        byte[] template = templateService.getTemplateByName(templateName);
        if (Objects.isNull(template))
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Template not found");

        templateService.deleteTemplateByName(templateName);
        return ResponseEntity.noContent().build();
    }

}
