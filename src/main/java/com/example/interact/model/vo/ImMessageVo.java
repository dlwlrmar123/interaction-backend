package com.example.interact.model.vo;

import com.example.interact.model.domain.Im;
import lombok.Data;

/**
 * @author W.G.
 */
@Data
public class ImMessageVo {

    /**
     * 消息类型
     */
    private Integer type;

    /**
     * 消息
     */
    private Im im;

}
