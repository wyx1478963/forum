package com.fc.service;
import com.fc.model.ContentInfo;
import com.fc.model.RecommendModel;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import org.deeplearning4j.parallelism.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class MediaPreProcessor {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private Set<String> stopWordSet = new ConcurrentHashSet<>();
    private static final Map<String, double[]> wordVectorCache = new HashMap<>();
    @Resource
    private RecommendModel recommendModel;
    private JiebaSegmenter segmenter = new JiebaSegmenter();
    public MediaPreProcessor() {
        loadStopWordSet();
    }
    public void loadStopWordSet() {
        try {
            long start = System.currentTimeMillis();
//            BufferedReader tfIdfRead = new BufferedReader(new FileReader("src/main/resources/stopwords.txt"));
            BufferedReader tfIdfRead = new BufferedReader(new InputStreamReader(MediaPreProcessor.class.getClassLoader().getResourceAsStream("stopwords.txt")));
            String str = null;
            while ((str = tfIdfRead.readLine()) != null) {
                stopWordSet.add(str);
            }
            stopWordSet.add(" ");
            long end = System.currentTimeMillis();
            logger.info(String.format("Stop words loaded in %dms.", end - start));
        } catch (IOException e) {
            logger.info("Stop words load failed");
        }
    }
    public double[] convertMediaToVectorByTitle(ContentInfo contentInfo) {
        String content = contentInfo.getTitle();
        Map<String, Integer> words = splitTextToWords(content);
        return convertWordMapToArticleVector(words);
    }
    public double[] convertMediaToVectorByInfo(ContentInfo contentInfo) {
        String content = contentInfo.getContent();
        Map<String, Integer> words = splitTextToWords(content);
        return convertWordMapToArticleVector(words);
    }
    public Map<String, Integer> splitTextToWords(String content) {
        if (stopWordSet.isEmpty()) {
            loadStopWordSet();
        }
        List<SegToken> tokens = segmenter.process(content, JiebaSegmenter.SegMode.SEARCH);
        List<String> words = tokens.stream().map(segToken -> segToken.word).collect(Collectors.toList());
        words.removeAll(stopWordSet);
        Map<String, Integer> wordMap = toWordMap(words);
        return wordMap;
    }
    public Map<String, Integer> toWordMap(List<String> words) {
        Map<String, Integer> wordMap = new HashMap<>();
        for (String word : words) {
            if (!wordMap.containsKey(word)) {
                wordMap.put(word, 1);
            } else {
                wordMap.put(word, wordMap.get(word) + 1);
            }
        }
        return wordMap;
    }
    public double[] convertWordMapToArticleVector(Map<String, Integer> wordMap) {
        List<double[]> wordVectors = getWordVectors(wordMap);
        if (wordVectors.size() == 0) {
            return null;
        } else {
            double[] textVector = wordVectorsToTextVector(wordVectors);
            textVector = normalization(textVector);
            return textVector;
        }
    }
    private List<double[]> getWordVectors(Map<String, Integer> wordMap) {
        List<double[]> wordVectors = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            String word = entry.getKey();
            int wordFrequency = entry.getValue();
            double[] wordVector = convertWordToVector(word);
            if (wordVector == null)
                continue;
            if (wordFrequency > 1)
                wordVector = wordWeighted(wordVector, wordFrequency);
            wordVectors.add(wordVector);
        }
        return wordVectors;
    }
    private double[] wordVectorsToTextVector(List<double[]> wordVectors) {
        int length = wordVectors.get(0).length;
        double[] textVector = new double[length];
        for (int i = 0; i < length; i++) {
            textVector[i] = 0.0;
        }
        for (double[] wordVector : wordVectors) {
            for (int i = 0; i < length; i++) {
                textVector[i] += wordVector[i];
            }
        }
        return textVector;
    }
    private double[] convertWordToVector(String word) {
        if (!wordVectorCache.containsKey(word)) {
            double[] wordVector = new double[]{};
            try {
                double[] getWordVector = recommendModel.getModel().getWordVector(word);
                wordVector = getWordVector;
                if (wordVector == null) return null;
                synchronized (wordVectorCache) {
                    wordVectorCache.put(word, wordVector);
                }
            } catch (Exception e) {
                return null;
            }
        }
        return wordVectorCache.get(word);
    }
    /**
     * 相同词的词向量计算，词向量乘以词频
     *
     * @param wordVector
     * @param wordFrequency
     * @return
     */
    private double[] wordWeighted(double[] wordVector, Integer wordFrequency) {
        double[] wordVectorWeighted = new double[wordVector.length];
        for (int i = 0; i < wordVector.length; i++) {
            wordVectorWeighted[i] = wordVector[i] * wordFrequency;
        }
        return wordVectorWeighted;
    }
    /**
     * 将向量归一化
     *
     * @param textVector
     * @return
     */
    public double[] normalization(double[] textVector) {
        double[] normalizationVector = new double[textVector.length];
        Double textVectorLength = 0.0;
        for (Double num : textVector)
            textVectorLength += num * num;
        textVectorLength = Math.sqrt(textVectorLength);
        for (int i = 0; i < textVector.length; i++) {
            normalizationVector[i] = textVector[i] / textVectorLength;
        }
        return normalizationVector;
    }
}