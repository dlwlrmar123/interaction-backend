package com.example.interact.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.interact.mapper.FollowMapper;
import com.example.interact.model.domain.Follow;
import com.example.interact.model.domain.User;
import com.example.interact.service.IFollowService;
import com.example.interact.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 服务实现类
 *
 * @author W.G.
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements IFollowService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserService userService;

    /**
     * 关注与取关
     * @param followUserId
     * @param isFollow
     * @param request
     * @return
     */
    @Override
    public int follow(Long followUserId, Boolean isFollow, HttpServletRequest request) {
        // 1.获取登录用户
        Long userId = userService.getLoginUser(request).getId();
        String key = "follows:" + userId;
        // 1.判断到底是关注还是取关
        if (isFollow) {
            // 2.关注，新增数据到redis
            Follow follow = new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean isSuccess = save(follow);
            if (isSuccess) {
                // 把关注用户的id，放入redis的set集合 sadd userId followerUserId
                stringRedisTemplate.opsForSet().add(key, followUserId.toString());
            }
        } else {
            // 3.取关，删除 delete from tb_follow where user_id = ? and follow_user_id = ?
            boolean isSuccess = remove(new QueryWrapper<Follow>()
                    .eq("user_id", userId).eq("follow_user_id", followUserId));
            if (isSuccess) {
                // 把关注用户的id从Redis集合中移除
                stringRedisTemplate.opsForSet().remove(key, followUserId.toString());
            }
        }
        return 1;
    }

    /**
     * 判断是否关注
     * @param followUserId
     * @param request
     * @return
     */
    @Override
    public boolean isFollow(Long followUserId, HttpServletRequest request) {
        // 1.获取登录用户
        Long userId = userService.getLoginUser(request).getId();
        // 2.查询是否关注 select count(*) from tb_follow where user_id = ? and follow_user_id = ?
        Long count = query().eq("user_id", userId).eq("follow_user_id", followUserId).count();
        // 3.判断
        return count > 0;
    }

    @Override
    public List<User> followCommons(Long id, HttpServletRequest request) {
        // 1.获取当前用户
        Long userId = userService.getLoginUser(request).getId();
        String key = "follows:" + userId;
        // 2.求交集
        String key2 = "follows:" + id;
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key, key2);
        if (intersect == null || intersect.isEmpty()) {
            // 无交集
            return Collections.emptyList();
        }
        // 3.解析id集合
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        // 4.查询用户
        List<User> users = userService.listByIds(ids)
                .stream()
                .map(user -> BeanUtil.copyProperties(user, User.class))
                .collect(Collectors.toList());
        return users;
    }
}
