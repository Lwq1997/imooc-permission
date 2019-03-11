package com.lwq.common;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: Lwq
 * @Date: 2019/3/11 17:51
 * @Version 1.0
 * @Describe
 */
@Getter
@Setter
public class JsonData {

    private boolean ret;

    private String msg;

    private Object data;

    public JsonData(boolean ret) {
        this.ret = ret;
    }

    public static JsonData success(Object object,String msg){
        JsonData jsonData = new JsonData(true);
        jsonData.data=object;
        jsonData.msg=msg;
        return jsonData;
    }

    public static JsonData success(Object object){
        JsonData jsonData = new JsonData(true);
        jsonData.data=object;
        return jsonData;
    }

    public static JsonData success(){
        return new JsonData(true);
    }

    public static JsonData fail(String msg){
        JsonData jsonData = new JsonData(false);
        jsonData.msg=msg;
        return jsonData;
    }
}
