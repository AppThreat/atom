package proj;

import web.Security;
import static web.Security.http;

public class Imports {
  web.Session viaQualified() {
    return new web.Session().header("a", "b");
  }

  Security staticUse() {
    return http();
  }
}
