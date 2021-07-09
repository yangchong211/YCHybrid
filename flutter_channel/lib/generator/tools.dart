import 'dart:convert';
import 'dart:io';
import 'dart:mirrors';

import 'package:flutter_channel/constant/constants.dart';

import '../ast/ast.dart';

/// The current version of pigeon. This must match the version in pubspec.yaml.
const String pigeonVersion = '0.2.0';

/// Read all the content from [stdin] to a String.
String readStdin() {
  final List<int> bytes = <int>[];
  int byte = stdin.readByteSync();
  while (byte >= 0) {
    bytes.add(byte);
    byte = stdin.readByteSync();
  }
  return utf8.decode(bytes);
}

/// A helper class for managing indentation, wrapping a [StringSink].
class Indent {
  /// Constructor which takes a [StringSink] [Ident] will wrap.
  Indent(this._sink);

  int _count = 0;
  final StringSink _sink;

  /// String used for newlines (ex "\n").
  final String newline = '\n';

  /// String used to represent a tab.
  final String tab = '  ';

  /// Increase the indentation level.
  void inc([int level = 1]) {
    _count += level;
  }

  /// Decrement the indentation level.
  void dec([int level = 1]) {
    _count -= level;
  }

  /// Returns the String representing the current indentation.
  String str() {
    String result = '';
    for (int i = 0; i < _count; i++) {
      result += tab;
    }
    return result;
  }

  /// Replaces the newlines and tabs of input and adds it to the stream.
  void format(String input,
      {bool leadingSpace = true, bool trailingNewline = true}) {
    final List<String> lines = input.split('\n');
    for (int i = 0; i < lines.length; ++i) {
      final String line = lines[i];
      if (i == 0 && !leadingSpace) {
        addln(line.replaceAll('\t', tab));
      } else if (i == lines.length - 1 && !trailingNewline) {
        write(line.replaceAll('\t', tab));
      } else {
        writeln(line.replaceAll('\t', tab));
      }
    }
  }

  /// Scoped increase of the ident level.  For the execution of [func] the
  /// indentation will be incremented.
  void scoped(
      String begin,
      String end,
      Function func, {
        bool addTrailingNewline = true,
      }) {
    if (begin != null) {
      _sink.write(begin + newline);
    }
    nest(1, func);
    if (end != null) {
      _sink.write(str() + end);
      if (addTrailingNewline) {
        _sink.write(newline);
      }
    }
  }

  /// Like `scoped` but writes the current indentation level.
  void writeScoped(
      String begin,
      String end,
      Function func, {
        bool addTrailingNewline = true,
      }) {
    scoped(str() + (begin ?? ''), end, func,
        addTrailingNewline: addTrailingNewline);
  }

  /// Scoped increase of the ident level.  For the execution of [func] the
  /// indentation will be incremented by the given amount.
  void nest(int count, Function func) {
    inc(count);
    func();
    dec(count);
  }

  /// Add [text] with indentation and a newline.
  void writeln(String text) {
    if (text.isEmpty) {
      _sink.write(newline);
    } else {
      _sink.write(str() + text + newline);
    }
  }

  void writeNewline() {
    _sink.write(newline);
  }

  /// Add [text] with indentation.
  void write(String text) {
    _sink.write(str() + text);
  }

  /// Add [text] with a newline.
  void addln(String text) {
    _sink.write(text + newline);
  }

  /// Just adds [text].
  void add(String text) {
    _sink.write(text);
  }
}

/// Create the generated channel name for a [func] on a [api].
String makeChannelName(Module module, Method func) {
  return Constants.channelPrefix+'.${module.name}.${func.name}';
}

/// Represents the mapping of a Dart datatype to a Host datatype.
class HostDatatype {
  /// Parametric constructor for HostDatatype.
  HostDatatype({
    this.datatype,
    this.isBuiltin,
  });

  /// The [String] that can be printed into host code to represent the type.
  final String datatype;

  /// `true` if the host datatype is something builtin.
  final bool isBuiltin;
}

/// Calculates the [HostDatatype] for the provided [Field].  It will check the
/// field against the `classes` to check if it is a builtin type.
/// `builtinResolver` will return the host datatype for the Dart datatype for
/// builtin types.  `customResolver` can modify the datatype of custom types.
HostDatatype getHostDatatype(
    Field field, List<Model> classes, String Function(String) builtinResolver,
    {String Function(String) customResolver}) {
  final String datatype = builtinResolver(field.type);
  if (datatype == null) {
    if (classes.map((Model x) => x.name).contains(field.type)) {
      final String customName = customResolver != null
          ? customResolver(field.type)
          : field.type;
      return HostDatatype(datatype: customName, isBuiltin: false);
    } else {
      throw Exception(
          'unrecognized datatype for field:"${field.name}" of type:"${field.type}"');
    }
  } else {
    return HostDatatype(datatype: datatype, isBuiltin: true);
  }
}

/// String to be printed after `generatedCodeWarning`.
const String seeAlsoWarning = 'See also: https://pub.dev/packages/pigeon';

/// Collection of keys used in dictionaries across generators.
class Keys {
  /// The key in the result hash for the 'result' value.
  static const String result = 'result';

  /// The key in the result hash for the 'error' value.
  static const String error = 'error';

  /// The key in an error hash for the 'code' value.
  static const String errorCode = 'code';

  /// The key in an error hash for the 'message' value.
  static const String errorMessage = 'message';

  /// The key in an error hash for the 'details' value.
  static const String errorDetails = 'details';
}

/// Returns true if `type` represents 'void'.
bool isVoid(TypeMirror type) {
  return MirrorSystem.getName(type.simpleName) == 'void';
}
