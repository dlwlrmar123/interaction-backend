package com.example.interact.service.impl;

import com.example.interact.mapper.UserInfoMapper;
import com.example.interact.model.domain.UserInfo;
import com.example.interact.service.IUserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 *  服务实现类
 *  
 * @author W.G.
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
