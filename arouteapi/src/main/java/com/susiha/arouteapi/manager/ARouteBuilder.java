package com.susiha.arouteapi.manager;

import android.content.Context;
import android.os.Bundle;

public class ARouteBuilder {

    private Context mContext;
    private int mCode =-1;


    public Bundle getBundle() {
        return mBundle;
    }

    private Bundle mBundle =null;


    /**
     * 检查Bundle 是否为null 如果为null 创建一个Bundle
     */
    private void checkBundle(){
        if(mBundle ==null){
            mBundle = new Bundle();
        }
    }

    public ARouteBuilder withContext(Context context){
        this.mContext = context;
        return this;
    }

    public ARouteBuilder withCode(int code){
        this.mCode = code;
        return this;
    }


    public ARouteBuilder with(String name,String value){
        checkBundle();
        mBundle.putString(name,value);
        return this;
    }

    public ARouteBuilder with(String name,double value){
        checkBundle();
        mBundle.putDouble(name,value);
        return this;
    }
    public ARouteBuilder with(String name,float value){
        checkBundle();
        mBundle.putFloat(name,value);
        return this;
    }
    public ARouteBuilder with(String name,int value){
        checkBundle();
        mBundle.putInt(name,value);
        return this;
    }
    public ARouteBuilder with(String name,long value){
        checkBundle();
        mBundle.putLong(name,value);
        return this;
    }

    public ARouteBuilder with(String name,boolean value){
        checkBundle();
        mBundle.putBoolean(name,value);
        return this;
    }


    public Object novigation(){
        if(mContext ==null){
            throw new IllegalArgumentException("Context 不能为空,请使用withContext进行赋值");
        }

       return  ARoute.getInstance().navigation(mContext,this,mCode);
    }

}
