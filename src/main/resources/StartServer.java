import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class StartServer{

    public static void main(String[] args){
        try {
            System.out.println("Running folder: " + new File(".").getAbsolutePath());
            File server = new File("server.jar");
            Manifest m = new JarFile(server).getManifest();

            ClassLoader loader = new SpongeClassLoader(
                    new URL[]{server.toURI().toURL()}, StartServer.class.getClassLoader()
            );

            try {
                loader.loadClass(m.getMainAttributes().getValue("Main-Class"))
                        .getMethod("main", String[].class)
                        .invoke(null, (Object) args);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class SpongeClassLoader extends URLClassLoader {

        public SpongeClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
            try {
                //making sure the classloaders in forge don't get confused :).
                Method ADDURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                ADDURL.setAccessible(true);

                for (URL url : urls) {
                    try {
                        ADDURL.invoke(getClass().getClassLoader(), url);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }

        }
    }
}
