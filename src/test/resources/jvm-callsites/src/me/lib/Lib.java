package me.lib;

import me.app.App;

public class Lib {

    public static String CONSTANT = "c";

    public int count;

    private final String label;

    public Lib(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static int util(int n) {
        return n + 1;
    }

    public static int roundtrip(int n) {
        return App.base(n) + 1;
    }
}
