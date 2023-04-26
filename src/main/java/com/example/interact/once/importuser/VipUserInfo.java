package com.example.interact.once.importuser;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 会员用户信息
 */
@Data
public class VipUserInfo {

    /**
     * id
     */
    @ExcelProperty("会员编号")
    private String vipCode;

    /**
     * 用户昵称
     */
    @ExcelProperty("用户昵称")
    private String username;

}