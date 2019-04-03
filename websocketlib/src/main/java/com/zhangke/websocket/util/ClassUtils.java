package com.zhangke.websocket.util;

import com.zhangke.websocket.dispatcher.IResponseDispatcher;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 类操作相关工具类
 *
 * Created by ZhangKe on 2019/4/3.
 */
public class ClassUtils {

    public static Class getClassFromT(IResponseDispatcher dispatcher){
        Type[] typeArray = dispatcher.getClass().getGenericInterfaces();
        if(typeArray.length > 0){
            Type itemType = typeArray[0];
            if(itemType instanceof )
            Type[] t = ((ParameterizedType)array[0]).getActualTypeArguments();
        }
    }
}
