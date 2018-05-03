package com.fc.model;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.word2vec.Word2Vec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class RecommendModel {
    private Word2Vec model;
    public RecommendModel() {
//        long start = System.currentTimeMillis();
//        try {
//            File file = new File(this.getClass().getClassLoader().getResource("embedding.model").getPath());
//            System.out.println(file.getPath());
//            Word2Vec recommendModel = WordVectorSerializer.readWord2VecModel(file);
//            long end = System.currentTimeMillis();
//            Logger logger = LoggerFactory.getLogger(com.sohu.CFRecommend.data.model.RecommendModel.class);
//            logger.info(String.format("Model file loaded in %dms.", end - start));
//            this.model = recommendModel;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
    public Word2Vec getModel() {
        return model;
    }
}