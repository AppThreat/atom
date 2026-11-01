package proj;

import web.RequestParam;
import web.Security;

@RestController
public class Annotations {
  @Override
  public String toString() {
    return "a";
  }

  void handle(@RequestParam("id") String id) {
  }
}
