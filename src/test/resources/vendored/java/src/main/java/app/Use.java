package app;

import com.vend.Helper;
import com.vend.Shape;

public class Use implements Shape {
    private Helper field;

    public int area() { return 1; }

    int take(Helper h) {
        Helper local = new Helper();
        return local.go() + h.go();
    }
}
