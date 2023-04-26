package com.example.interact.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户加入队伍请求体
 *
 * @author W.G.
 */
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID = -543286974259314538L;

    /**
     * id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;
}
