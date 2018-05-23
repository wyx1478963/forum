package com.fc.service;

import com.fc.mapper.UserViewHistoryMapper;
import com.fc.model.UserViewHistory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class RecommendService {

    @Autowired
    UserViewHistoryMapper userViewHistoryMapper;

    public boolean isNewUser(int uid) {
        List<UserViewHistory> userViewHistoryList = userViewHistoryMapper.getUserViewHistoryByUid(uid);
        return userViewHistoryList == null || userViewHistoryList.isEmpty();
    }

}
