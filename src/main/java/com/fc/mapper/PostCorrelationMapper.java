package com.fc.mapper;

import com.fc.model.Post;
import com.fc.model.PostCorrelation;
import org.apache.ibatis.annotations.Insert;

import java.util.List;

public interface PostCorrelationMapper {
    List<Post> selectSimilarPostById(Integer postId);

    @Insert("insert into post_correlation(source_cid,target_cid,correlation) values #{source_cid},#{target_cid},#{correlation}")
    int insertPostCorrelation(PostCorrelation postCorrelation);

}