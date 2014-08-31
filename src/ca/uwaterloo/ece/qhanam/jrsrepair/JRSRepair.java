package ca.uwaterloo.ece.qhanam.jrsrepair;

import java.util.List;
import java.util.LinkedList;
import java.util.Collection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import ca.uwaterloo.ece.qhanam.jrsrepair.mutation.*;

public class JRSRepair {
	
	File sourcePath;
	File faultyCoverageFile;
	File seedCoverageFile;
	
	/**
	 * Creates a JRSRepair object with the path to the source folder
	 * of the program we are mutating.
	 * @param sourcePath The path to the source folder of the program we are mutating.
	 */
	public JRSRepair(File sourcePath, File faultyCoverageFile, File seedCoverageFile){
		this.sourcePath = sourcePath;
		this.faultyCoverageFile = faultyCoverageFile;
		this.seedCoverageFile = seedCoverageFile;
	}
	
	/**
	 * Builds ASTs for all the source files.
	 */
	public void buildASTs() throws Exception{
		Collection<File> sourceFiles;
		String[] sourceFilesArray;
		
		/* If the buggy file is a directory, get all the java files in that directory. */
		if(this.sourcePath.isDirectory()){
			sourceFiles = FileUtils.listFiles(this.sourcePath, new SuffixFileFilter(".java"), TrueFileFilter.INSTANCE);
			for (File javaFile : sourceFiles){
				System.out.println(javaFile);
			}
		}
		/* The buggy file may also be a source code file. */
		else{
			sourceFiles = new LinkedList<File>();
			sourceFiles.add(this.sourcePath);
		}
		
		/* Create the String array. */
		sourceFilesArray = new String[sourceFiles.size()];
		int i = 0;
		for(File sourceFile : sourceFiles){
			sourceFilesArray[i] = sourceFile.getCanonicalPath();
			i++;
		}
		
		/* Create the map of file paths to file contents (Document) */
		HashMap<String, DocumentASTRewrite> sourceFileContents = JRSRepair.buildSourceDocumentMap(sourceFilesArray);
		
		/* Create the ASTParser with the source files to generate ASTs for, and set up the
		 * environment using ASTParser.setEnvironment.
		 */
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		
		/* setEnvironment(
		 * String[] classpathEntries,
		 * String[] sourcepathEntries, 
		 * String[] encodings,
		 * boolean includeRunningVMBootclasspath) */
		parser.setEnvironment(new String[] {}, sourceFilesArray, null, false);
		parser.setResolveBindings(false); // Throws an error when set to 'true' for some reason.
		
		/* Set up the AST handler. We need to create LineCoverage and Statements classes to store 
		 * and filter the statements from the ASTs. */
		LineCoverage faultyLineCoverage = new LineCoverage(this.faultyCoverageFile);
		LineCoverage seedLineCoverage = new LineCoverage(this.seedCoverageFile);
		Statements faultyStatements = new Statements();
		Statements seedStatements = new Statements();
		FileASTRequestor fileASTRequestor = new MutationASTRequestor(sourceFileContents, faultyLineCoverage, seedLineCoverage, faultyStatements, seedStatements);
		
		/* createASTs(
		 * String[] sourceFilePaths, 
		 * String[] encodings, - the source file encodings (e.g., "ASCII", "UTF8", "UTF-16"). Can be set to null if platform encoding is sufficient.
		 * String[] bindingKeys, 
		 * FileASTRequestor requestor, 
		 * IProgressMonitor monitor) */
		parser.createASTs(sourceFilesArray, null, new String[] {}, fileASTRequestor, null);
		
		/* TODO: Mutate the program. */
		for(int j = 0; j < 1; j++){
			Mutation mutation = new AdditionMutation(sourceFileContents, faultyStatements.getRandomStatement(), seedStatements.getRandomStatement());
			mutation.mutate();
			mutation.undo();
			mutation.mutate();
			mutation.undo();
		}
		
		/* TODO: Compile and execute the program. */
		
	}
	
	/**
	 * Builds a HashMap with Java file paths as keys and Java file text contents as values.
	 * @param sourceFilesArray
	 * @return A HashMap containing the text of the source Java files.
	 */
	private static HashMap<String, DocumentASTRewrite> buildSourceDocumentMap(String[] sourceFilesArray) throws Exception{
		HashMap<String, DocumentASTRewrite> map = new HashMap<String, DocumentASTRewrite>();
		for(String sourceFile : sourceFilesArray){
            byte[] encoded = Files.readAllBytes(Paths.get(sourceFile));
            IDocument contents = new Document(new String(encoded));
            DocumentASTRewrite docrw = new DocumentASTRewrite(contents, null);
            map.put(sourceFile, docrw);
		}
		return map;
	}
		
}
