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
    use(s, m, own);
  }

  void use(Supplier<Security> s, Supplier<Session> m, Consumer<String> own) {}
}
