package com.cbd.memify.meme;

import com.cbd.memify.user.User;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@AllArgsConstructor
public class MemeService {

    private final MemeRepository memeRepository;
    private final GridFsTemplate gridFsTemplate;

    private final Integer FONT_SIZE = 40;

    public List<MemeResponse> getAllMemes() {
        return mapMemes(memeRepository.findAll());
    }

    public Meme getMemeByName(String name) {
        return memeRepository.findByName(name).orElse(null);
    }

    public byte[] getMemeImageByName(String name) throws IOException {
        GridFSFile memeFile = gridFsTemplate.findOne(new Query(Criteria.where("metadata.name").is(name)
                .and("metadata.type").is("meme")));
        if(memeFile == null)
            return null;

        return gridFsTemplate.getResource(memeFile).getInputStream().readAllBytes();
    }

    public List<MemeResponse> getMemesByUserId(String username) {
        return mapMemes(memeRepository.findByUserId(username));
    }

    public MemeResponse addMeme(MemeRequest meme, User user, byte[] template) throws IOException {
        Meme newMeme = Meme.builder()
                .name(meme.getName())
                .templateName(meme.getTemplateName())
                .upperText(meme.getUpperText())
                .lowerText(meme.getLowerText())
                .user(user)
                .build();
        memeRepository.save(newMeme);

        byte[] memeImage = createMeme(template, meme.getUpperText(), meme.getLowerText());
        DBObject metadata = new BasicDBObject();
        metadata.put("name", newMeme.getName());
        metadata.put("type", "meme");
        gridFsTemplate.store(new ByteArrayInputStream(memeImage), newMeme.getId(), "image/png", metadata);

        return MemeResponse.builder()
                .name(newMeme.getName())
                .templateName(newMeme.getTemplateName())
                .username(newMeme.getUser().getUsername())
                .build();
    }

    public void deleteMemeByName(String name) {
        Meme meme = getMemeByName(name);

        memeRepository.delete(meme);
        gridFsTemplate.delete(new Query(Criteria.where("metadata.name").is(name)
                .and("metadata.type").is("meme")));
    }

    private List<MemeResponse> mapMemes(List<Meme> memes) {
        return memes.stream().map(meme -> MemeResponse.builder()
                .name(meme.getName())
                .templateName(meme.getTemplateName())
                .username(meme.getUser().getUsername())
                .build()).toList();
    }

    private byte[] createMeme(byte[] memeImage, String upperText, String lowerText) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(memeImage));
        int imageHeight = image.getHeight();
        int upperY = 75;
        int lowerY = imageHeight - 25;

        memeImage = processTextAndCreateImage(memeImage, upperText, upperY);
        memeImage = processTextAndCreateImage(memeImage, lowerText, lowerY);

        return memeImage;
    }

    private byte[] processTextAndCreateImage(byte[] memeImage, String text, int y) throws IOException {
        text = divideTextIntoBlocks(checkTextIsNotNullOrEmpty(text));
        String[] textLines = text.split("\n");
        int totalTextHeight = textLines.length * FONT_SIZE;
        y = y - totalTextHeight / 2;

        for (String line : textLines) {
            memeImage = getImageWithText(memeImage, line, y);
            y += 50;
        }

        return memeImage;
    }

    private String checkTextIsNotNullOrEmpty(String text) {
        if (text == null || text.isEmpty()) {
            return " ";
        }
        return text;
    }

    private String divideTextIntoBlocks(String text) {
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        int wordsInBlock = 4;

        for (int i = 0; i < words.length; i++) {
            sb.append(words[i]).append(" ");
            if ((i + 1) % wordsInBlock == 0) {
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private byte[] getImageWithText(byte[] imageBytes, String text, int y) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        Graphics2D graphics =  image.createGraphics();
        graphics.drawImage(image, 0, 0, null);

        Font font = new Font("Impact", Font.BOLD, FONT_SIZE);
        graphics.setColor(Color.WHITE);
        graphics.setFont(font);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int x = (image.getWidth() - graphics.getFontMetrics().stringWidth(text)) / 2;

        graphics.drawString(text, x, y);
        TextLayout layout = new TextLayout(text, font, graphics.getFontRenderContext());
        Shape outline = layout.getOutline(null);

        AffineTransform transform = graphics.getTransform();
        transform.translate(x, y);
        graphics.transform(transform);

        graphics.setColor(Color.BLACK);
        graphics.draw(outline);
        graphics.setClip(outline);

        graphics.dispose();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bos);
        return bos.toByteArray();
    }

}
