package proj;

import web.Security;

public class Literals {
  void read() {
    Class<?> c = Security.class;
    java.util.function.Function<String, String> f = Security::http;
    String v = Security.VERSION;
    Security.Method m = Security.Method.GET;
  }
}
