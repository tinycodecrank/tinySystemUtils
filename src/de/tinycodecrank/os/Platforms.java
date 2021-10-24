package de.tinycodecrank.os;

import static java.io.File.*;

import java.io.File;
import java.util.Locale;

public final class Platforms
{
	private Platforms()
	{}
	
	private static String appData = getAppDataForOs(OS.currentOS);
	
	private static final String getAppDataForOs(OS os)
	{
		switch (os)
		{
			case MAC_OS:
				return System.getProperty("user.home") + separator + "Library" + separator + "Application Support";
			case WINDOWS:
				return System.getenv("APPDATA");
			case LINUX:
				return System.getProperty("user.home");
			case OTHER:
			default:
				return System.getProperty("user.dir");
		}
	}
	
	public static OS getOS()
	{
		return OS.currentOS;
	}
	
	public static String getOsName()
	{
		return OS.osName;
	}
	
	public static String getAppDataPath()
	{
		return appData;
	}
	
	public static File getAppDataFile()
	{
		return new File(getAppDataPath());
	}
	
	public static File getDesktop()
	{
		switch (OS.currentOS)
		{
			case MAC_OS:
			case WINDOWS:
			case LINUX:
				return new File(System.getProperty("user.home") + separator + "Desktop");
			case OTHER:
			default:
				return new File(System.getProperty("user.dir") + separator + "Desktop");
		}
	}
	
	public static enum OS
	{
		WINDOWS,
		LINUX,
		MAC_OS,
		OTHER;
		
		private static final String	osName		= System.getProperty("os.name", "generic");
		private static final OS		currentOS	= getCurrent();
		
		private static final OS getCurrent()
		{
			String os = osName.toLowerCase(Locale.ENGLISH);
			if (os.contains("mac") || os.contains("darwin"))
			{
				return OS.MAC_OS;
			}
			else if (os.contains("win"))
			{
				return OS.WINDOWS;
			}
			else if (os.contains("nux"))
			{
				return OS.LINUX;
			}
			else
			{
				return OS.OTHER;
			}
		}
	}
}