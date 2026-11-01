package proj;

import web.Security;

public class Initializers {
  private static final Security SHARED = Security.http();
  private web.Session session = new web.Session();

  void use() {
  }
}
