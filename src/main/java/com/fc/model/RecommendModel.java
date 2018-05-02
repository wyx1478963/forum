package com.fc.model;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.springframework.stereotype.Component;

@Component
public class RecommendModel {
    private Word2Vec model;
    public RecommendModel() {

    }
    public Word2Vec getModel() {
        return model;
    }
}