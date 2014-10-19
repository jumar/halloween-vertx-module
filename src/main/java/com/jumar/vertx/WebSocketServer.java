package com.jumar.vertx;

import java.net.URL;

import javafx.scene.media.AudioClip;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

public class WebSocketServer extends Verticle
{

	@Override
    public void start()
	{
		final Logger logger = container.logger();
		logger.info("WebSocketServer Verticle started");

		URL lRes = getClass().getResource("Aftermath.mp3");
		AudioClip lClip = new AudioClip(lRes.toString());

		vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>()
		{
			@Override
            public void handle(final ServerWebSocket ws)
			{
				if (ws.path().startsWith("/audioplayer"))
				{
					if (ws.path().endsWith("/play"))
					{
						ws.dataHandler(new Handler<Buffer>()
						{
							@Override
                            public void handle(Buffer data)
							{
								logger.info("PLAYING");
								lClip.play(); // play
								ws.writeTextFrame(data.toString()); // echo data
							}
						});
					}
					else if (ws.path().endsWith("/stop"))
					{
						ws.dataHandler(new Handler<Buffer>()
						{
							@Override
                            public void handle(Buffer data)
							{
								if (lClip.isPlaying())
		                        {
									logger.info("STOPING");
			                        lClip.stop();
			                        ws.writeTextFrame(data.toString()); // echo data
		                        }
							}
						});
					}
					else
					{
						logger.error("path: " + ws.path());
						ws.reject();
					}
				}
				else
				{
					logger.error("path: " + ws.path());
					ws.reject();
				}
			}
		}).listen(8080);
	}
}