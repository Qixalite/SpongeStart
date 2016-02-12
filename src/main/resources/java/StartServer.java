import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class StartServer{

    public static void main(String[] args){
        try {
            ClassLoader loader = URLClassLoader.newInstance(
                    new URL[]{new File("run", "server.jar").toURI().toURL()},
                    ClassLoader.getSystemClassLoader()
            );
            Class clazz = loader.loadClass("net.minecraftforge.fml.relauncher.ServerLaunchWrapper");
            Method method = clazz.getMethod("main", String[].class);
            new Thread(){
                @Override
                public void run() {
                    try {
                        method.invoke(null, (Object) args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            };

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
