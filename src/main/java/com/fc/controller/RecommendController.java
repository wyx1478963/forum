package com.fc.controller;

import com.fc.mapper.PostMapper;
import com.fc.model.PageBean;
import com.fc.model.Post;
import com.fc.model.User;
import com.fc.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/")
public class RecommendController {

    @Autowired
    ALSFilterService alsFilterService;

    @Autowired
    HotRecommendService hotRecommendService;

    @Autowired
    PostService postService;

    @Autowired
    MatchService matchService;

    @Autowired
    UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(RecommendController.class);

    /**
     * 推荐流（基于topic）
     *
     * @param userId
     * @param num
     * @return
     */
    @RequestMapping(value = "/recommend.do", method = RequestMethod.GET)
    public List<Integer> getItemByUserIdAndTopic(@RequestParam("userId") int userId,
                                         @RequestParam("topicId") int topicId,
                                         @RequestParam(defaultValue = "10") int num,
                                         @RequestParam(defaultValue = "0") int page) {
        int multiple = 10;
        String result = alsFilterService.predictMovie(userId, num * multiple);
        List<Integer> feedList = hotRecommendService.getPostListByModelAndTopic(result, topicId);
        return selectByPage(feedList, num, page);
    }

    /**
     * 推荐流
     *
     * @param userId
     * @param num
     * @return
     */
    @RequestMapping(value = "/recommendTopic.do", method = RequestMethod.GET)
    public List<Integer> getItemByUserId(@RequestParam("userId") int userId,
                                         @RequestParam("topicId") int topicId,
                                         @RequestParam(defaultValue = "10") int num,
                                         @RequestParam(defaultValue = "0") int page) {
        int multiple = 10;
        String result = alsFilterService.predictMovie(userId, num * multiple);
        List<Integer> feedList = hotRecommendService.getPostListByModel(result);
        return selectByPage(feedList, num, page);
    }





    /**
     * 新用户feed流，热度推荐（根据post,基于topic）
     *
     * @return
     */
    @RequestMapping("/toHotTopic.do")
    public String toHotTopicIndex(@RequestParam("tid") int tid, Model model, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr());
        //记录访问信息
        userService.record(request.getRequestURL(), request.getContextPath(), request.getRemoteAddr());
        //列出帖子
        PageBean<Post> pageBean = postService.listPostByTimeAndTopic(1, tid, true);
        //列出用户
        List<User> userList = userService.listUserByTime();
        //列出活跃用户
        List<User> hotUserList = userService.listUserByHot();
        //向模型中添加数据
        model.addAttribute("pageBean", pageBean);
        model.addAttribute("userList", userList);
        model.addAttribute("hotUserList", hotUserList);
        model.addAttribute("tid",tid);
        return "topicIndex";
    }

    /**
     * 相似文章（根据post,基于topic）
     *
     * @return
     */
    @RequestMapping("/toSimilarTopic.do")
    public String toSimilarTopicIndex(@RequestParam("tid") int tid, Model model, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr());
        //记录访问信息
        userService.record(request.getRequestURL(), request.getContextPath(), request.getRemoteAddr());
        //列出帖子
        PageBean<Post> pageBean = postService.listPostByTimeAndTopic(1, tid, true);
        //列出用户
        List<User> userList = userService.listUserByTime();
        //列出活跃用户
        List<User> hotUserList = userService.listUserByHot();
        //向模型中添加数据
        model.addAttribute("pageBean", pageBean);
        model.addAttribute("userList", userList);
        model.addAttribute("hotUserList", hotUserList);
        model.addAttribute("tid",tid);
        return "topicIndex";
    }

    /**
     * 相似文章（根据post）
     *
     * @return
     */
    @RequestMapping("/toSimilar.do")
    public String toSimilarIndex(Model model, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr());
        //记录访问信息
        userService.record(request.getRequestURL(), request.getContextPath(), request.getRemoteAddr());
        //列出帖子
        PageBean<Post> pageBean = postService.listPostByTime(1, true);
        //列出用户
        List<User> userList = userService.listUserByTime();
        //列出活跃用户
        List<User> hotUserList = userService.listUserByHot();
        //向模型中添加数据
        model.addAttribute("pageBean", pageBean);
        model.addAttribute("userList", userList);
        model.addAttribute("hotUserList", hotUserList);
        return "index";
    }



    /**
     * 热度推荐（根据post）
     *
     * @return
     */
    @RequestMapping("/toHot.do")
    public String toHotIndex(Model model, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr());
        //记录访问信息
        userService.record(request.getRequestURL(), request.getContextPath(), request.getRemoteAddr());
        //列出帖子
        PageBean<Post> pageBean = postService.listPostByTime(1, true);
        //列出用户
        List<User> userList = userService.listUserByTime();
        //列出活跃用户
        List<User> hotUserList = userService.listUserByHot();
        //向模型中添加数据
        model.addAttribute("pageBean", pageBean);
        model.addAttribute("userList", userList);
        model.addAttribute("hotUserList", hotUserList);
        return "index";
    }

    /**
     * 相似文章
     *
     * @param postId
     * @return
     */
    @RequestMapping(value = "/simMedia.do", method = RequestMethod.GET)
    public List<Long> matchSimilaryMedia(@RequestParam("postId") int postId) {
        return matchService.matchSimilarMediaById(postId).subList(0, 5);
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
