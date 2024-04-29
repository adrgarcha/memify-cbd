package com.cbd.memify.template;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
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

        gridFsTemplate.find(new Query().addCriteria(Criteria.where("metadata.type").is("template"))).into(gridFsFiles);
        for (GridFSFile gridFsFile : gridFsFiles) {

            assert gridFsFile.getMetadata() != null;
            Template template = Template.builder()
                    .name(gridFsFile.getMetadata().getString("name"))
                    .build();
            templates.add(template);
        }

        return templates;
    }

    public byte[] getTemplateByName(String name) throws IOException {

        GridFSFile templateFile = findTemplateByName(name);
        if(templateFile == null)
            return null;

        return gridFsTemplate.getResource(templateFile).getInputStream().readAllBytes();
    }

    public Document getTemplateMetadataByName(String name) {
        GridFSFile templateFile = findTemplateByName(name);
        if(templateFile == null)
            return null;

        return templateFile.getMetadata();
    }

    public Template addTemplate(String name, MultipartFile template) throws IOException {

        DBObject metaData = new BasicDBObject();
        metaData.put("name", name);
        metaData.put("type", "template");

        gridFsTemplate.store(template.getInputStream(), name, template.getContentType(), metaData);

        return Template.builder().name(name).build();
    }

    public void deleteTemplateByName(String name) {
        gridFsTemplate.delete(new Query().addCriteria(Criteria.where("metadata.name").is(name)));
    }

    private GridFSFile findTemplateByName(String name) {
        return gridFsTemplate.findOne(new Query().addCriteria(Criteria.where("metadata.name").is(name)
                .and("metadata.type").is("template")));
    }

}
