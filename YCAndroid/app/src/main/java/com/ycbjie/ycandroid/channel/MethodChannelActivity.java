package com.ycbjie.ycandroid.channel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ycbjie.ycandroid.R;
import com.ycbjie.ycandroid.router.RouterToNaAboutActivity;
import com.ycbjie.ycandroid.router.RouterToNaMeActivity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.android.FlutterView;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.loader.FlutterLoader;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.StandardMethodCodec;

/**
 * @author yc
 */
public class MethodChannelActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvContent;
    private ImageView ivImage;
    private FrameLayout rlFlutter;
    private FlutterView flutterView;
    private FlutterEngine flutterEngine;
    private DartExecutor dartExecutor;
    private BinaryMessenger binaryMessenger;
    private MethodChannel nativeChannel;
    /**
     * 从flutter这边传递数据到Android
     */
    public static final String METHOD_CHANNEL = "com.ycbjie.android/method";
    public static final int RESULT_OK1 = 100;
    public static final int RESULT_OK2 = 100;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_method);

        TextView tv = findViewById(R.id.tv);
        ivImage = findViewById(R.id.iv_image);
        tvContent = findViewById(R.id.tv_content);
        TextView tvInvoke = findViewById(R.id.tv_invoke);
        rlFlutter = findViewById(R.id.rl_flutter);
        TextView tvImage = findViewById(R.id.tv_image);
        tvInvoke.setOnClickListener(this);
        tvImage.setOnClickListener(this);

        tv.setText("MethodChannel通信交互（FlutterView）");
        addFlutterView();
        createChannel();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_invoke:
                invoke();
                break;
            case R.id.tv_image:
                Bitmap bitmap = getImageFromAssetsFile(MethodChannelActivity.this, "assets/images/map_marker_c.png");
                ivImage.setImageBitmap(bitmap);
                break;
            default:
                break;
        }
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
        if (flutterEngine != null) {
            flutterEngine.destroy();
        }
    }

    private void addFlutterView() {
        flutterEngine = new FlutterEngine(this);
        dartExecutor = flutterEngine.getDartExecutor();
        binaryMessenger = dartExecutor.getBinaryMessenger();
        flutterEngine.getNavigationChannel().setInitialRoute("method_channel");
        flutterEngine.getDartExecutor().executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
        );
        // 通过FlutterView引入Flutter编写的页面
        // 这里的FlutterView位于io.flutter.embedding.android包中
        // 和此前我们所创建的FlutterView（位于io.flutter.view包中）是不一样的。
        // 通过查看FlutterView的源码可以发现它继承自FrameLayout，因此像一个普通的View那样添加就可以了。
        flutterView = new FlutterView(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        rlFlutter.addView(flutterView, lp);

        //flutterEngine.getNavigationChannel().setInitialRoute("yc");

        // 关键代码，将Flutter页面显示到FlutterView中
        // 这个方法的作用就是将Flutter编写的UI页面显示到FlutterView中
        // flutterEngine的类型为FlutterEngine，字面意思就是Flutter引擎
        // 它负责在Android端执行Dart代码，将Flutter编写的UI显示到FlutterView/FlutterActivity/FlutterFragment中。
        flutterView.attachToFlutterEngine(flutterEngine);

        // FlutterEngine加载的路由名称为"/"，我们可以通过下面的代码指定初始路由名称
        // 传参的情况没有变化，直接在路由名称后面拼接参数就可以
        // 放在这里不生效，思考为什么
        //flutterEngine.getNavigationChannel().setInitialRoute("yc");
    }

    private void createChannel() {
        //FlutterEngine flutterEngine = new FlutterEngine(this);
        //DartExecutor dartExecutor = flutterEngine.getDartExecutor();
        //BinaryMessenger binaryMessenger = flutterEngine.getDartExecutor().getBinaryMessenger();

        // 在Android端创建MethodChannel时需要注意了，
        // 此前都是传入io.flutter.view包下的FlutterView作为BinaryMessenger，现在肯定是无法获取到该类对象了，
        // 那么这个参数应该传什么呢。通过查看继承关系我们可以找到两个相关的类：DartExecutor和DartMessenger。
        // DartExecutor可以通过FlutterEngine的getDartExecutor()方法获得，
        // 而DartMessenger又可以通过DartExecutor的getBinaryMessenger()方法获得
        // MethodChannel nativeChannel = new MethodChannel(dartExecutor, METHOD_CHANNEL);
        // 或
        //MethodChannel nativeChannel = new MethodChannel(binaryMessenger, METHOD_CHANNEL);
        // 或者
        nativeChannel = new MethodChannel(binaryMessenger, METHOD_CHANNEL, StandardMethodCodec.INSTANCE);
        nativeChannel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
            @Override
            public void onMethodCall(@NonNull MethodCall methodCall, @NonNull MethodChannel.Result result) {
                if ("doubi".equals(methodCall.method)) {
                    //接收来自flutter的指令
                    //跳转到指定Activity
                    Intent intent = new Intent(MethodChannelActivity.this, MethodResultActivity.class);
                    startActivityForResult(intent,RESULT_OK2);
                    //返回给flutter的参数
                    result.success("Na收到指令");
                } else if ("android".equals(methodCall.method)) {
                    //接收来自flutter的指令
                    //解析参数
                    Object text = methodCall.argument("flutter");
                    if (text instanceof String){
                        //带参数跳转到指定Activity
                        Intent intent = new Intent(MethodChannelActivity.this, RouterToNaMeActivity.class);
                        intent.putExtra("yc", (String) text);
                        startActivity(intent);
                    }else if (text instanceof List){
                        Intent intent = new Intent(MethodChannelActivity.this, RouterToNaAboutActivity.class);
                        intent.putStringArrayListExtra("yc", (ArrayList<String>) text);
                        startActivity(intent);
                    }
                    //返回给flutter的参数
                    result.success("Na成功");
                } else if ("image".equals(methodCall.method)) {
                    //接收来自flutter的指令
                    //解析参数，测试
                    String image = methodCall.argument("image");
                    Bitmap bitmap = getImageFromFlutterFile(MethodChannelActivity.this, image);
                    ivImage.setImageBitmap(bitmap);
                    //返回给flutter的参数
                    result.success("Na设置图片成功");
                } if ("goBackWithResult".equals(methodCall.method)) {
                    // 返回上一页，携带数据
                    Intent backIntent = new Intent();
                    backIntent.putExtra("message", (String) methodCall.argument("message"));
                    setResult(RESULT_OK1, backIntent);
                    finish();
                } else {
                    result.notImplemented();
                }
            }
        });
        //todo 需要注意，这里在创建MethodChannel时传入的FlutterEngine对象
        //     必须和我们此前创建好的FlutterView/FlutterFragment中使用的是同一个。

    }

    private void invoke(){
        if (nativeChannel!=null){
            HashMap<String , String> map = new HashMap<>();
            map.put("invokeKey","你好，这个是从NA传递过来的数据");
            //nativeChannel.resizeChannelBuffer(100);
            nativeChannel.invokeMethod("getFlutterResult", map , new MethodChannel.Result() {
                @SuppressLint("SetTextI18n")
                @Override
                public void success(@Nullable Object result) {
                    tvContent.setText("测试内容1："+result);
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void error(String errorCode, @Nullable String errorMessage, @Nullable Object errorDetails) {
                    tvContent.setText("测试内容：flutter传递给na数据传递错误1");
                }

                @Override
                public void notImplemented() {

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null && resultCode==RESULT_OK2) {
            // MethodResultActivity返回的数据
            String message = data.getStringExtra("message");
            Map<String, Object> result = new HashMap<>();
            result.put("message", message);
            // 调用Flutter端定义的方法
            nativeChannel.invokeMethod("onActivityResult", result, new MethodChannel.Result() {
                @SuppressLint("SetTextI18n")
                @Override
                public void success(@Nullable Object result) {
                    tvContent.setText("测试内容2："+result);
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void error(String errorCode, @Nullable String errorMessage, @Nullable Object errorDetails) {
                    tvContent.setText("测试内容：flutter传递给na数据传递错误2");
                }

                @Override
                public void notImplemented() {

                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    /**
     * 从资源路径获取文件，并加载成bitmap，注意仅限于加载图片资源
     * @param context                                   上下文
     * @param fileName                                  文件路径
     * @return
     */
    public Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap bitmap = null;
        AssetManager am = context.getResources().getAssets();
        InputStream is = null;
        try {
            is = am.open(fileName);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    public static Bitmap getImageFromFlutterFile(Context context, String fileName) {
        Bitmap bitmap = null;
        AssetManager assetManager = context.getResources().getAssets();
        FlutterLoader loader = FlutterLoader.getInstance();
        String key = loader.getLookupKeyForAsset(fileName);
        AssetFileDescriptor is = null;
        try {
            is = assetManager.openFd(key);
            FileInputStream inputStream = is.createInputStream();
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

}
