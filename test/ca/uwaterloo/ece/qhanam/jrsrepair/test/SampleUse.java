package ca.uwaterloo.ece.qhanam.jrsrepair.test;

import java.util.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.core.resources.ResourcesPlugin;


public class SampleUse {

	public static void main(String[] args) {
		/* TODO: We have two options: 
		 * 	1. Set the environment using Eclipse java project and IJavaProject. 
		 *  2. Set the environment using ASTParser.setEnvironment. */
	}

}
