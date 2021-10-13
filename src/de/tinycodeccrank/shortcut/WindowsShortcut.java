package de.tinycodeccrank.shortcut;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.function.Consumer;

import de.tinycodecrank.monads.Opt;

public class WindowsShortcut
{
	private File		lnkFile;
	public String		targetPath;
	public Opt<String>	iconLocation	= Opt.empty();
	
	public WindowsShortcut(File lnkFile, String targetPath)
	{
		this.lnkFile	= lnkFile;
		this.targetPath	= targetPath;
	}
	
	public void setIconLocation(Opt<String> iconLocation)
	{
		this.iconLocation = iconLocation;
	}
	
	public boolean createShortcut(Consumer<InputStream> errorInputStream) throws IOException
	{
		return createShortcut(Opt.of(errorInputStream));
	}
	
	public boolean createShortcut() throws IOException
	{
		return createShortcut(Opt.empty());
	}
	
	private boolean createShortcut(Opt<Consumer<InputStream>> errorInputStream) throws IOException
	{
		String script = buildScript();
		
		ProcessBuilder pb = new ProcessBuilder("cmd");
		
		pb.redirectError(Redirect.PIPE);
		pb.redirectOutput(Redirect.PIPE);
		pb.redirectInput(Redirect.PIPE);
		
		Process process = pb.start();
		
		errorInputStream.if_(c -> c.accept(process.getErrorStream()));
		
		try (OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream()))
		{
			writer.write(script);
		}
		
		while (process.isAlive())
		{
			try
			{
				process.waitFor();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		return process.exitValue() == 0;
	}
	
	private String buildScript()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("powershell -Command $LnkFile = {\"")
			.append(lnkFile)
			.append("\"}; ");
		sb.append("$WScriptShell = New-Object -ComObject WScript.Shell; ");
		sb.append("$Lnk = $WScriptShell.CreateShortcut($LnkFile); ");
		sb.append("$Lnk.TargetPath = {\"")
			.append(targetPath)
			.append("\"}; ");
		iconLocation.if_(
			icon -> sb.append("$Lnk.IconLocation = {\"")
				.append(iconLocation)
				.append("\"}; "));
		sb.append("$Lnk.Save();\r\n");
		return sb.toString();
	}
}