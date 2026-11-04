package app;

import com.vend.Helper;
import notvend.Util;

public class Main extends Helper {
    public static void main(String[] args) {
        Helper.run(1);
        new Helper().go();
        Util.help();
    }
}
