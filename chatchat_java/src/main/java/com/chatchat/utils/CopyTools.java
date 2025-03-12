package com.chatchat.utils;

import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

public class CopyTools {
    public static  <T,S> List<T> copyList(List<S> sourceList, Class<T> class_) {
        List<T> list=new ArrayList<>();
        for (S s : sourceList) {
            T t=null;
            try {
                t=class_.newInstance();
            }catch (Exception e) {
                e.printStackTrace();
            }
            BeanUtils.copyProperties(s,t);
            list.add(t);
        }
        return list;
    }

    public static <T,S> T copy(S source, Class<T> class_) {
        T t=null;
        try {
            t=class_.newInstance();
        }catch (Exception e) {
            e.printStackTrace();
        }
        BeanUtils.copyProperties(source,t);
        return t;
    }
}
