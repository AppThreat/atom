package me.app;

import java.util.function.Predicate;

import me.lib.Lib;

public class App {

    static final String CONST = Lib.CONSTANT;

    private final Lib lib = new Lib("a");

    String viaLib() {
        return lib.label();
    }

    static String viaStatic() {
        return Lib.util(1) + CONST;
    }

    String putGet() {
        lib.count = 2;
        return String.valueOf(lib.count);
    }

    boolean kindOf(Object o) {
        return o instanceof Lib;
    }

    Lib cast(Object o) {
        return (Lib) o;
    }

    Class<?> literal() {
        return Lib.class;
    }

    String concat(String s) {
        return "x" + s;
    }

    public static int base(int n) {
        return n - 1;
    }

    Predicate<String> handle() {
        return String::isEmpty;
    }

    java.util.function.IntUnaryOperator ownHandle() {
        return App::base;
    }

    Predicate<String> lambda() {
        return s -> s.isBlank();
    }

    String twoHandles(java.util.List<String> xs) {
        java.util.function.Function<String, Integer> length = String::length;
        java.util.function.Supplier<StringBuilder> builder = StringBuilder::new;
        return builder.get().append(length.apply(xs.get(0))).toString();
    }
}
