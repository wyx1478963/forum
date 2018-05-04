package com.fc.controller;

import com.fc.service.ALSFilterService;
import com.fc.service.HotRecommendService;
import com.fc.service.MatchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/")
public class RecommendController {

    @Autowired
    ALSFilterService alsFilterService;

    @Autowired
    HotRecommendService hotRecommendService;

    @Autowired
    MatchService matchService;

    private static final Logger logger = LoggerFactory.getLogger(RecommendController.class);

    /**
     * 老用户推荐流
     *
     * @param userId
     * @param num
     * @return
     */
    @RequestMapping(value = "/recommend.do", method = RequestMethod.GET)
    public List<Integer> getItemByUserId(@RequestParam("userId") int userId,
                                      @RequestParam("topicId") int topicId,
                                      @RequestParam(defaultValue = "10") int num,
                                      @RequestParam(defaultValue = "0") int page) {
        int multiple = 10;
        String result = alsFilterService.predictMovie(userId, num * multiple);
        List<Integer> feedList = hotRecommendService.getPostListByModelAndTopic(result,topicId);
        return selectByPage(feedList, num, page);
    }


    /**
     * 新用户feed流，热度推荐（根据访问记录,基于topic）
     *
     * @return
     */
    @RequestMapping(value = "/recommend/topic.do", method = RequestMethod.GET)
    public List<Integer> getHotMediaFeedByTagId(
            @RequestParam("topicId") Integer topicId,
            @RequestParam(defaultValue = "10") int num,
            @RequestParam(defaultValue = "0") int page) {
        long start = System.currentTimeMillis();
//        //todo check num
//        String modelResult = alsFilterService.predictMovie(userId,100);
//        List<Integer> feedList = hotRecommendService.getPostListByModelAndTopic(modelResult,topicId);
        List<Integer> postList = hotRecommendService.getHotPostByTopic(topicId);
        long end = System.currentTimeMillis();
        logger.info(String.format("Get HotMediaFeed By tag in %dms", end - start));
        return selectByPage(postList, num, page);
    }

    /**
     * 相似文章
     * @param postId
     * @return
     */
    @RequestMapping(value = "/simMedia.do", method = RequestMethod.GET)
    public List<Long> matchSimilaryMedia(@RequestParam("postId") int postId){
        return matchService.matchSimilarMediaById(postId).subList(0,5);
    }



    private List<Integer> selectByPage(List<Integer> feedList, int num, int page) {
        if (feedList.size() >= (page + 1) * num)
            return feedList.subList(page * num, (page + 1) * num);
        else if (feedList.size() > page * num && feedList.size() < (page + 1) * num)
            return feedList.subList(page * num, feedList.size());
        else
            return Collections.emptyList();
    }


}
