package proj;

import dup.Shared;
import shaded.com.example.biz.Util;
import web.Security;

public class Purl {
  void run() {
    Shared.run();
    Util.trim("x");
    Security.http();
  }
}
