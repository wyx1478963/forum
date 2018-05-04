package com.fc.controller;

import com.fc.model.PageBean;
import com.fc.model.Post;
import com.fc.model.Topic;
import com.fc.model.User;
import com.fc.service.PostService;
import com.fc.service.QiniuService;
import com.fc.service.TopicService;
import com.fc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/")
public class TopicController {


    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Autowired
    private TopicService topicService;

    /**
     * 列出所有话题
     *
     * @param model
     * @return
     */
    @RequestMapping("/listTopic.do")
    public String listTopic(Model model) {
        List<Topic> topicList = topicService.listTopic();
        model.addAttribute("topicList", topicList);
        return "topic";
    }

    @RequestMapping("/listImage.do")
    public String listImage(Model model) {
        List<String> imageList = topicService.listImage();
        model.addAttribute("imageList", imageList);
        return "image";
    }

    @RequestMapping("/toTopicIndex.do")
    public String listTopicPost(@RequestParam("tid") int tid, Model model, HttpServletRequest request) {
        System.out.println(request.getRemoteAddr());
        //记录访问信息
        userService.record(request.getRequestURL(), request.getContextPath(), request.getRemoteAddr());
        //列出帖子
        PageBean<Post> pageBean = postService.listPostByTimeAndTopic(1, tid);
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
}





