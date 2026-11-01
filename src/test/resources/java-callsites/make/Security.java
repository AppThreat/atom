package web;

public class Security {
    public web.Session login(String user) { return new web.Session(); }
    public static web.Security http() { return new web.Security(); }
    public static final String VERSION = "1.0";
    public enum Method { GET, POST }
}
