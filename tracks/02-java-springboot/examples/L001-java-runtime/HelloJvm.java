public class HelloJvm {
    public static void main(String[] args) {
        String name = args.length > 0 ? args[0] : "frontend developer";

        System.out.println("Hello, " + name + "!");
        System.out.println("Java source file: HelloJvm.java");
        System.out.println("Compiled bytecode: HelloJvm.class");
        System.out.println("Runtime JVM: " + System.getProperty("java.vm.name"));
        System.out.println("Java version: " + System.getProperty("java.version"));
    }
}
