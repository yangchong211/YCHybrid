

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:ycflutter/gank/local/gank_localizations.dart';

class GankLocalizationsDelegate
    extends LocalizationsDelegate<GankLocalizations> {
  GankLocalizationsDelegate();

  @override
  bool isSupported(Locale locale) {
    ///支持中文和英语
    return ['en', 'zh'].contains(locale.languageCode);
  }

  ///根据locale，创建一个对象用于提供当前locale下的文本显示
  @override
  Future<GankLocalizations> load(Locale locale) {
    return SynchronousFuture<GankLocalizations>(GankLocalizations(locale));
  }

  @override
  bool shouldReload(LocalizationsDelegate<GankLocalizations> old) {
    return false;
  }

  ///全局静态的代理
  static GankLocalizationsDelegate delegate = GankLocalizationsDelegate();
}