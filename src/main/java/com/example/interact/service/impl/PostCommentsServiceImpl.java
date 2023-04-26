package com.example.interact.service.impl;

import com.example.interact.mapper.PostCommentsMapper;
import com.example.interact.model.domain.PostComments;
import com.example.interact.service.IPostCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 *
 * @author W.G.
 */
@Service
public class PostCommentsServiceImpl extends ServiceImpl<PostCommentsMapper, PostComments> implements IPostCommentsService {

}
