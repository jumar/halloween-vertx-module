package com.jumar.vertx;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.logging.impl.LoggerFactory;

import com.google.common.base.Joiner;

public class cParameters
{
	private static final String MEDIAPLAYER_PROPERTIES = "mediaplayer.properties";
	final Logger lLogger = LoggerFactory.getLogger(getClass());
	private List<URL> aMedia = new ArrayList<URL>();
	private String aMediaPlayerCmd;
	private String[] aMediaPlayerArgs;

	private static cParameters INSTANCE;

	public static cParameters mGetInstance()
	{
		if(INSTANCE == null)
        {
	        INSTANCE = new cParameters();
        }
		return INSTANCE;
	}
	private cParameters()
	{
		try (InputStream lInputStream = getClass().getResourceAsStream(MEDIAPLAYER_PROPERTIES))
		{
			Properties lProp = new Properties();
			if (lInputStream == null)
			{
				lLogger.error("property file '" + MEDIAPLAYER_PROPERTIES + "' not found in the classpath");
			}
			else
			{
				lProp.load(lInputStream);
				int i=0;
				while(lProp.containsKey("media.file."+i))
				{
					String lName = lProp.getProperty("media.file." +i++);
					if(lName!=null && !lName.isEmpty())
					{
						URL lUrl = getClass().getResource(lName);
						if(Files.exists(Paths.get(lUrl.toURI())))
                        {
	                        aMedia.add(lUrl);
                        }
					}
				}
				lLogger.info("Media: " + Joiner.on(";").join(aMedia));
				aMediaPlayerCmd = lProp.getProperty("mediaplayer.cmd");
				lLogger.info("Mediaplayer cmd : " + aMediaPlayerCmd );
				aMediaPlayerArgs = lProp.getProperty("mediaplayer.args").split(" ");
				lLogger.info("Mediaplayer args : " + Arrays.toString(aMediaPlayerArgs));
			}
		}
		catch (IOException | URISyntaxException e)
		{
			lLogger.error(e);
		}
	}

	public List<URL> mGetMedia()
	{
		return aMedia;
	}

	public List<String> mGetMediaPlayerCmdAndArgs()
	{
		List<String> lCmdAndArgs = new ArrayList<String>();
		lCmdAndArgs.add(aMediaPlayerCmd);
		for (String lS : aMediaPlayerArgs)
		{
			lCmdAndArgs.add(lS);
		}
		return lCmdAndArgs;
	}

}
