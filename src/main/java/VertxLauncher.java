import java.io.File;

import com.jumar.vertx.WebSocketServer;


public class VertxLauncher {
    public static void main(String[] args)
    {
	org.vertx.java.platform.impl.cli.Starter.main(new String[]{"run",WebSocketServer.class.getName(), "-cp", new File("bin").getAbsolutePath()});
    }
}
