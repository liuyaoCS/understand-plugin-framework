package com.weishu.binder_hook.app;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.os.IBinder;
import android.util.Log;


public class BinderProxyHookHandler implements InvocationHandler {

    private static final String TAG = "BinderProxyHookHandler";

    // 绝大部分情况下,这是一个BinderProxy对象
    // 只有当Service和我们在同一个进程的时候才是Binder本地对象
    // 这个基本不可能
    IBinder base;

    Class<?> stub;

    Class<?> iinterface;

    public BinderProxyHookHandler(IBinder base) {
        this.base = base;
        try {
            this.stub = Class.forName("android.content.IClipboard$Stub");
            this.iinterface = Class.forName("android.content.IClipboard");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("queryLocalInterface".equals(method.getName())) {

            Log.d(TAG, "hook queryLocalInterface");

            // 这里直接返回真正被Hook掉的Service接口，其实这里面Hook的是一个BinderProxy对象(Binder代理) (代理Binder的queryLocalInterface正常情况下是返回null)
            //注意，传入的是原始的Binder对象，所以不会出现无限循环
            return Proxy.newProxyInstance(proxy.getClass().getClassLoader(),
                    new Class[] { this.iinterface },
                    new BinderHookHandler(base, stub));
        }

        Log.d(TAG, "method:" + method.getName());
        return method.invoke(base, args);
    }
}
