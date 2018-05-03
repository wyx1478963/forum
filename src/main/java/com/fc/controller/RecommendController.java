package com.fc.controller;

import com.fc.service.ALSFilterService;
import com.fc.service.HotRecommendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/feed")
public class RecommendController {

    @Autowired
    ALSFilterService alsFilterService;

    @Autowired
    HotRecommendService hotRecommendService;

    private static final Logger logger = LoggerFactory.getLogger(RecommendController.class);
    /**
     * 通用推荐接口，返回推荐流，区分新老用户
     *
     * @param userId:用户id
     * @param num:返回推荐流中内容数量
     * @return
     */
    @RequestMapping(value = "/recommend", method = RequestMethod.GET)
    public List<Long> getRecommendById(@RequestParam Long userId,
                                       @RequestParam(defaultValue = "10") int num,
                                       @RequestParam(defaultValue = "-1") Long tagId,
                                       @RequestParam(defaultValue = "-1") Long channelId,
                                       @RequestParam(defaultValue = "0") int page) {
        List<Long> feedList = new ArrayList<>();
        if (isOldUser(userId))
            feedList = getItemByUserId(userId, num, tagId, channelId, page);
        else if (tagId != -1)
            feedList = getHotMediaFeedByTagId(tagId, num, page);
        else if (channelId != -1)
            feedList = getHotMediaFeedByChannelId(channelId, num, page);
        return selectByPage(feedList, num, page);
    }

    /**
     * 老用户feed流
     *
     * @param userId
     * @param num
     * @return
     */
    @RequestMapping(value = "/cfRecommend/{userId}", method = RequestMethod.GET)
    public List<Long> getItemByUserId(@PathVariable Long userId,
                                      @RequestParam(defaultValue = "10") int num,
                                      @RequestParam(defaultValue = "-1") Long tagId,
                                      @RequestParam(defaultValue = "-1") Long channelId,
                                      @RequestParam(defaultValue = "0") int page) {
        Integer idToInteger = Integer.valueOf(userId.toString());
        int multiple = 10;
        String result = alsFilterService.predictMovie(idToInteger, num * multiple);
        List<Long> feedList = new ArrayList<>();
        if (tagId != -1)
            feedList = hotRecommendService.filterWithTagOrChannel(result, tagId, -1L);
        if (channelId != -1)
            feedList = hotRecommendService.filterWithTagOrChannel(result, -1L, channelId);
        return selectByPage(feedList, num, page);
    }

    /**
     * 新用户feed流，热度推荐,基于channel
     *
     * @return
     */
    @RequestMapping(value = "/hotMedia/channel", method = RequestMethod.GET)
    public List<Long> getHotMediaFeedByChannelId(@RequestParam(defaultValue = "-1") Long channelId,
                                                 @RequestParam(defaultValue = "10") int num,
                                                 @RequestParam(defaultValue = "0") int page) {
        num = num > 100 ? 100 : num;
        long start = System.currentTimeMillis();
        List<Long> feedList = new ArrayList<>();
        if (channelId != -1)
            feedList = hotRecommendService.getHotMediaFeedByChannelId(channelId);
        else
            feedList = hotRecommendService.getAllHotMediaFeed();
        long end = System.currentTimeMillis();
        logger.info(String.format("Get HotMediaFeed By channel %dms", end - start));
        return selectByPage(feedList, num, page);
    }

    /**
     * 新用户feed流，热度推荐,基于tagId
     *
     * @return
     */
    @RequestMapping(value = "/hotMedia/tag", method = RequestMethod.GET)
    public List<Long> getHotMediaFeedByTagId(@RequestParam(defaultValue = "-1") Long tagId,
                                             @RequestParam(defaultValue = "10") int num,
                                             @RequestParam(defaultValue = "0") int page) {
        long start = System.currentTimeMillis();
        List<Long> feedList = new ArrayList<>();
        if (tagId != -1)
            feedList = hotRecommendService.getHotMediaFeedByTagId(tagId);
        else
            feedList = hotRecommendService.getAllHotMediaFeed();
        long end = System.currentTimeMillis();
        logger.info(String.format("Get HotMediaFeed By tag in %dms", end - start));
        return selectByPage(feedList, num, page);
    }

//    @RequestMapping(value = "/simMedia", method = RequestMethod.GET)
//    public List<Long> matchSimilaryMedia(@RequestParam Long id){
//        return matchService.matchSimilarMediaById(id).subList(0,5);
//    }
//


    /**
     * 判断是否是老用户，通过浏览和购买数评判
     *
     * @param id
     * @return
     */
    public Boolean isOldUser(Long id) {
        return hotRecommendService.isOldUser(id);
    }

    private List<Long> selectByPage(List<Long> feedList, int num, int page) {
        if (feedList.size() >= (page + 1) * num)
            return feedList.subList(page * num, (page + 1) * num);
        else if (feedList.size() > page * num && feedList.size() < (page + 1) * num)
            return feedList.subList(page * num, feedList.size());
        else
            return Collections.emptyList();
    }


}
