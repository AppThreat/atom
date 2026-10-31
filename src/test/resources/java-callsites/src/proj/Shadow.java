package proj;

import web.*;

// The project Handler in this package shadows the web one the wildcard import names.
public class Shadow {
  private Handler handler = new Handler();

  void use() {
    handler.handle("x");
  }
}
