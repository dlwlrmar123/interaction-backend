package com.example.interact.service;

import com.example.interact.model.domain.Follow;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.interact.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *  服务类
 *
 * @author W.G.
 */
public interface IFollowService extends IService<Follow> {

    int follow(Long followUserId, Boolean isFollow, HttpServletRequest request);

    boolean isFollow(Long followUserId, HttpServletRequest request);

    List<User> followCommons(Long id, HttpServletRequest request);
}
