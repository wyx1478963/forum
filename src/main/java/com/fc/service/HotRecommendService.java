package com.fc.service;


import com.fc.mapper.PostMapper;
import com.fc.mapper.UserViewHistoryMapper;
import com.fc.model.UserViewHistory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
//todo: finish
public class HotRecommendService {

    @Autowired
    UserViewHistoryMapper userViewHistoryMapper;

    @Autowired
    PostMapper postMapper;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final List<UserViewHistory> userViewHistoryPool = new ArrayList<>();
    private static final Map<Integer, List<Integer>> topicId2PostIdListMap = new HashMap<>();

    /**
     * 加载用户浏览记录
     */
    public void loadUserInfoPool() {
        long start = System.currentTimeMillis();
        List<UserViewHistory> userViewHistoryList = userViewHistoryMapper.getAllUserViewHistory();
        synchronized (userViewHistoryPool) {
            userViewHistoryPool.clear();
            userViewHistoryPool.addAll(userViewHistoryList);
        }

        long end = System.currentTimeMillis();
        logger.info(String.format("load UserInfosPool in %dms", end - start));
    }

    /**
     * 根据访问记录 获得最热文章
     *
     * @param topicId
     * @return
     */
    public List<Integer> getHotPostByTopic(int topicId) {
        if (userViewHistoryPool.isEmpty())
            loadUserInfoPool();

        List<Integer> postTopicList = new ArrayList<>();
        if (topicId2PostIdListMap.containsKey(topicId)) {
            postTopicList = topicId2PostIdListMap.get(topicId);
        }
        if ( postTopicList.isEmpty()) {
            postTopicList = postMapper.listPostOfTopic(topicId);
            synchronized (topicId2PostIdListMap) {
                topicId2PostIdListMap.put(topicId, postTopicList);
            }
        }

        Map<Integer, Integer> postVisitStatMap = new HashMap<>();

        for (UserViewHistory userInfo : userViewHistoryPool) {
            int postId = userInfo.getPid();
            if (postTopicList.contains(postId)) {
                if (!postVisitStatMap.containsKey(postId))
                    postVisitStatMap.put(postId, 1);
                else
                    postVisitStatMap.put(postId, postVisitStatMap.get(postId) + 1);
            }
        }

        List<Map.Entry<Integer, Integer>> entryList = new ArrayList<>(postVisitStatMap.entrySet());
        entryList.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        List<Integer> resultPostId = entryList.stream().map(Map.Entry::getKey).collect(Collectors.toCollection(ArrayList::new));

        return resultPostId;
    }

    public List<Integer> getPostListByModelAndTopic(String modelResult, int topicId) {
        String[] postIdStrArray = modelResult.split(" ");
        List<Integer> postIdListFromModel = new ArrayList<>();
        CollectionUtils.collect(Arrays.asList(postIdStrArray), new Transformer() {
            @Override
            public Object transform(Object o) {
                return Integer.parseInt(o.toString());
            }
        }, postIdListFromModel);

        List<Integer> postTopicList = new ArrayList<>();
        if (topicId2PostIdListMap.containsKey(topicId)) {
            postTopicList = topicId2PostIdListMap.get(topicId);
        }
        if (postTopicList.isEmpty()) {
            postTopicList = postMapper.listPostOfTopic(topicId);
            synchronized (topicId2PostIdListMap) {
                topicId2PostIdListMap.put(topicId, postTopicList);
            }
        }

        postIdListFromModel.retainAll(postTopicList);
        return postIdListFromModel;
    }


}
