
import 'package:flutter/material.dart';
import 'package:yc_flutter_utils/log/log_utils.dart';
import 'package:yc_flutter_utils_example/utils/data_utils_page.dart';
import 'package:yc_flutter_utils_example/utils/file_utils_page.dart';
import 'package:yc_flutter_utils_example/utils/get_it_page.dart';
import 'package:yc_flutter_utils_example/utils/log_utils_page.dart';
import 'package:yc_flutter_utils_example/widget/custom_raised_button.dart';
//StatelessWidget表示组件，一切都是widget，可以理解为组件
//有状态的组件（Stateful widget）
//无状态的组件（Stateless widget）
//Stateful widget可以拥有状态，这些状态在widget生命周期中是可以变的，而Stateless widget是不可变的
class HomePage extends StatefulWidget{

  HomePage({Key key, this.title}) : super(key: key);
  final String title;

  //createState()来创建状态(State)对象
  @override
  State<StatefulWidget> createState() {
    return new HomePageState();
  }

}

class HomePageState extends State<HomePage>{


  @override
  void initState() {
    super.initState();
    LogUtils.init(isDebug: true);
  }

  @override
  void didChangeDependencies() {
    super.didChangeDependencies();
  }

  @override
  void deactivate() {
    super.deactivate();
  }

  @override
  void dispose() {
    super.dispose();
  }

  void _incrementCounter() {
    //setState方法的作用是通知Flutter框架，有状态发生了改变，Flutter框架收到通知后，
    //会执行build方法来根据新的状态重新构建界面， Flutter 对此方法做了优化，
    //使重新执行变的很快，所以你可以重新构建任何需要更新的东西，而无需分别去修改各个widget。
    //todo 思考一下原理是什么？具体做了什么优化？没有更新的东西会刷新嘛？
    setState(() {

    });
  }


  //在构建页面时，会调用组件的build方法
  //widget的主要工作是提供一个build()方法来描述如何构建UI界面
  //通常是通过组合、拼装其它基础widget
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: new AppBar(
          title: new Text(this.widget.title),
        ),
        body: new Center(
          child: new ListView(
            children: <Widget>[
              CustomRaisedButton(new GetItPage(), "serviceLocator测试"),
              CustomRaisedButton(new LogUtilsPage(), "LogUtils 日志工具类"),
              CustomRaisedButton(new DatePage(), "DateUtils 日期工具类"),
              CustomRaisedButton(new LogUtilsPage(), "JsonUtils Json工具类"),
              CustomRaisedButton(new FileStoragePage(), "FileUtils 文件工具类"),
            ],
          ),
        ),
      ),
    );
  }

}

// 关闭mas日志
// MASConfig.SWITCH_PRINT_TRACE_LOG = false;