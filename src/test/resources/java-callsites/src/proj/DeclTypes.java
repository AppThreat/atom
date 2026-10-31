package proj;

import java.util.List;
import java.io.IOException;
import web.Security;

public class DeclTypes {
  private Security field;

  List<String> load(java.util.Map<String, String> byKey) throws IOException {
    List<String> local = List.of("a");
    return local;
  }
}
