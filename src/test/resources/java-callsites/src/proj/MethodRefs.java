package proj;

import java.util.function.Consumer;
import java.util.function.Supplier;
import web.Security;
import web.Session;

public class MethodRefs {
  void run() {
    Supplier<Security> s = Security::http;
    Supplier<Session> m = Session::new;
    Consumer<String> own = new Handler()::handle;
    java.util.function.IntFunction<String[]> arrays = String[]::new;
    use(s, m, own, arrays);
  }

  void use(
      Supplier<Security> s,
      Supplier<Session> m,
      Consumer<String> own,
      java.util.function.IntFunction<String[]> arrays) {}
}
