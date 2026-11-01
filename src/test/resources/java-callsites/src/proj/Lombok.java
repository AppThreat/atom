package proj;

import lombok.extern.slf4j.Slf4j;
import web.Security;

@Slf4j
public class Lombok {
  private Security sec = new Security();

  void run() {
    log.info("starting");
    sec.login("me");
  }
}
