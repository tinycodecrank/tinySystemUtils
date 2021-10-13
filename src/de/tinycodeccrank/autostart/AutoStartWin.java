package de.tinycodeccrank.autostart;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import de.tinycodeccrank.os.Platforms;
import de.tinycodecrank.monads.Opt;

public final class AutoStartWin extends AutoStart
{
	AutoStartWin(String programName, String launchFileName, File runningJar)
	{
		super(programName, new File(getAutoStartDir(), launchFileName + ".bat"), runningJar, "javaw");
	}
	
	AutoStartWin(String programName, File launchFile, File runningJar)
	{
		super(programName, launchFile, runningJar, "javaw");
	}
	
	@Override
	public boolean isEnabled()
	{
		return isPresent();
	}
	
	@Override
	public void write(Opt<String> vmArgs, Opt<String> programArgs) throws IOException
	{
		if (isPresent())
		{
			remove();
		}
		
		StringBuilder sb = new StringBuilder(javaPath);
		vmArgs.if_(args -> sb.append(" ").append(args));
		sb.append(" -jar ").append(runningJar.getAbsolutePath());
		programArgs.if_(args -> sb.append(" ").append(args));
		
		try (FileWriter fw = new FileWriter(launchFile))
		{
			fw.write(sb.toString());
		}
	}
	
	private static File getAutoStartDir()
	{
		return new File(Platforms.getAppDataPath(), "/Microsoft/Windows/Start Menu/Programs/Startup");
	}
}