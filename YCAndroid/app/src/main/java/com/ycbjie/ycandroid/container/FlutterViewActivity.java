package com.ycbjie.ycandroid.container;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ycbjie.ycandroid.R;
import com.ycbjie.ycandroid.router.RouterToNaAboutActivity;
import com.ycbjie.ycandroid.router.RouterToNaMeActivity;

import java.util.ArrayList;
import java.util.List;

import io.flutter.Log;
import io.flutter.embedding.android.FlutterSurfaceView;
import io.flutter.embedding.android.FlutterTextureView;
import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.systemchannels.NavigationChannel;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

/**
 * @author yc
 */
public class FlutterViewActivity extends AppCompatActivity {

    private FrameLayout rlFlutter;
    private TextView tvOpen;
    private FlutterView flutterView;
    private FlutterEngine flutterEngine;
    private BinaryMessenger binaryMessenger;
    private NavigationChannel navigationChannel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flutter_view);
        rlFlutter = findViewById(R.id.rl_flutter);
        tvOpen = findViewById(R.id.tv_open);

        addFlutterView();
        tvOpen.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("VisibleForTests")
            @Override
            public void onClick(View v) {
                if (flutterView!=null && flutterView.isAttachedToFlutterEngine()){
                    Toast.makeText(FlutterViewActivity.this,
                            "跳转",Toast.LENGTH_LONG).show();
                    navigationChannel.pushRoute("yc");
                }
            }
        });
    }

    /**
     * 分别在onResume()、onPause()和onStop()方法中调用了LifecycleChannel的appIsResumed()、
     * appIsInactive()和appIsPaused()方法，作用就是同步Flutter端与原生端的生命周期。
     *
     * 猜想可能是FlutterVIew的渲染机制有了一些变化，在接收到原生端对应生命周期方法中发送的通知才会显示。
     * todo 研究下原理
     */
    @Override
    protected void onResume() {
        super.onResume();
        // flutterEngine.getLifecycleChannel()获取到的是一个LifecycleChannel对象，类比于MethodChannel，
        // 作用大概就是将Flutter和原生端的生命周期相互联系起来。
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
        flutterEngine.getLifecycleChannel().appIsDetached();
    }

    private void addFlutterView() {
        flutterEngine = new FlutterEngine(this);
        binaryMessenger = flutterEngine.getDartExecutor().getBinaryMessenger();
        //获取路由channel通信对象
        navigationChannel = flutterEngine.getNavigationChannel();
        String route = "router_channel?{\"name\":\"杨充\"}";
        //设置初始化路由
        navigationChannel.setInitialRoute(route);
        navigationChannel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {
                String method = methodCall.method;
                Log.i("onMethodCall","---"+method);
                if ("android".equals(method)) {
                    //接收来自flutter的指令
                    //解析参数
                    String router = methodCall.argument("router");
                    Object text = methodCall.argument("flutter");
                    if (router==null || router.length()==0){
                        Toast.makeText(FlutterViewActivity.this,
                                "路由地址不能为空",Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (router.equals("main/me")) {
                        //带参数跳转到指定Activity
                        Intent intent = new Intent(
                                FlutterViewActivity.this,
                                RouterToNaMeActivity.class);
                        intent.putExtra("yc", (String) text);
                        startActivity(intent);
                    } else if (router.equals("main/about")){
                        Intent intent = new Intent(
                                FlutterViewActivity.this, RouterToNaAboutActivity.class);
                        intent.putStringArrayListExtra("yc", (ArrayList<String>) text);
                        startActivity(intent);
                    }
                    //返回给flutter的参数
                    result.success("Na成功");
                }
            }
        });
        flutterEngine.getDartExecutor().executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
        );
        // 通过FlutterView引入Flutter编写的页面
        // 这里的FlutterView位于io.flutter.embedding.android包中
        // 和此前我们所创建的FlutterView（位于io.flutter.view包中）是不一样的。
        // 通过查看FlutterView的源码可以发现它继承自FrameLayout，因此像一个普通的View那样添加就可以了。
        // flutterView = new FlutterView(this);
        flutterView = new FlutterView(this,(FlutterTextureView)(new FlutterTextureView(this)));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rlFlutter.addView(flutterView, lp);

        // todo 放在这里不生效，思考为什么
        //flutterEngine.getNavigationChannel().setInitialRoute("yc");

        // 关键代码，将Flutter页面显示到FlutterView中
        // 这个方法的作用就是将Flutter编写的UI页面显示到FlutterView中
        // flutterEngine的类型为FlutterEngine，字面意思就是Flutter引擎
        // 它负责在Android端执行Dart代码，将Flutter编写的UI显示到FlutterView/FlutterActivity/FlutterFragment中。
        flutterView.attachToFlutterEngine(flutterEngine);

        // FlutterEngine加载的路由名称为"/"，我们可以通过下面的代码指定初始路由名称
        // 传参的情况没有变化，直接在路由名称后面拼接参数就可以
        // todo 放在这里不生效，思考为什么
        //flutterEngine.getNavigationChannel().setInitialRoute("yc");
    }
}
