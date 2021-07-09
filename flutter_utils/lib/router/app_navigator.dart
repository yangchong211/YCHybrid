import 'package:flutter/material.dart';
import 'package:yc_flutter_utils/log/log_utils.dart';


/*
 * <pre>
 *     @author yangchong
 *     blog  : https://github.com/yangchong211
 *     time  : 2019/5/30
 *     desc  : 路由简单封装
 *     revise: 路由几个问题
 *             1.如何处理路径配置错误跳转降级操作
 *             2.参数的传递，多个参数如何操作
 *             3.context有什么作用
 *             4.如何统一管理路由，避免混乱
 * </pre>
 */
class AppNavigator {

  // 什么是路由管理？？？
  // 简单俩说，导航管理都会维护一个路由栈，
  // 路由入栈(push)操作对应打开一个新页面，
  // 路由出栈(pop)操作对应页面关闭操作，
  // 而路由管理主要是指如何来管理路由栈。

  static pushGlobal(GlobalKey<NavigatorState> navigatorKey, Widget scene) {
    // Another exception was thrown:
    // Navigator operation requested with a context that does not include a Navigator.
    // 因为flutter会根据这个context一直上溯，一直到根节点的widget，
    // 注意，上溯是根据context的，会上溯到这个context相关的widget的最根节点
    // 导航到新路由
    try {
      //执行build方法
      var currentState = navigatorKey.currentState;
      currentState.push(MaterialPageRoute(
        builder: (BuildContext context) => scene,
      ),
      );
    } catch (e, stack) {
      // 有异常时则弹出错误提示
      LogUtils.e("路由异常"+e.toString()+"-----"+stack.toString());
    }
  }

  static push(BuildContext context, Widget scene) {
    // 第一种方式
    // Navigator.of(context).push(new MaterialPageRoute(builder: (context) {
    //   return new LoginPage();
    // }));

    // 第二种方式
    // Future push = Navigator.push(context, new MaterialPageRoute(builder: (context) {
    //   return new LoginPage();
    // }));

    // 简单封装，导航到新路由
    Navigator.push(context, MaterialPageRoute(
        builder: (BuildContext context) => scene,
      ),
    );
  }

  static pushAnimation(BuildContext context, Widget scene) {
    Navigator.push(context, PageRouteBuilder(
        //动画时间为500毫秒
        transitionDuration: Duration(milliseconds: 500),
        pageBuilder: (BuildContext context, Animation animation,
            Animation secondaryAnimation) {
          return new FadeTransition(
            //使用渐隐渐入过渡,
            opacity: animation,
            //路由页面
            child: scene,
          );
        },
      ),
    );
  }

  static pushNamed(BuildContext context,String path) {
    // 如何注册路由表，如下所示
    // MaterialApp(
    //     title: 'Flutter Demo',
    //     initialRoute:"/", //名为"/"的路由作为应用的home(首页)
    //     theme: ThemeData(
    //       primarySwatch: Colors.blue,
    //     ),
    //     //注册路由表
    //     routes:{
    //       "new_page":(context) => NewRoute(),
    //       "/":(context) => MyHomePage(title: 'Flutter Demo Home Page'), //注册首页路由
    //     }
    // );


    // 简单封装，导航到新路由
    if(path!=null){
      //过路由名称来打开新路由，可以使用Navigator 的pushNamed方法
      Navigator.pushNamed(context,path);
    }
  }

  static pushNamedArguments(BuildContext context,String path,{Object arguments}) {
    // 简单封装，导航到新路由
    if(path!=null){
      //过路由名称来打开新路由，可以使用Navigator 的pushNamed方法
      Navigator.pushNamed(context,path,arguments: "参数");
    }
  }

  static replace(BuildContext context,Widget old, Widget scene) {
    // 简单封装，导航到新路由
    //Navigator.replace(context,{old,scene});
  }

  static pushAndRemoveUntil(BuildContext context, Widget scene) {
    Navigator.pushAndRemoveUntil(
      context,
      MaterialPageRoute(
        builder: (BuildContext context) => scene,
      ), (route) => route == null
    );
  }

  static pushResult(BuildContext context, Widget scene, Function(Object) function) {
    Navigator.push(context, MaterialPageRoute(
        builder: (BuildContext context) => scene,
      ),
    ).then((result){
      // 页面返回result为null
      if (result == null){
        return;
      }
      function(result);
    }).catchError((error) {
      print("$error");
    });
  }
}
