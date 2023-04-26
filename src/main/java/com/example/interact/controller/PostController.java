package com.example.interact.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.interact.common.BaseResponse;
import com.example.interact.common.ResultUtils;
import com.example.interact.model.domain.Post;
import com.example.interact.model.domain.User;
import com.example.interact.model.dto.ScrollResult;
import com.example.interact.service.IPostService;
import com.example.interact.service.UserService;
import com.example.interact.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 前端控制器
 *
 * @author W.G.
 */
@RestController
@RequestMapping("/post")
public class PostController {

    @Resource
    private IPostService PostService;

    @Resource
    private UserService userService;

    @PostMapping
    public BaseResponse<Long> savePost(@RequestBody Post post, HttpServletRequest request) {
        long postId = PostService.savePost(post, request);
        return ResultUtils.success(postId);
    }

    @PutMapping("/like/{id}")
    public BaseResponse<Integer> likePost(@PathVariable("id") Long id, HttpServletRequest request) {
        int result = PostService.likePost(id, request);
        return ResultUtils.success(result);
    }

    @GetMapping("/of/me")
    public BaseResponse<List<Post>> queryMyPost(@RequestParam(value = "current", defaultValue = "1") Integer current, HttpServletRequest request) {
        // 获取登录用户
        User currentUser = userService.getLoginUser(request);
        // 根据用户查询
        Page<Post> page = PostService.query()
                .eq("user_id", currentUser.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Post> records = page.getRecords();
        return ResultUtils.success(records);
    }

    @GetMapping("/hot")
    public BaseResponse<List<Post>> queryHotPost(@RequestParam(value = "current", defaultValue = "1") Integer current, HttpServletRequest request) {
        List<Post> records = PostService.queryHotPost(current, request);
        return ResultUtils.success(records);
    }

    @GetMapping("/{id}")
    public BaseResponse<Post> queryPostById(@PathVariable("id") Long id, HttpServletRequest request) {
        Post post = PostService.queryPostById(id, request);
        return ResultUtils.success(post);
    }

    @GetMapping("/likes/{id}")
    public BaseResponse<List<User>> queryPostLikes(@PathVariable("id") Long id) {

        List<User> listUsers = PostService.queryPostLikes(id);
        return ResultUtils.success(listUsers);
    }

    @GetMapping("/of/user")
    public BaseResponse<List<Post>> queryPostByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // 根据用户查询
        Page<Post> page = PostService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Post> records = page.getRecords();
        return ResultUtils.success(records);
    }

    @GetMapping("/of/follow")
    public BaseResponse<ScrollResult> queryPostOfFollow(
            @RequestParam("lastId") Long max, @RequestParam(value = "offset", defaultValue = "0") Integer offset, HttpServletRequest request){
        ScrollResult record = PostService.queryPostOfFollow(max, offset, request);
        return ResultUtils.success(record);
    }
}
