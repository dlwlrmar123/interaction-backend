package com.example.interact.controller;


import com.example.interact.common.BaseResponse;
import com.example.interact.common.ResultUtils;
import com.example.interact.model.domain.User;
import com.example.interact.service.IFollowService;
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
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private  IFollowService followService;

    @PutMapping("/{id}/{isFollow}")
    public BaseResponse<Integer> follow(@PathVariable("id") Long followUserId, @PathVariable("isFollow") Boolean isFollow, HttpServletRequest request) {
         int result = followService.follow(followUserId, isFollow, request);
        return ResultUtils.success(result);
    }

    @GetMapping("/or/not/{id}")
    public BaseResponse<Boolean> isFollow(@PathVariable("id") Long followUserId, HttpServletRequest request) {
        boolean result = followService.isFollow(followUserId, request);
        return ResultUtils.success(result);

    }

    @GetMapping("/common/{id}")
    public List<User> followCommons(@PathVariable("id") Long id, HttpServletRequest request){

        List<User> records = followService.followCommons(id, request);
        return records;
    }
}
