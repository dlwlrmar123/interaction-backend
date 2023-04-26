package com.example.interact.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.interact.model.domain.Post;
import com.example.interact.model.domain.User;
import com.example.interact.model.dto.ScrollResult;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 *  服务类
 *
 * @author W.G.
 */
public interface IPostService extends IService<Post> {

    List<Post> queryHotPost(Integer current, HttpServletRequest request);

    Post queryPostById(Long id, HttpServletRequest request);

    int likePost(Long id, HttpServletRequest request);

    List<User> queryPostLikes(Long id);

    long savePost(Post post, HttpServletRequest request);

    ScrollResult queryPostOfFollow(Long max, Integer offset, HttpServletRequest request);

}
