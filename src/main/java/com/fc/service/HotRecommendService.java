package com.fc.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
//todo: finish
public class HotRecommendService {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Resource
    private MpLiveMapper mpLiveMapper;

    @Resource
    private FeedTagRelationMapper feedTagRelationMapper;

    private static final List<UserInfo> userInfosPool = new ArrayList();
    private static final HashMap<Long, Long> userViewHistory = new HashMap<>();
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public List<UserInfo> GetIdFromUserRecharge() {
        return mpLiveMapper.ids();
    }

    public List<String> getRecommendById(Long id) {
        return new ArrayList<>();
    }

    /**
     * 加载用户浏览记录
     */
    public void loadUserInfoPool() {
        long start = System.currentTimeMillis();
//        LocalDateTime standardTime = LocalDateTime.now();
//        String beginTime = dateTimeFormatter.format(standardTime.minusDays(7));
//        Timestamp startTime = Timestamp.valueOf(beginTime);
        List<UserInfo> userInfoList = mpLiveMapper.getUserInfoList();
        synchronized (userInfosPool) {
            userInfosPool.clear();
            userInfosPool.addAll(userInfoList);
        }

        long end = System.currentTimeMillis();
        logger.info(String.format("load UserInfosPool in %dms", end - start));
    }

    public List<Long> getHotMediaFeed(HashSet<Long> feedTagRelationsList) {
        HashMap<Long, Integer> hotBrowseByTag = new HashMap<>();
        List<Long> feedList = new ArrayList<>();
        if (userInfosPool.isEmpty())
            loadUserInfoPool();
        for (UserInfo userInfo : userInfosPool) {
            Long feedId = userInfo.getFeedId();
            if (feedTagRelationsList.contains(feedId)) {
                if (!hotBrowseByTag.containsKey(feedId))
                    hotBrowseByTag.put(feedId, 1);
                else
                    hotBrowseByTag.put(feedId, hotBrowseByTag.get(feedId) + 1);
            }
        }
        List<Map.Entry<Long, Integer>> hotBrowses = getHotBrowse(hotBrowseByTag);
        hotBrowses.forEach(o -> {
            feedList.add(o.getKey());
        });
        return feedList;
    }

    public List<Map.Entry<Long, Integer>> getHotBrowse(HashMap<Long, Integer> hotBrowse) {
        List<Map.Entry<Long, Integer>> hotBrowses = new ArrayList<Map.Entry<Long, Integer>>(hotBrowse.entrySet());
        Collections.sort(hotBrowses, new Comparator<Map.Entry<Long, Integer>>() {
            public int compare(Map.Entry<Long, Integer> o1, Map.Entry<Long, Integer> o2) {
                return (o2.getValue() - o1.getValue());
            }
        });
        return hotBrowses;
    }

    public List<Long> getHotMediaFeedByChannelId(Long channelId) {
        List<Long> tagList = feedTagRelationMapper.getFeedTagByChannelId(channelId);
        HashSet<Long> feedTagRelationsByChannel = new HashSet<>();
        for (Long tag : tagList) {
            feedTagRelationsByChannel.addAll(feedTagRelationMapper.getFeedTagRelationByTag(tag));
        }
        return getHotMediaFeed(feedTagRelationsByChannel);
    }

    public List<Long> getHotMediaFeedByTagId(Long tagId) {
        long start = System.currentTimeMillis();
        HashSet<Long> feedTagRelationsByTag = feedTagRelationMapper.getFeedTagRelationByTag(tagId);
        long end = System.currentTimeMillis();
        logger.info(String.format("getFeedTagRelationByTag in %dms", end - start));
        return getHotMediaFeed(feedTagRelationsByTag);
    }

    public List<Long> getAllHotMediaFeed() {
        HashSet<Long> feedTagRelationsByTag = feedTagRelationMapper.getAllFeedTagRelation();
        return getHotMediaFeed(feedTagRelationsByTag);
    }


    public void calcUserViewHistory() {
        if (userInfosPool.isEmpty())
            System.out.println("userInfo.isEmpty()");
            loadUserInfoPool();
        for (UserInfo userInfo : userInfosPool) {
            Long userId = userInfo.getUserId();
            if (!userViewHistory.containsKey(userId))
                userViewHistory.put(userId, 1L);
            else
                userViewHistory.put(userId, userViewHistory.get(userId) + 1);
        }
    }

    public Boolean isOldUser(Long id) {
        if (userViewHistory.isEmpty())
            calcUserViewHistory();
        if (!userViewHistory.containsKey(id))
            return false;
        else if (userViewHistory.get(id) >= 5)
            return true;
        else
            return false;

    }

    public List<Long> filterWithTagOrChannel(String result, Long tagId, Long channelId) {
        List<Long> resultList = new ArrayList<>();
        String[] arr = result.split(" ");
        Long[] num = new Long[arr.length];
        for (int i = 0; i < arr.length; i++) {
            num[i] = Long.parseLong(arr[i]);
        }
        List<Long> arrayList = Arrays.asList(num);
        HashSet<Long> feedByTagOrChannel = new HashSet<>();
        if (tagId != -1)
            feedByTagOrChannel = feedTagRelationMapper.getFeedTagRelationByTag(tagId);
        else if (channelId != -1) {
            List<Long> feedTagList = feedTagRelationMapper.getFeedTagByChannelId(channelId);
            for (Long tag : feedTagList) {
                feedByTagOrChannel.addAll(feedTagRelationMapper.getFeedTagRelationByTag(tag));
            }
        }
        resultList.addAll(arrayList);
        resultList.retainAll(feedByTagOrChannel);
        return resultList;
    }

}
