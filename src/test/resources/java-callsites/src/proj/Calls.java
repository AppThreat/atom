package proj;

import web.Security;

public class Calls {
  private Security sec = new Security();

  void call() {
    sec.login("me").header("a", "b");
    Security.http().login("me");
  }
}
