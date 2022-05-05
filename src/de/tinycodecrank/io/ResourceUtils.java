package de.tinycodecrank.io;

import java.util.Scanner;

public class ResourceUtils
{
	private static final String UTF8 = "UTF-8";
	
	/**
	 * @param relative
	 *            The class that determines the location of the resource's path when
	 *            using jar files.
	 * @param path
	 *            The path of the resource.
	 * @return The contents of the resource as a String assuming the resource's
	 *         encoding is UTF-8.
	 */
	public static String resourceAsString(Class<?> relative, String path)
	{
		return resourceAsString(relative.getClassLoader(), path);
	}
	
	/**
	 * @param loader
	 *            The loader that determines the location of the resource's path
	 *            when using jar files.
	 * @param path
	 *            The path of the resource.
	 * @return The contents of the resource as a String assuming the resource's
	 *         encoding is UTF-8.
	 */
	public static String resourceAsString(ClassLoader loader, String path)
	{
		return resourceAsString(loader, path, UTF8);
	}
	
	/**
	 * @param relative
	 *            The class that determines the location of the resources's path
	 *            when using jar files.
	 * @param path
	 *            The path of the resource.
	 * @param charSetName
	 *            The resource's encoding.
	 * @return The contents of the resource as a String assuming the resource's
	 *         encoding is the provided one.
	 */
	public static String resourceAsString(Class<?> relative, String path, String charSetName)
	{
		return resourceAsString(relative.getClassLoader(), path, charSetName);
	}
	
	/**
	 * @param loader
	 *            The loader that determines the location of the resource's path
	 *            when using jar files.
	 * @param path
	 *            The path of the resource.
	 * @param charsetName
	 *            The resource's encoding.
	 * @return The contents of the resource as a String assuming the resource's
	 *         encoding is the provided one.
	 */
	public static String resourceAsString(ClassLoader loader, String path, String charsetName)
	{
		try (Scanner sc = new Scanner(loader.getResourceAsStream(path), charsetName))
		{
			return sc.useDelimiter("\\A").next();
		}
	}
}