package org.typing.forgerock.auth;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import nl.captcha.Captcha;
import nl.captcha.backgrounds.BackgroundProducer;
import nl.captcha.backgrounds.TransparentBackgroundProducer;
import nl.captcha.noise.CurvedLineNoiseProducer;
import nl.captcha.noise.NoiseProducer;
import nl.captcha.text.producer.TextProducer;
import nl.captcha.text.renderer.DefaultWordRenderer;
import nl.captcha.text.renderer.WordRenderer;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;


public class CaptchaGenerator {
    private BackgroundProducer backgroundProducer;
    private TextProducer textProducer;
    private WordRenderer wordRenderer;
    private NoiseProducer noiseProducer;

    private Captcha createSingleCaptcha(int width, int height, String phrase){
        try {
            afterPropertiesSet(phrase);
        }catch(Exception Ex){}


        return new Captcha.Builder(width, height).addBackground(backgroundProducer).addText(textProducer, wordRenderer)
                .addNoise(noiseProducer).build();
    }

    public BufferedImage createCaptcha(String phrase){
        int len = phrase.replace(" ", "").length();

        String [] words = phrase.split(" ");
        //BufferedImage [] captchas = new BufferedImage[words.length];
        BufferedImage result = new BufferedImage(600, 40, BufferedImage.TYPE_INT_ARGB);
        Graphics g = result.getGraphics();
        int x = 0;
        int y = 50;
        for (int i=0; i<words.length;i++) {
            Captcha captcha = this.createSingleCaptcha(words[i].length() * 25 + 1, 40, words[i]);
            if (words[i].length() == 1){
                captcha = this.createSingleCaptcha(words[i].length() * 25 + 3, 40, words[i]);
            }
            BufferedImage imagen = captcha.getImage();
            if ((x + (imagen.getWidth() -5)) > result.getWidth() ){
                BufferedImage result2 = new BufferedImage(700, y+50, BufferedImage.TYPE_INT_ARGB);
                Graphics g2 = result2.getGraphics();
                g2.drawImage(result,0,0, null);
                result = result2;
                g = result.getGraphics();
                y += 50;
                x = 0;
            }
            g.drawImage(imagen, x, y-50, null);
            x += imagen.getWidth() -5;

        }
        g.dispose();
        return result;
    }
    private void afterPropertiesSet(String phrase) throws Exception {
        this.backgroundProducer = new TransparentBackgroundProducer();

        this.textProducer = new PhraseTextProducer(phrase);

        this.wordRenderer = new DefaultWordRenderer();

        this.noiseProducer = new CurvedLineNoiseProducer();

    }
    public String encodeBase64(BufferedImage captcha) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(captcha, "png", outputStream);
            return DatatypeConverter.printBase64Binary(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
