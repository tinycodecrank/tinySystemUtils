package de.tinycodeccrank.autostart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import de.tinycodeccrank.os.Platforms;
import de.tinycodeccrank.shortcut.FreedesktopOrgShortcut;
import de.tinycodeccrank.shortcut.FreedesktopOrgShortcut.LocalizableArgument;
import de.tinycodecrank.monads.Opt;

public final class AutoStartGnome extends AutoStart
{
	private Opt<String> icon = Opt.empty();
	
	AutoStartGnome(String programName, String launchFileName, File runningJar)
	{
		super(programName, new File(getAutoStartDir(), launchFileName + ".desktop"), runningJar, "java");
	}
	
	AutoStartGnome(String programName, File launchFile, File runningJar)
	{
		super(programName, launchFile, runningJar, "java");
	}
	
	public void setIcon(String icon)
	{
		this.icon = Opt.of(icon);
	}
	
	@Override
	public boolean isEnabled()
	{
		if (isPresent())
		{
			try (Scanner sc = new Scanner(launchFile, "UTF8"))
			{
				while (sc.hasNextLine())
				{
					String line = sc.nextLine();
					if (line.startsWith("X-GNOME-Autostart-enabled"))
					{
						return Boolean.parseBoolean(line.substring(line.indexOf("=") + 1).trim());
					}
				}
			}
			catch (FileNotFoundException e)
			{
				e.printStackTrace();
			}
		}
		return false;
	}
	
	@Override
	public void write(Opt<String> vmArgs, Opt<String> programArgs) throws IOException
	{
		if (isPresent())
		{
			remove();
		}
		
		FreedesktopOrgShortcut shortcut = new FreedesktopOrgShortcut(launchFile);
		shortcut.type					= Opt.of("Application");
		shortcut.hidden					= Opt.of("false");
		shortcut.noDisplay				= Opt.of("false");
		shortcut.name					= Opt.of(new LocalizableArgument(programName));
		shortcut.icon					= this.icon;
		shortcut.xGnomeAutoStartEnabled	= Opt.of("true");
		
		StringBuilder sb = new StringBuilder(javaPath);
		vmArgs.if_(args -> sb.append(" ").append(args));
		sb.append(" -jar ").append(runningJar.getAbsolutePath());
		programArgs.if_(args -> sb.append(" ").append(args));
		shortcut.exec = Opt.of(sb.toString());
		shortcut.createShortcut();
	}
	
	private static File getAutoStartDir()
	{
		return new File(Platforms.getAppDataPath(), "/.config/autostart/");
	}
}