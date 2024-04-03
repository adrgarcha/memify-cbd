package com.cbd.memify.template;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final GridFsTemplate gridFsTemplate;

    public ResponseEntity<List<Template>> getTemplates() {
        List<GridFSFile> gridFsFiles = new ArrayList<>();
        List<Template> templates = new ArrayList<>();

        gridFsTemplate.find(new Query()).into(gridFsFiles);
        for (GridFSFile gridFsFile : gridFsFiles) {

            assert gridFsFile.getMetadata() != null;
            Template template = Template.builder()
                    .name(gridFsFile.getMetadata().getString("name"))
                    .username(gridFsFile.getMetadata().getString("username"))
                    .build();
            templates.add(template);
        }

        return ResponseEntity.ok(templates);
    }

    public ResponseEntity<byte[]> getTemplateByName(String name) throws IOException {
        GridFSFile gridFsFile = findFileByName(name);

        if (Objects.isNull(gridFsFile)) {
            return ResponseEntity.notFound().build();
        }

        GridFsResource gridFsImage = gridFsTemplate.getResource(gridFsFile);
        byte[] image = gridFsImage.getInputStream().readAllBytes();

        return ResponseEntity.ok().contentType(MediaType.valueOf(gridFsImage.getContentType())).body(image);
    }

    public ResponseEntity<Template> addTemplate(String username, String name, MultipartFile template) throws IOException {

        if (name.isBlank() || template.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (findFileByName(name) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String contentType = template.getContentType();
        if (contentType != null && !contentType.startsWith("image/")) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }

        DBObject metaData = new BasicDBObject();
        metaData.put("username", username);
        metaData.put("name", name);

        gridFsTemplate.store(template.getInputStream(), name, contentType, metaData);
        Template newTemplate = Template.builder().username(username).name(name).build();

        return ResponseEntity.status(HttpStatus.CREATED).body(newTemplate);
    }

    private GridFSFile findFileByName(String name) {
        return gridFsTemplate.findOne(new Query().addCriteria(Criteria.where("metadata.name").is(name)));
    }

}
