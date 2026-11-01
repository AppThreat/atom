package proj;

import lombok.Data;
import web.Security;

@Data
public class Dto {
  private Security security;
  private int count;
  private String[] tags;

  void use(Runnable task) {
    Runnable later = () -> task.run();
    later.run();
  }
}
