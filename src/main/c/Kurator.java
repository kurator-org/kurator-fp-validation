
public class Kurator {
    static {
        System.loadLibrary("kurator"); // Load native library at runtime
    }

    // Declare a native method sayHello() that receives nothing and returns void
    private native void sayHello();

    // Test Driver
    public static void main(String[] args) {
        new Kurator().sayHello();  // invoke the native method
    }
}
