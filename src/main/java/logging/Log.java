package main.java.logging;

public class Log {
    private static final String boldc = "\33[1m";
    private static final String cursivec = "\33[3m";
    private static final String transparentc = "\33[90m";
    private static final String redc = "\33[91m";
    private static final String greenc = "\33[92m";
    private static final String yellowc = "\33[93m";
    private static final String bluec = "\33[94m";
    private static final String pinkc = "\33[95m";
    private static final String brightc = "\33[97m";
    private static final String endc = "\33[0m";

    public static void out(String message) {
        System.out.println(message);
    }

    public static void bold(String message) {
        System.out.println(boldc + message + endc);
    }

    public static void cursive(String message) {
        System.out.println(cursivec + message + endc);
    }

    public static void transparent(String message) {
        System.out.println(transparentc + message + endc);
    }

    public static void red(String message) {
        System.out.println(redc + message + endc);
    }

    public static void green(String message) {
        System.out.println(greenc + message + endc);
    }

    public static void yellow(String message) {
        System.out.println(yellowc + message + endc);
    }

    public static void blue(String message) {
        System.out.println(bluec + message + endc);
    }

    public static void pink(String message) {
        System.out.println(pinkc + message + endc);
    }

    public static void bright(String message) {
        System.out.println(brightc + message + endc);
    }
}
