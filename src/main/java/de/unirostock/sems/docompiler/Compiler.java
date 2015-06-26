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
 * Hello world!
 *
 */
public class Compiler 
{
	
	public static final void die (String message)
	{
		System.err.println ("!!! " + message);
		System.exit (2);
	}
	
	
    public static void main( String[] args ) throws IOException, InterruptedException
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
  		
  		System.out.println (">>> extracting research object " + file + " to " + tmpDir);
  		try (Bundle bundle = Bundles.openBundle (file))
  		{
  			Manifest mf = bundle.getManifest ();
  			List<PathMetadata> aggr = mf.getAggregates ();
  			for (PathMetadata pm : aggr)
  			{
  				System.out.println ("  > " + pm.getFile ());
  				String cur = pm.getFile ().toString ();
  				Path target = tmpDir.resolve (cur.substring (1));
  				Files.createDirectories (target.getParent ());
  				Files.copy (pm.getFile (), target);
  			}
  		}
  		
  		String texFile = "document.tex";
  		if (texFile == null || !texFile.endsWith (".tex"))
  			die ("could not find valid tex file (" + texFile + ")");
  		
  		Path logFile = file.getParent ().resolve (texFile.substring (0, texFile.length () - 4) + ".outlog");
  		System.out.println (">>> compiler log will be available in " + logFile);
  		String pdfFile = texFile.substring (0, texFile.length () - 4)+ ".pdf";
  		
  		
  		// compile the tex code
  		ProcessBuilder pb = new ProcessBuilder (
  			"latexmk",
  			"-pdf",
  			"-f",
  			"-pdflatex=pdflatex -shell-escape -interaction=nonstopmode",
  			texFile).redirectErrorStream (true);
  		pb.directory (tmpDir.toFile ());
  		Process p = pb.start ();
  		
  		BufferedReader br = new BufferedReader (new InputStreamReader(p.getInputStream()));
  		BufferedWriter bw = new BufferedWriter (new FileWriter (logFile.toFile ()));
  		String line;
  		while ((line = br.readLine()) != null)
  		{
  			bw.write (line);
  			bw.newLine ();
  		}
  		br.close ();
  		bw.close ();
  		if (p.waitFor () == 0)
  			if (p.exitValue () == 0)
  			{
  				Path finalPdf = file.getParent ().resolve (pdfFile);
  				Files.copy (tmpDir.resolve (pdfFile), finalPdf);
  	  		System.out.println (">>> final pdf will be available in " + finalPdf);
  				System.out.println (">>> compilation done.");
  				System.exit (0);
  			}
  		die ("compiling document object failed");
    	
    }
}
