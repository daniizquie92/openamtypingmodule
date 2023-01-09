package org.typing.forgerock.auth;

import nl.captcha.text.producer.TextProducer;

public class PhraseTextProducer implements TextProducer{
    private String phrase;

    public PhraseTextProducer(String phrase){
        this.phrase = phrase;
    }
    @Override
    public String getText() {
        return this.phrase;
    }
}
