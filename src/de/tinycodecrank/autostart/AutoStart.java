package de.tinycodecrank.autostart;

import java.io.File;
import java.io.IOException;

import de.tinycodecrank.monads.Opt;

public abstract class AutoStart
{
	protected final String	programName;
	protected final File	launchFile;
	protected final File	runningJar;
	protected String		javaPath;
	
	protected AutoStart(String programName, File launchFile, File runningJar, String javaPath)
	{
		this.programName	= programName;
		this.launchFile		= launchFile;
		this.runningJar		= runningJar;
		this.javaPath		= javaPath;
	}
	
	public final boolean isPresent()
	{
		return launchFile.exists();
	}
	
	public abstract boolean isEnabled();
	
	public abstract void write(Opt<String> vmArgs, Opt<String> programArgs) throws IOException;
	
	public final boolean remove()
	{
		if (isPresent())
		{
			return launchFile.delete();
		}
		return false;
	}
	
	protected final Opt<File> getRunningJar()
	{
		return Opt.empty();
	}
	
	public final void setJavaPath(String javaPath)
	{
		this.javaPath = javaPath;
	}
}