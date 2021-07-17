

import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:ycflutter/gank/local/string_base.dart';
import 'package:ycflutter/gank/local/string_zh.dart';
import 'package:ycflutter/gank/local/string_en.dart';

///自定义多语言实现
class GankLocalizations {

  final Locale locale;

  GankLocalizations(this.locale);

  static Map<String, StringBase> _localizedValues = {
    'zh': StringZh(),
    'en': StringZh(),
//    'en': StringEn(),
  };

  StringBase get currentLocalized {
    return _localizedValues[locale.languageCode];
  }

  static GankLocalizations of(BuildContext context) {
    return Localizations.of(context, GankLocalizations);
  }
}