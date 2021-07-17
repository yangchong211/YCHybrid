package com.ycbjie.ycandroid.channel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ycbjie.ycandroid.R;
import com.ycbjie.ycandroid.router.RouterToNaAboutActivity;
import com.ycbjie.ycandroid.router.RouterToNaMeActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.embedding.android.FlutterFragment;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.StandardMethodCodec;

import static com.ycbjie.ycandroid.channel.MethodChannelActivity.RESULT_OK1;
import static com.ycbjie.ycandroid.channel.MethodChannelActivity.RESULT_OK2;

/**
 * @author yc
 */
public class MethodChannelActivity2 extends AppCompatActivity implements View.OnClickListener {

    private TextView tvContent;
    private TextView tvInvoke;
    private FrameLayout rlFlutter;
    private MethodChannel nativeChannel;
    private FlutterFragment flutterFragment;
    public static final String METHOD_CHANNEL = "com.ycbjie.android/method";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_method);

        TextView tv = findViewById(R.id.tv);
        tvContent = findViewById(R.id.tv_content);
        tvInvoke = findViewById(R.id.tv_invoke);
        rlFlutter = findViewById(R.id.rl_flutter);

        tvInvoke.setOnClickListener(this);

        tv.setText("MethodChannel通信交互（FlutterFragment）");
        addFlutterView();
        tvInvoke.postDelayed(new Runnable() {
            @Override
            public void run() {
                //todo 思考如何解决flutterFragment.getFlutterEngine()报空指针问题，或者如何做到在flutterFragment渲染完后在创建channel
                createChannel();
            }
        },2000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (flutterFragment != null && flutterFragment.getFlutterEngine()!=null) {
            flutterFragment.getFlutterEngine().destroy();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.tv_invoke:
                invoke();
                break;
            default:
                break;
        }
    }


    private void addFlutterView() {
        // 通过FlutterFragment引入Flutter编写的页面
        // 通过FlutterFragment.createDefault()创建出FlutterFragment
        // 需要注意这里的FlutterFragment位于io.flutter.embedding.android包中
        //FlutterFragment flutterFragment = FlutterFragment.createDefault();

        // 通过FlutterFragment.withNewEngine()获取到NewEngineFragmentBuilder对象
        FlutterFragment.NewEngineFragmentBuilder fragmentBuilder = FlutterFragment.withNewEngine();
        // 使用建造者模式构造出FlutterFragment对象，可以通过initialRoute()方法指定初始路由名称。
        // 传递参数只需要在路由名称后面进行拼接。
        String route = "method_channel?{\"author\":\"杨充\"}";
        FlutterFragment.NewEngineFragmentBuilder initialRoute = fragmentBuilder.initialRoute(route);
        flutterFragment = initialRoute.build();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.rl_flutter, flutterFragment)
                .commit();


        // 存在的问题
        // 使用的withNewEngine()方法从名称上也能看出每次都是创建一个新的FlutterEngine对象来显示Flutter UI，
        // 但是从官方文档中我们可以了解到每个FlutterEngine对象在显示出Flutter UI之前
        // 是需要一个warm-up（不知道能不能翻译为预热）期的，这会导致屏幕呈现短暂的空白，
        // 解决方式就是预先创建并启动FlutterEngine，完成warm-up过程，然后将这个FlutterEngine缓存起来，
        // 之后使用这个FlutterEngine来显示出Flutter UI。
        // 解决方案看：FlutterFragmentCachedActivity


        // 如何获取到FlutterEngine对象呢？FlutterFragment中定义了一个getFlutterEngine()方法，
        // 从方法名来看大概就是获取FlutterEngine对象。
        // 尝试过创建MethodChannel时传入flutterFragment.getFlutterEngine().getDartExecutor()，
        // 运行后会直接抛出空指针异常，异常产生的位置在FlutterFragment的getFlutterEngine()方法中
        // 错误原因是这里的delegate为null，全局搜索一下，发现在FlutterFragment的onAttach()方法中会对delegate赋值，也就是说明此时没有执行onAttach()方法。
        // 猜测这就是由于上面提到过的FlutterEngine的warm-up机制，这是一个耗时过程，
        // 因此FlutterFragment并不会立刻执行onAttach()方法，导致我们在Activity的onCreate()方法中直接使用FlutterFragment的getFlutterEngine()方法会抛出异常。
        // todo 调用下面这句话会空指针崩溃
        // FlutterEngine flutterEngine = flutterFragment.getFlutterEngine();
    }

    private void createChannel() {
        // todo 调用下面这句话会空指针崩溃
        // 解决办法那就是等到渲染完后，在做执行操作？
        // 这个是activity页面还是fragment页面渲染完成？是fragment页面渲染完成才可以或者flutterEngine

        FlutterEngine flutterEngine = flutterFragment.getFlutterEngine();
        BinaryMessenger binaryMessenger = flutterEngine.getDartExecutor().getBinaryMessenger();

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
                    Intent intent = new Intent(MethodChannelActivity2.this, MethodResultActivity.class);
                    startActivityForResult(intent,RESULT_OK2);
                    //返回给flutter的参数
                    result.success("Na收到指令");
                } else if ("android".equals(methodCall.method)) {
                    //接收来自flutter的指令
                    //解析参数
                    Object text = methodCall.argument("flutter");
                    if (text instanceof String){
                        //带参数跳转到指定Activity
                        Intent intent = new Intent(MethodChannelActivity2.this, RouterToNaMeActivity.class);
                        intent.putExtra("yc", (String) text);
                        startActivity(intent);
                    }else if (text instanceof List){
                        Intent intent = new Intent(MethodChannelActivity2.this, RouterToNaAboutActivity.class);
                        intent.putStringArrayListExtra("yc", (ArrayList<String>) text);
                        startActivity(intent);
                    }
                    //返回给flutter的参数
                    result.success("Na成功");
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
            nativeChannel.invokeMethod("getFlutterResult", map , new MethodChannel.Result() {
                @SuppressLint("SetTextI18n")
                @Override
                public void success(@Nullable Object result) {
                    tvContent.setText("测试内容："+result);
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void error(String errorCode, @Nullable String errorMessage, @Nullable Object errorDetails) {
                    tvContent.setText("测试内容：flutter传递给na数据传递错误");
                }

                @Override
                public void notImplemented() {

                }
            });
        }
    }
}
