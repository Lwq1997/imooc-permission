package com.lwq.param;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @Author: Lwq
 * @Date: 2019/3/16 11:18
 * @Version 1.0
 * @Describe
 */
@Getter
@Setter
@ToString
public class SearchLogParam {

    private Integer type; // LogType

    private String beforeSeg;

    private String afterSeg;

    private String operator;

    private String fromTime;//yyyy-MM-dd HH:mm:ss

    private String toTime;
}

