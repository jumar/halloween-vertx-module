package com.jumar.vertx;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.platform.Verticle;

import com.google.common.base.Joiner;

public class WebSocketServer extends Verticle
{
	Process lProc = null;

	@Override
	public void start()
	{
		final Logger lLogger = container.logger();
		lLogger.info("WebSocketServer Verticle started");
		List<URL> aMedia = cParameters.mGetInstance().mGetMedia();

		vertx.createHttpServer().websocketHandler(new Handler<ServerWebSocket>()
		{
			@Override
			public void handle(final ServerWebSocket ws)
			{
				if (ws.path().equals("/echo")|| ws.path().equals("echo"))
				{
					ws.dataHandler(new Handler<Buffer>()
					{
						@Override
						public void handle(Buffer data)
						{
							String lDataStr = data.toString();
							lLogger.info("Echoing: " + lDataStr);
							ws.writeTextFrame(lDataStr);
						}
					});
				}
				else if (ws.path().startsWith("/mediaplayer/play"))
				{
					ws.dataHandler(new Handler<Buffer>()
					{
						@Override
						public void handle(Buffer data)
						{
							String lDataStr = data.toString();
							if (lDataStr.matches("[0-9]+"))
							{
								int lMediaID = Integer.parseInt(lDataStr);
								if (lMediaID < aMedia.size())
								{
									mStop(lLogger);
									lLogger.info("PLAYING index " + lMediaID);
									mPlay(aMedia.get(lMediaID), (proc) -> lProc = proc);
									ws.writeTextFrame(data.toString());
								}
								else
								{
									lLogger.error("No media at index: " + lMediaID);
								}
							}
							else
							{
								lLogger.error("Unknown play command option: " + lDataStr);
							}
						}
					});
				}
				else if (ws.path().endsWith("/mediaplayer/stop"))
				{
					ws.dataHandler(new Handler<Buffer>()
					{
						@Override
						public void handle(Buffer data)
						{
							mStop(lLogger);
							ws.writeTextFrame(data.toString()); // echo data
						}

					});
				}
				else
				{
					lLogger.error("path: " + ws.path());
					ws.reject();
				}

			}
		}).listen(8080);
	}

	private void mStop(final Logger lLogger)
	{
		if (lProc != null && lProc.isAlive())
		{
			lLogger.info("STOPING");
			lProc.destroy();
		}
	}

	private Thread mPlay(URL pUrl, Consumer<Process> pConsumer)
	{
		final Logger lLogger = container.logger();
		Thread lTrd = new Thread()
		{
			@Override
			public void run()
			{
				List<String> lCmd = new ArrayList<String>();
				lCmd.addAll(cParameters.mGetInstance().mGetMediaPlayerCmdAndArgs());
				try
				{
					String lFile = new File(pUrl.toURI()).getAbsolutePath();
					lCmd.add(lFile);
					ProcessBuilder lPB = new ProcessBuilder(lCmd);
					lLogger.info("Playing: " + Joiner.on(' ').join(lPB.command()));
					lPB.redirectErrorStream(true);
					try
					{
						Process lProc = lPB.start();
						pConsumer.accept(lProc);
						OutputReader lOutputReader = new OutputReader(lProc.getInputStream());
						Thread lReaderThread = new Thread(lOutputReader, "command output reader");
						lReaderThread.start();
						lLogger.info("Return value: " + lProc.waitFor());
						lReaderThread.interrupt();
					}
					catch (IOException | InterruptedException e)
					{
						lLogger.error(e);
					}
				}
				catch (URISyntaxException e1)
				{
					lLogger.error(e1);
				}

			}
		};
		lTrd.start();
		return lTrd;
	}

	class OutputReader implements Runnable
	{

		private final InputStream inputStream;

		OutputReader(InputStream inputStream)
		{
			this.inputStream = inputStream;
		}

		private BufferedReader getBufferedReader(InputStream is)
		{
			return new BufferedReader(new InputStreamReader(is));
		}

		@Override
		public void run()
		{
			BufferedReader lBR = getBufferedReader(inputStream);
			String lLine = "";
			try
			{
				while ((lLine = lBR.readLine()) != null)
				{
					// System.out.println(ligne);
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

}