package de.tinycodecrank.io;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import de.tinycodecrank.collections.Stack;
import de.tinycodecrank.concurrent.ThreadLocalExecutorService;
import de.tinycodecrank.functions.bool_.BooleanBinaryOperator;
import de.tinycodecrank.monads.Opt;

public class FileSystemUtils
{
	private final boolean		parallel;
	private final LinkOption[]	options;
	
	private static final File[] EMPTY = {};
	
	public FileSystemUtils()
	{
		this.parallel	= false;
		this.options	= new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
	}
	
	public FileSystemUtils(boolean parallel, LinkOption... options)
	{
		this.parallel	= parallel;
		this.options	= options;
	}
	
	public FileSystemUtils parallel()
	{
		return new FileSystemUtils(true, this.options);
	}
	
	public FileSystemUtils serial()
	{
		return new FileSystemUtils(false, this.options);
	}
	
	public FileSystemUtils setLinkOptions(LinkOption... options)
	{
		return new FileSystemUtils(parallel, options);
	}
	
	private boolean isAccessibleDirectory(File directory)
	{
		return directory.isDirectory() && Files.isDirectory(directory.toPath(), this.options);
	}
	
	public boolean performRecursive(
		final File directory,
		final Predicate<File> fileAction,
		final BooleanBinaryOperator successAggregator)
	{
		class FolderVisitor
		{
			boolean	visited	= false;
			File	folder;
			
			FolderVisitor(File folder)
			{
				this.folder = folder;
			}
		}
		
		class Aggregate
		{
			volatile boolean success = !successAggregator.apply(true, false); // initializes with true for "And" and
																				// false for "Or" - for xOr this doesn't
																				// make sense!
			
			private void add(boolean success)
			{
				synchronized (this)
				{
					this.success = successAggregator.apply(this.success, success);
				}
			}
			
			private boolean getResult()
			{
				synchronized (this)
				{
					return this.success;
				}
			}
		}
		
		if (directory.exists())
		{
			final ExecutorService executor;
			if (this.parallel)
			{
				executor = Executors.newWorkStealingPool();
			}
			else
			{
				executor = new ThreadLocalExecutorService();
			}
			if (isAccessibleDirectory(directory))
			{
				Aggregate aggregate = new Aggregate();
				
				Stack<FolderVisitor> stack = new Stack<>();
				stack.push(new FolderVisitor(directory));
				while (!stack.isEmpty())
				{
					FolderVisitor last = stack.getLast();
					if (last.visited)
					{
						File tmp = stack.pop().folder;
						executor.execute(() -> aggregate.add(fileAction.test(tmp)));
					}
					else
					{
						File[] currentFiles = Opt.of(last.folder.listFiles()).get(() -> EMPTY);
						if (currentFiles.length > 0)
						{
							last.visited = true;
							for (File file : currentFiles)
							{
								if (isAccessibleDirectory(file))
								{
									stack.push(new FolderVisitor(file));
								}
								else
								{
									executor.execute(() -> aggregate.add(fileAction.test(file)));
								}
							}
						}
						else
						{
							File tmp = stack.pop().folder;
							executor.execute(() -> aggregate.add(fileAction.test(tmp)));
						}
					}
				}
				executor.shutdown();
				boolean working = true;
				while (working)
				{
					try
					{
						working = !executor.awaitTermination(100L, TimeUnit.MILLISECONDS);
					}
					catch (InterruptedException e)
					{}
				}
				return aggregate.getResult();
			}
			else
			{
				return fileAction.test(directory);
			}
		}
		else
		{
			return false;
		}
	}
	
	public boolean deleteRecursive(File directory)
	{
		return performRecursive(directory, File::delete, Boolean::logicalAnd);
	}
	
	public static boolean doesRootExist(File file)
	{
		String fileName = file.getAbsolutePath().replace("/", File.separator).replace("\\", File.separator);
		return Opt.of(File.listRoots())
			.map(
				roots -> Arrays.stream(roots)
					.map(File::getAbsolutePath)
					.anyMatch(fileName::startsWith))
			.get(() -> false);
	}
	
	/**
	 * @return The jar file the program is started from or empty if it was started
	 *         from within an IDE!
	 */
	public static Opt<File> getProgramMainJar()
	{
		String javaClassPath = System.getProperty("java.class.path");
		if (javaClassPath.contains(":"))
		{
			return Opt.empty();
		}
		else
		{
			return Opt.of(new File(javaClassPath));
		}
	}
}