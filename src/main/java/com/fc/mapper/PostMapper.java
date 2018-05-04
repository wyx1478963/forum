package com.fc.mapper;

import com.fc.model.ContentInfo;
import com.fc.model.Post;
import com.fc.model.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


public interface PostMapper {


    List<Post> listPostByUid(int uid);

    int insertPost(Post post);

    List<Post> listPostByTime(@Param("offset") int offset, @Param("limit") int limit);

    List<Post> listPostByTimeAndTopic(@Param("offset") int offset, @Param("limit") int limit,@Param("tid")int tid);

    int selectPostCount();

    int selectTopicPostCount(@Param("tid")int tid);

    Post getPostByPid(int pid);

    void updateReplyCount(int pid);

    void updateScanCount(int pid);

    void updateReplyTime(int pid);

    int getUidByPid(int pid);

    String getTitleByPid(int pid);

    @Select("select distinct(pid) from post where tid = #{topicId}")
    List<Integer> listPostOfTopic(@Param("topicId") int topicId);

    ContentInfo getContentInfoByPostId(@Param("postId") int postId);

    List<ContentInfo> getAllContentInfo();

    List<Post> getHotPost();

}
