package com.fc.mapper;

import com.fc.model.UserViewHistory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserViewHistoryMapper {

    List<UserViewHistory> getAllUserViewHistory();

    List<UserViewHistory> getUserViewHistoryByUid(@Param("uid") Integer uid);

    @Insert("insert into user_view_history(pid,uid) values(#{pid},#{uid})")
    int insertUserViewHistory(UserViewHistory userViewHistory);
}
