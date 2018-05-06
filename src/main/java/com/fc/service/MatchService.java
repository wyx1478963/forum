package com.fc.service;


import com.fc.mapper.PostMapper;
import com.fc.model.ContentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MatchService {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private MediaPreProcessor mediaPreProcessor;

    @Resource
    private PostMapper postMapper;

    private static final List<ContentInfo> contentInfoPool = new ArrayList();

    public void loadContentInfoPool() {
        long start = System.currentTimeMillis();
        // get content title and id
        List<ContentInfo> userInfoList = postMapper.getAllContentInfo();
        synchronized (contentInfoPool) {
            contentInfoPool.clear();
            contentInfoPool.addAll(userInfoList);
        }

        long end = System.currentTimeMillis();
        logger.info(String.format("load UserContentPool in %dms", end - start));
    }

    public List<Integer> matchSimilarMediaById(int postId) {
        if (contentInfoPool.isEmpty())
            loadContentInfoPool();
        List<ContentInfo> contentInfos = contentInfoPool;
        ContentInfo contentInfoById = postMapper.getContentInfoByPostId(postId);
        if (contentInfoById == null)
            return null;
        double[] vector = mediaPreProcessor.convertMediaToVectorByTitle(contentInfoById);
        for (ContentInfo contentInfo : contentInfos) {
            double[] vectorEach = mediaPreProcessor.convertMediaToVectorByTitle(contentInfo);
            contentInfo.setVector(vectorEach);
            contentInfo.setDistance(calcDistanceBetweenUnitVectors(vector, vectorEach));
        }
        List<ContentInfo> contentInfosSort = sortByDistance(contentInfos);
        List<Integer> result = new ArrayList<>();
        contentInfosSort.forEach(o -> result.add(o.getId().intValue()));
        result.removeIf(contentInfo -> contentInfo.equals(postId));
        return result;
    }


    private List<ContentInfo> sortByDistance(List<ContentInfo> contentInfos) {
        Collections.sort(contentInfos, MatchService::compare);
        return contentInfos;
    }

    private static int compare(Object o1, Object o2) {
        ContentInfo s1 = (ContentInfo) o1;
        ContentInfo s2 = (ContentInfo) o2;
        if (s1.getDistance() < s2.getDistance())
            return 1;
        else if (s1.getDistance() == s2.getDistance())
            return 0;
        else
            return -1;
    }


    public double calcDistanceBetweenUnitVectors(double[] vector1, double[] vector2) {
        if (vector1 == null || vector2 == null)
            return 0;
        double result = 0.0;
        for (int i = 0; i < vector1.length; i++) {
            result += vector1[i] * vector2[i];
        }
        return result;
    }

}