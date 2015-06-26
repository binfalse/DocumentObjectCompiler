package de.unirostock.sems.docompiler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.taverna.robundle.Bundle;
import org.apache.taverna.robundle.Bundles;
import org.apache.taverna.robundle.manifest.Manifest;
import org.apache.taverna.robundle.manifest.PathMetadata;



/**
 * Compiler for Document Objects.
 * 
 * This compiler takes a <a href="http://www.researchobject.org/">Research Object</a> and compiles it into a PDF document.
 * 
 * @author Martin Scharm
 */
public class Compiler
{
	
	/**
	 * Die. Stop the execution delivering a last message.
	 *
	 * @param message the last message
	 */
	public static final void die (String message)
	{
		System.err.println ("!!! " + message);
		System.exit (2);
	}
	
	/**
	 * Extract the files of a research object to a directory on the disk.
	 *
	 * @param bundle the research object
	 * @param targetDir the directory to write to
	 * @throws IOException the IO exception
	 */
	public static final void extractResearchObject (Bundle bundle, Path targetDir) throws IOException
	{
			Manifest mf = bundle.getManifest ();
			List<PathMetadata> aggr = mf.getAggregates ();
			for (PathMetadata pm : aggr)
			{
				if (pm.getFile () == null)
					continue;
				System.out.println ("  > " + pm.getFile ());
				String cur = pm.getFile ().toString ();
				Path target = targetDir.resolve (cur.substring (1));
				Files.createDirectories (target.getParent ());
				Files.copy (pm.getFile (), target);
			}
	}
	
	
	/**
	 * Compile the latex project to a pdf file.
	 *
	 * @param sourceDirectory the directory containing the latex project
	 * @param texFile the main latex file
	 * @param logFile the log file
	 * @return true, if compile latex
	 * @throws IOException the IO exception
	 * @throws InterruptedException the interrupted exception
	 */
	public static final boolean compileLatex (Path sourceDirectory, String texFile, Path logFile) throws IOException, InterruptedException
	{
		// create a latexmk process
		// latexmk will run the pdflatex command multiple times to make sure all refs are correctly resolved
		ProcessBuilder pb = new ProcessBuilder (
			"latexmk",
			"-pdf",
			"-f",
			"-pdflatex=pdflatex -shell-escape -interaction=nonstopmode",
			texFile)
			.redirectErrorStream (true)
			.directory (sourceDirectory.toFile ());
		Process p = pb.start ();
		
		// read output and write it to the log
		BufferedReader br = new BufferedReader (new InputStreamReader (
			p.getInputStream ()));
		BufferedWriter bw = new BufferedWriter (new FileWriter (logFile.toFile ()));
		String line;
		while ( (line = br.readLine ()) != null)
		{
			bw.write (line);
			bw.newLine ();
		}
		br.close ();
		bw.close ();
		// that's been a success if latexmk returns 0
		if (p.waitFor () == 0)
			return true;
		return false;
	}
	
	
	/**
	 * The main method.
	 *
	 * @param args the arguments: |args| = 1 && args[0] must be a file
	 * @throws IOException the IO exception
	 * @throws InterruptedException the interrupted exception
	 */
	public static void main (String[] args)
		throws IOException,
			InterruptedException
	{
		if (args.length != 1)
			die ("expect exactly one argument: the document object");
		
		// create temp directory to extract the archive
		Path tmpDir = Files.createTempDirectory ("documentObject");
		System.out.println (">>> tmp dir is: " + tmpDir);
		
		// read document object and extract it
		Path file = new File (args[0]).toPath ();
		if (!Files.exists (file))
			die ("file " + file + " does not exist");
		
		System.out.println (">>> extracting research object " + file + " to "
			+ tmpDir);
		try (Bundle bundle = Bundles.openBundleReadOnly (file))
		{
			extractResearchObject (bundle, tmpDir);
		}
		
		String texFile = "document.tex";
		if (texFile == null || !texFile.endsWith (".tex"))
			die ("could not find valid tex file (" + texFile + ")");
		
		Path logFile = file.getParent ().resolve (
			texFile.substring (0, texFile.length () - 4) + ".outlog");
		System.out.println (">>> compiler log will be available in " + logFile);
		String pdfFile = texFile.substring (0, texFile.length () - 4) + ".pdf";
		
		// compile the tex code
		if (compileLatex (tmpDir, texFile, logFile))
		{
			Path finalPdf = file.getParent ().resolve (pdfFile);
			Files.copy (tmpDir.resolve (pdfFile), finalPdf);
			System.out.println (">>> final pdf will be available in " + finalPdf);
			System.out.println (">>> compilation done.");
			//System.exit (0);
		}
		else
			die ("compiling document object failed");
		
	}
}
