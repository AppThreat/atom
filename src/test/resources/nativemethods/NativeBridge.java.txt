package demo;

/** JNI boundary: native methods have no body in bytecode, yet are called like any other. */
public class NativeBridge {
    static {
        System.loadLibrary("bridge");
    }

    public static native String stringFromJNI();

    public native int checksum(byte[] data, int length);

    private static native void nativeLog(String message);

    public int verify(String input) {
        byte[] bytes = input.getBytes();
        int sum = checksum(bytes, bytes.length);
        nativeLog("checksum " + sum);
        return sum;
    }

    public static String greeting() {
        return "native says " + stringFromJNI();
    }
}
