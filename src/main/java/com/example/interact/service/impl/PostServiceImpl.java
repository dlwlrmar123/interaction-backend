package com.example.interact.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.interact.common.ErrorCode;
import com.example.interact.exception.BusinessException;
import com.example.interact.mapper.PostMapper;
import com.example.interact.model.domain.Follow;
import com.example.interact.model.domain.Post;
import com.example.interact.model.domain.User;
import com.example.interact.model.dto.ScrollResult;
import com.example.interact.model.vo.UserVO;
import com.example.interact.service.IFollowService;
import com.example.interact.service.IPostService;
import com.example.interact.service.UserService;
import com.example.interact.utils.SystemConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.interact.utils.RedisConstants.FEED_KEY;
import static com.example.interact.utils.RedisConstants.Post_LIKED_KEY;


/**
 * 服务实现类
 *
 * @author W.G.
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IFollowService followService;

    @Override
    public List<Post> queryHotPost(Integer current, HttpServletRequest request) {
        // 根据用户查询
        Page<Post> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Post> records = page.getRecords();
        // 查询用户
        records.forEach(Post -> {
            this.queryPostUser(Post);
            this.isPostLiked(Post, request);
        });
        return records;
    }

    @Override
    public Post queryPostById(Long id, HttpServletRequest request) {
        // 1.查询Post
        Post post = getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 2.查询Post有关的用户
        queryPostUser(post);
        // 3.查询Post是否被点赞
        isPostLiked(post, request);

        return post;
    }

    private void isPostLiked(Post post, HttpServletRequest request) {
        // 1.获取登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            // 用户未登录，无需查询是否点赞
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = loginUser.getId();
        // 2.判断当前登录用户是否已经点赞
        String key = "Post:liked:" + post.getId();
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        post.setIsLike(score != null);
    }

    @Override
    public int likePost(Long id, HttpServletRequest request) {
        // 1.获取登录用户
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        // 2.判断当前登录用户是否已经点赞
        String key = Post_LIKED_KEY + id;
        Double score = stringRedisTemplate.opsForZSet().score(key, userId.toString());
        if (score == null) {
            // 3.如果未点赞，可以点赞
            // 3.1.数据库点赞数 + 1
            boolean isSuccess = update().setSql("liked = liked + 1").eq("id", id).update();
            // 3.2.保存用户到Redis的set集合  zadd key value score
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().add(key, userId.toString(), System.currentTimeMillis());
            }
        } else {
            // 4.如果已点赞，取消点赞
            // 4.1.数据库点赞数 -1
            boolean isSuccess = update().setSql("liked = liked - 1").eq("id", id).update();
            // 4.2.把用户从Redis的set集合移除
            if (isSuccess) {
                stringRedisTemplate.opsForZSet().remove(key, userId.toString());
            }
        }
        return 1;
    }

    /**
     * 点赞用户排行榜Top5
     * @param id
     * @return
     */
    @Override
    public List<User> queryPostLikes(Long id) {
        String key = Post_LIKED_KEY + id;
        // 1.查询top5的点赞用户 zrange key 0 4
        Set<String> top5 = stringRedisTemplate.opsForZSet().range(key, 0, 4);
        if (top5 == null || top5.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 2.解析出其中的用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        String idStr = StrUtil.join(",", ids);
        // 3.根据用户id查询用户 WHERE id IN ( 5 , 1 ) ORDER BY FIELD(id, 5, 1)
        List<User> userList = userService.query()
                .in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list()
                .stream()
                .map(user -> BeanUtil.copyProperties(user, User.class))
                .collect(Collectors.toList());
        // 4.返回
        for (int i = 0; i < userList.size(); i++){
            User safetyUser = userService.getSafetyUser(userList.get(i));
            userList.set(i, safetyUser);
        }
        return userList;
    }

    /**
     * 发帖、推送
     * @param post
     * @param request
     * @return
     */
    @Override
    public long savePost(Post post, HttpServletRequest request) {
        // 1.获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if(loginUser == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        post.setUserId(loginUser.getId()) ;
        // 2.保存帖子
        boolean isSuccess = save(post);
        if(!isSuccess){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        // 3.查询笔记作者的所有粉丝 select * from tb_follow where follow_user_id = ?
        List<Follow> follows = followService.query().eq("follow_user_id", loginUser.getId()).list();
        if(follows != null){
            // 4.推送笔记id给所有粉丝
            for (Follow follow : follows) {
                // 4.1.获取粉丝id
                Long userId = follow.getUserId();
                // 4.2.推送
                String key = FEED_KEY + userId;
                stringRedisTemplate.opsForZSet().add(key, post.getId().toString(), System.currentTimeMillis());
            }
        }
        // 5.返回id
        return post.getId();
    }

    @Override
    public ScrollResult queryPostOfFollow(Long max, Integer offset, HttpServletRequest request) {
        // 1.获取当前用户
        Long userId = userService.getLoginUser(request).getId();
        // 2.查询收件箱 ZREVRANGEBYSCORE key Max Min LIMIT offset count
        String key = FEED_KEY + userId;
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, offset, 2);
        // 3.非空判断
        if (typedTuples == null || typedTuples.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 4.解析数据：PostId、minTime（时间戳）、offset
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0; // 2
        int os = 1; // 2
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) { // 5 4 4 2 2
            // 4.1.获取id
            ids.add(Long.valueOf(tuple.getValue()));
            // 4.2.获取分数(时间戳）
            long time = tuple.getScore().longValue();
            if(time == minTime){
                os++;
            }else{
                minTime = time;
                os = 1;
            }
        }

        // 5.根据id查询Post
        String idStr = StrUtil.join(",", ids);
        List<Post> Posts = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();

        for (Post Post : Posts) {
            // 5.1.查询Post有关的用户
            queryPostUser(Post);
            // 5.2.查询Post是否被点赞
            isPostLiked(Post, request);
        }

        // 6.封装并返回
        ScrollResult r = new ScrollResult();
        r.setList(Posts);
        r.setOffset(os);
        r.setMinTime(minTime);

        return r;
    }

    /**
     * 关联帖子用户信息
     * @param post
     */
    private void queryPostUser(Post post) {
        Long userId = post.getUserId();
        User user = userService.getById(userId);
        post.setUsername(user.getUsername());
        post.setAvatarUrl(user.getAvatarUrl());
    }
}
