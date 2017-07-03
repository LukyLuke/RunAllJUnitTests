package ch.ranta.eclipse.plugin.runallunittests.listener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import ch.ranta.eclipse.plugin.runallunittests.Activator;
import ch.ranta.eclipse.plugin.runallunittests.resources.EclipseFile;


public class UpdateListener implements IPartListener, IResourceChangeListener {
	static final String FILE_EXTENSION = "java";
	
	public UpdateListener() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}
	
	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		// Only if there is changed something
		IResourceDelta delta = event.getDelta();
		if (delta == null) {
			return;
		}
		
		// Get the file form the Editor
		IEditorPart part = getOpenEditorPart();
		if (part instanceof ITextEditor) {
			IFile file = getEditorFile(part);
			
			// Update if there is a Delta in the Resource of the File
			IResourceDelta member = delta.findMember(file.getFullPath());
			if (member != null) {
				update((ITextEditor)part);
			}
		}
	}

	@Override
	public void partActivated(IWorkbenchPart part) {
		// Do Nothing
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {
		// Do Nothing
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
		// Do nothing
	}
	
	@Override
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof ITextEditor) {
			detach((ITextEditor)part);
		}
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof ITextEditor) {
			attach((ITextEditor)part);
		}
	}
	
	/**
	 * Update the annotation model if needed
	 */
	void update(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		if (provider == null) {
			return;
		}
		
		// Only Model-Extensions
		IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
		if (!(model instanceof IAnnotationModelExtension)) {
			return;
		}
		
		// Get the AnnotationModel and update
		IAnnotationModel annotationModel = ((IAnnotationModelExtension)model).getAnnotationModel(AnnotationModel.MODEL_KEY);
		if (annotationModel instanceof AnnotationModel) {
			((AnnotationModel)annotationModel).updateAnnotations();
		}
	}
	
	/**
	 * Removes the {@link AnnotationModel} which identified the current Editor and loaded Document
	 * @param editor
	 */
	void detach(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		if (provider == null) {
			return;
		}
		
		// Only Model-Extensions
		IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
		if (!(model instanceof IAnnotationModelExtension)) {
			return;
		}
		
		// Remove the AnnotatinModel
		((IAnnotationModelExtension)model).removeAnnotationModel(AnnotationModel.MODEL_KEY);
	}
	
	/**
	 * Add a new {@link AnnotationModel} to the cache if there is none for the current Editor and Document
	 * @param editor
	 */
	void attach(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		if (provider == null) {
			return;
		}
		
		// Only Model-Extensions
		IAnnotationModel model = provider.getAnnotationModel(editor.getEditorInput());
		if (!(model instanceof IAnnotationModelExtension)) {
			return;
		}
		
		IAnnotationModelExtension extension = (IAnnotationModelExtension) model;
    IAnnotationModel annotationModel = extension.getAnnotationModel(AnnotationModel.MODEL_KEY);
    
    // If there is no Annotation-Model for this editor and Document, create one and register it
    if (annotationModel == null) {
    	annotationModel = new AnnotationModel(editor);
    	extension.addAnnotationModel(AnnotationModel.MODEL_KEY, annotationModel);
    }
	}
	
	/**
	 * Returns the active Editor from the current Workbench
	 * 
	 * @return
	 */
	IEditorPart getOpenEditorPart() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();

		if (window != null) {
			IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				return page.getActiveEditor();
			}
		}

		return null;
  }
	
	/**
	 * If {@code part} is a {@link IEditorPart}, try to get the opened file and return it if it's a Java-File
	 * 
	 * @param part
	 * @return
	 */
	IFile getEditorFile(IWorkbenchPart part) {
		if (part instanceof IEditorPart) {
			IFile file = (IFile) ((IEditorPart)part).getEditorInput().getAdapter(IFile.class);
			if (FILE_EXTENSION.equals(file.getFileExtension())) {
				return file;
			}
		}
		return null;
	}
	
	/**
	 * Searches for TestFiles in all opened Projects and stores them internally
	 */
	public void findAndRegisterTestFiles() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject[] projects = root.getProjects(IResource.NONE);
		
		for (IProject project : projects) {
			IPath projectRoot = root.getLocation().append(project.getFullPath());
			Path path = Paths.get(projectRoot.toString());
			try {
				Files.find(path,
						Integer.MAX_VALUE,
						(filePath, fileAttr) -> (fileAttr.isRegularFile() && filePath.getFileName().toString().endsWith(FILE_EXTENSION)))
					.parallel()
					.forEach(f -> registerJavaTestFile(project, f));
				
			} catch (IOException e) {
				System.err.println(e);
			}
		}
	}
	
	/**
	 * Registers a Project-File for being cached to make the "Run-All JUnitTests" faster
	 * @param project
	 * @param path
	 */
	void registerJavaTestFile(IProject project, Path path) {
		try {
			String projectFile = path.toString().replace(project.getLocation().toString(), "");
			IFile sourceFile = project.getFile(projectFile);
			if (hasTestAnnotations(sourceFile)) {
				EclipseFile eclipseFile = new EclipseFile(sourceFile);
				Activator.getPlugin().registerTestFile(eclipseFile);
			}
		} catch (Exception e) {
			System.err.println(e);
		}
	}

	/**
	 * Check if the given File has at least one {@ Test}-Annotation
	 * @param file
	 * @return
	 * @throws Exception
	 */
	boolean hasTestAnnotations(IFile file) throws Exception {
		InputStreamReader reader = new InputStreamReader(file.getContents());
		BufferedReader buffer = new BufferedReader(reader);
		
		return buffer.lines()
				.filter(f -> (f.indexOf("@Test") >= 0 || f.indexOf("@org.junit.Test") >= 0))
				.findFirst()
				.isPresent();
	}
	
}
