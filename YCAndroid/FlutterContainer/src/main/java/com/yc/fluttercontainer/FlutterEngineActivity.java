package com.yc.fluttercontainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import io.flutter.embedding.android.SplashScreen;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.renderer.FlutterUiDisplayListener;
import io.flutter.embedding.engine.renderer.RenderSurface;
import io.flutter.embedding.engine.systemchannels.NavigationChannel;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;


/**
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2018/11/9
 *     desc  : 基类
 *     revise:
 * </pre>
 */
public abstract class FlutterEngineActivity extends FlutterBaseActivity {

    private static final String TAG = "FlutterEngineActivity";
    private LinearLayout root;
    private FrameLayout layoutContainer;
    private FlutterEngine flutterEngine;
    private FrameLayout mFlutterContainer;
    private FixFlutterView mFlutterView;
    private RenderSurface mRenderSurface;
    private BinaryMessenger binaryMessenger;
    private NavigationChannel navigationChannel;
    private String mPageId;
    private Map<String, Object> params;

    private final FlutterUiDisplayListener flutterUiDisplayListener =
            new FlutterUiDisplayListener() {
        @Override
        public void onFlutterUiDisplayed() {
            //当flutter ui展示出来的时候，会回调这个方法
            FlutterEngineActivity.this.onFlutterUiDisplayed();
        }

        @Override
        public void onFlutterUiNoLongerDisplayed() {
            FlutterEngineActivity.this.onFlutterUiNoLongerDisplayed();
        }
    };

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mPageId = SystemClock.uptimeMillis() + "@" + Integer.toHexString(hashCode());
        FlutterContainerRegistry.registerContainer(mPageId, this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.flutter_container);
        initIntentArgs();
        addFlutterEngine();
        initView();
    }

    /**
     * 创建FlutterEngine，
     */
    private void addFlutterEngine() {
        flutterEngine = new FlutterEngine(this);
        binaryMessenger = flutterEngine.getDartExecutor().getBinaryMessenger();
        //获取路由跳转channel通信
        navigationChannel = flutterEngine.getNavigationChannel();
        //处理NA给flutter传递参数的问题
        StringBuffer sb = new StringBuffer();
        if (params!=null){
            String path = (String) params.get("initial_route");
            FlutterLoggerUtils.log(TAG,"path：" + path);
            sb.append(path);
            sb.append("?");
            //遍历
            params.remove("initial_route");
            if (params.size()>0){
                JSONObject json = new JSONObject(params);
                String routerParams = json.toString();
                //String routerParams = "?{\"name\":\"杨充\"}";
                sb.append(routerParams);
                FlutterLoggerUtils.log(TAG,"routerParams：" + routerParams);
            }
            //String route = "yc?{\"name\":\"杨充\"}";
            String route = sb.toString();
            FlutterLoggerUtils.log(TAG,"route：" + route);
            navigationChannel.setInitialRoute(route);
        }
        navigationChannel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(@NonNull MethodCall methodCall,
                                     @NonNull MethodChannel.Result result) {
                onMethodCallListener(methodCall,result);
            }
        });
        //navigationChannel.setInitialRoute("yc?{\"name\":\"杨充\"}");
        flutterEngine.getDartExecutor().executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
        );
    }

    /**
     * 初始化view
     */
    private void initView() {
        root = findViewById(R.id.root);
        layoutContainer = findViewById(R.id.layout_container);
        root.setBackgroundColor(Color.WHITE);
        //创建FlutterView然后添加到布局中
        //创建FlutterView然后添加到布局中
        //创建FlutterView然后添加到布局中
        View view = onInflateFlutterLayout(layoutContainer);
        layoutContainer.addView(view);
    }

    /**
     * 通过覆写此接口自定义布局
     * 在自定义布局中需要自己调用 createFlutterView 创建 FlutterView
     * @param container                             container
     * @return
     */
    public View onInflateFlutterLayout(FrameLayout container) {
        return createFlutterView(container.getContext());
    }

    /**
     * 监听flutter跳转NA路由的操作
     * @param methodCall                            methodCall
     * @param result                                result
     */
    public abstract void onMethodCallListener(MethodCall methodCall,
                                              MethodChannel.Result result);

    /**
     * 获取Surface的类型，默认是FlutterTextureView，子类可以自己实现切换
     * @return                                      类型
     */
    public @FlutterCommons.SurfaceType int getSurfaceType(){
        //这里使用注解限制类型，必须传入SurfaceType的类型
        return FlutterCommons.TEXTURE_VIEW;
    }

    /**
     * 根据路由地址打开页面
     * @param route                                 路由地址
     */
    @SuppressLint("VisibleForTests")
    public void pushRoute(String route){
        if (navigationChannel!=null){
            if (mFlutterView!=null && mFlutterView.isAttachedToFlutterEngine()){
                navigationChannel.pushRoute(route);
            }
        }
    }

    /**
     * 根据路由地址pop页面
     */
    @SuppressLint("VisibleForTests")
    public void popRoute(){
        if (navigationChannel!=null){
            if (mFlutterView!=null && mFlutterView.isAttachedToFlutterEngine()){
                navigationChannel.popRoute();
            }
        }
    }

    public View createFlutterView(Context context) {
        mFlutterContainer = new FrameLayout(context);
        // 通过FlutterView引入Flutter编写的页面
        // 这里的FlutterView位于io.flutter.embedding.android包中
        // 和此前我们所创建的FlutterView（位于io.flutter.view包中）是不一样的。
        // 通过查看FlutterView的源码可以发现它继承自FrameLayout，因此像一个普通的View那样添加就可以了。
        if (getSurfaceType()==FlutterCommons.TEXTURE_VIEW){
            //使用TextureView
            FixFlutterTextureView flutterTextureView = new FixFlutterTextureView(this);
            mRenderSurface = flutterTextureView;
            mFlutterView = new FixFlutterView(this, flutterTextureView);
        } else {
            //使用SurfaceView
            FixFlutterSurfaceView flutterSurfaceView = new FixFlutterSurfaceView(this);
            mRenderSurface = flutterSurfaceView;
            mFlutterView = new FixFlutterView(this, flutterSurfaceView);
        }
        mFlutterView.addOnFirstFrameRenderedListener(flutterUiDisplayListener);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mFlutterContainer.addView(mFlutterView, lp);
        // 关键代码，将Flutter页面显示到FlutterView中
        // 这个方法的作用就是将Flutter编写的UI页面显示到FlutterView中
        // flutterEngine的类型为FlutterEngine，字面意思就是Flutter引擎
        // 它负责在Android端执行Dart代码，将Flutter编写的UI显示到FlutterView/FlutterActivity/FlutterFragment中。
        mFlutterView.attachToFlutterEngine(flutterEngine);
        return mFlutterContainer;
    }

    @Override
    public void onFlutterUiDisplayed() {
        super.onFlutterUiDisplayed();
        root.setBackgroundColor(Color.TRANSPARENT);
    }

    @Nullable
    @Override
    public SplashScreen provideSplashScreen() {
        //创建自定义flutter启动屏view
        return new FlutterSplashView();
    }

    /**
     * 处理路由参数传递信息
     */
    private void initIntentArgs() {
        Intent intent = getIntent();
        //从intent取出bundle
        //Bundle bundle = intent.getBundleExtra("Message");
        Bundle bundle = intent.getExtras();
        if (bundle!=null){
            String path = bundle.getString(FlutterCommons.BUNDLE_KEY_PATH);
            // 向 Flutter 传的参数
            // 页面参数扁平化透传
            Map<String, Object> stringObjectMap = transformBundleParameters(bundle);
            params = new HashMap<>(stringObjectMap);
            // 兼容嵌套 params Bundle
            if (bundle.containsKey(FlutterCommons.BUNDLE_KEY_PARAMS)) {
                Bundle bundleParams = bundle.getBundle(FlutterCommons.BUNDLE_KEY_PARAMS);
                if (bundleParams != null) {
                    Map<String, Object> parameters = transformBundleParameters(bundleParams);
                    params.putAll(parameters);
                }
            }
            // 传混合路由跳转路径
            params.put("initial_route", path);
            FlutterLoggerUtils.log(TAG,"传入的路由地址：" + path);
            FlutterLoggerUtils.log(TAG, "prams: " + params.toString());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * 分别在onResume()、onPause()和onStop()方法中调用了LifecycleChannel的appIsResumed()、
     * appIsInactive()和appIsPaused()方法，作用就是同步Flutter端与原生端的生命周期。
     * 猜想可能是FlutterVIew的渲染机制有了一些变化，在接收到原生端对应生命周期方法中发送的通知才会显示。
     */
    @Override
    protected void onResume() {
        super.onResume();
        // flutterEngine.getLifecycleChannel()获取到的是一个LifecycleChannel对象
        // 类比于MethodChannel，作用大概就是将Flutter和原生端的生命周期相互联系起来。
        flutterEngine.getLifecycleChannel().appIsResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        flutterEngine.getLifecycleChannel().appIsInactive();
    }

    @Override
    protected void onStop() {
        super.onStop();
        flutterEngine.getLifecycleChannel().appIsPaused();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFlutterView.removeOnFirstFrameRenderedListener(flutterUiDisplayListener);
        FlutterContainerRegistry.removeContainer(mPageId);
        // 在 engine 销毁前先把 FlutterView 拿下来，否则先销毁引擎再拿下视图
        // 会导致 FlutterTextureView 在脱离视图树的回调中再触发一次销毁，而这次销毁中会存在 NPE 问题
        mFlutterContainer.removeAllViews();
        mFlutterView.removeAllViews();
        if (mRenderSurface != null && mRenderSurface.getAttachedRenderer() != null) {
            // 不能单独用 mRenderSurface attachToRenderer 和 detachRenderer
            // 这只是把 Surface 释放掉了，会导致 destroy mFlutterView 释放不掉，最终导致泄漏 Activity
            mFlutterView.detachFromFlutterEngine();
        }
        // 注意需要先移除view，然后再销毁flutterEngine。
        // 如果把flutterEngine销毁放到removeAllViews之前，则会出现异常：
        // Cannot execute operation because FlutterJNI is not attached to native.
        if (flutterEngine != null) {
            // 修复 MouseCursorChannel 的内存泄漏
            flutterEngine.getMouseCursorChannel().setMethodHandler(null);
            // detached 操作
            flutterEngine.getLifecycleChannel().appIsDetached();
            // 销毁
            flutterEngine.destroy();
        }
        if (mRenderSurface != null) {
            // 打断内存泄漏
            ((FixFlutterTextureView) mRenderSurface).setSurfaceTextureListener(null);
        }
    }

}
