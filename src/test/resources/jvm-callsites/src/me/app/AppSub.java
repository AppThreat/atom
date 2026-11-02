package me.app;

public class AppSub extends AppBase {

    int throughGrand() {
        return foo();
    }

    int throughBase() {
        return bar();
    }

    int throughOverload() {
        return bar("x");
    }
}
