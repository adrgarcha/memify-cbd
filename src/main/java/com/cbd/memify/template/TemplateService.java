package com.cbd.memify.template;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final GridFsTemplate gridFsTemplate;

    public List<Template> getTemplates() {
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

        return templates;
    }

    public byte[] getTemplateByName(String name) throws IOException {
        return gridFsTemplate.getResource(findFileByName(name)).getInputStream().readAllBytes();
    }

    public Template addTemplate(String username, String name, MultipartFile template) throws IOException {

        DBObject metaData = new BasicDBObject();
        metaData.put("username", username);
        metaData.put("name", name);

        gridFsTemplate.store(template.getInputStream(), name, template.getContentType(), metaData);

        return Template.builder().username(username).name(name).build();
    }

    public GridFSFile findFileByName(String name) {
        return gridFsTemplate.findOne(new Query().addCriteria(Criteria.where("metadata.name").is(name)));
    }

}