package com.susiha.arouteapi.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.LruCache;

import com.susiha.annotation.module.ArouteBean;
import com.susiha.arouteapi.ArouteLoadGroup;
import com.susiha.arouteapi.ArouteLoadPath;

public class ARoute {

    private final String Group_prefix = "com.susiha.modular.apt.ARouter$$Group$$";


    LruCache<String, ArouteLoadGroup> groupCache;
    LruCache<String, ArouteLoadPath> pathCache;

    private static volatile ARoute INSTANCE = null;

    private ARoute() {
        groupCache = new LruCache<>(20);
        pathCache = new LruCache<>(20);
    }

    private String mPath;//路径

    public static ARoute getInstance() {
        if (INSTANCE == null) {
            synchronized (ARoute.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ARoute();
                }
            }
        }
        return INSTANCE;
    }

    public ARouteBuilder build(String path) {
        this.mPath = path;
        return new ARouteBuilder();
    }


    /**
     * 校验Path
     *
     * @return
     */
    private void checkPath() {

        if (!mPath.startsWith("/") || mPath.lastIndexOf("/") == 0) {
            throw new IllegalArgumentException("Path的路径格式错误，必须是/.../... 格式");
        }

    }


    Object navigation(Context context, ARouteBuilder builder, int code) {

        checkPath();
        //获取group
        String group = mPath.substring(1, mPath.indexOf("/", 1));
        String classFullName = Group_prefix + group;
        ArouteLoadGroup loadGroup = groupCache.get(classFullName);

        //缓存中没有
        if (loadGroup == null) {

            try {
                loadGroup = (ArouteLoadGroup) Class.forName(classFullName).newInstance();
                groupCache.put(classFullName, loadGroup);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
         if(loadGroup ==null){
             throw  new IllegalStateException("加载Group路由失败");
         }




        ArouteLoadPath loadPath = pathCache.get(group);
        if(loadPath ==null){
            try {
                loadPath =  loadGroup.loadGroup().get(group).newInstance();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }

        if(loadPath ==null){
            throw  new IllegalStateException("加载Path路由失败");
        }


        ArouteBean bean = loadPath.loadPath().get(mPath);

        Intent intent = new Intent(context,bean.getClazz());
        if(builder.getBundle()!=null){
            intent.putExtras(builder.getBundle());
        }
        if(code>0){
            ((Activity)context).startActivityForResult(intent,code);
        }else{
            context.startActivity(intent);
        }

        return null;
    }
}
