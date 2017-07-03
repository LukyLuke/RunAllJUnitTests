package ch.ranta.eclipse.plugin.runallunittests.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.texteditor.ITextEditor;

public class EclipseFile {
	private static final String SUPPORTED_FILE_EXTENSION = "java";
	private final IFile file;
	private IClassFile testClass;
	
	/**
	 * Constructor based on a IFile
	 * @param file
	 */
	public EclipseFile(IFile file) {
		this.file = file;
	}
	
	/**
	 * Constructor based on a ITextEditor
	 * @param editor
	 */
	public EclipseFile(ITextEditor editor) {
		this.file = (IFile)(editor.getEditorInput().getAdapter(IFile.class));
	}

	/**
	 * Just true :)
	 * @return
	 */
	public boolean hasDefaultSupport() {
		return true;
	}
	
	/**
	 * Test if this file is supported or should be ignored
	 * @return
	 */
	public boolean isSupported() {
		return getExtension().equalsIgnoreCase(SUPPORTED_FILE_EXTENSION);
	}
	
	/**
	 * Project in which this file is
	 * @return
	 */
	public EclipseProject getProject() {
		return new EclipseProject(file.getProject());
	}

	/**
	 * Returns the File-Extension
	 * @return
	 */
	public String getExtension() {
		String extension = (file == null) ? null : file.getFileExtension();
		return extension == null ? "" : extension;
	}
	
	/**
	 * Returns the IFile-Resource
	 * @return
	 */
	public IFile getFile() {
		return file;
	}
	
	/**
	 * Returns the ClassFile from this SourceFile.
	 * @return null if it's not possible to create the ClassFile
	 */
	public IClassFile getTestClass() {
		if (testClass == null) {
			IPath path = file.getFullPath().removeFileExtension().addFileExtension("class");
			IFile classFile = file.getProject().getFile(path.removeFirstSegments(1)); // First Segment is the Project which is not needed
			testClass = JavaCore.createClassFileFrom(classFile);
		}
		return testClass;
	}
	
	/**
	 * Test if this is a JUnit Test-Class file
	 * @return
	 */
	public boolean isTestClassFile() {
		ICompilationUnit compilationUnit = getCompilationUnit();
		if (compilationUnit != null) {
			IType primaryType = compilationUnit.findPrimaryType();
			return hasJUnitTestAnnotations(primaryType);
		}
		return false;
	}
	
	/**
	 * Check if the Class has at least one JUnit Test Annotation
	 * @param primaryType
	 * @return
	 */
	boolean hasJUnitTestAnnotations(IType primaryType) {
		try {
			for (IMethod method : primaryType.getMethods()) {
				if (method.getAnnotation("Test").exists() || method.getAnnotation("org.junit.Test").exists()) {
					return true;
				}
			}
		} catch (Exception e) {
			// Nothing
		}
		
		return false;
	}

	/**
	 * Tries to build a CompilationUnit and return it - or null
	 * @return
	 */
	ICompilationUnit getCompilationUnit() {
		return isSupported() ? JavaCore.createCompilationUnitFrom(file) : null;
	}
	
	@Override
	public String toString() {
		return "[" + getProject().getName() + "] " + file.getFullPath().toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof EclipseFile) {
			return ((EclipseFile)obj).getFile().equals(file);
		}
		return super.equals(obj);
	}
	
}
