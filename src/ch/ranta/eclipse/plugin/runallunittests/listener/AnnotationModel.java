package ch.ranta.eclipse.plugin.runallunittests.listener;

import java.util.Collections;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.ui.texteditor.ITextEditor;

import ch.ranta.eclipse.plugin.runallunittests.Activator;
import ch.ranta.eclipse.plugin.runallunittests.resources.EclipseFile;
import ch.ranta.eclipse.plugin.runallunittests.resources.FileChangedEvent;

public class AnnotationModel implements IAnnotationModel {
  static final String MODEL_KEY = "ch.ranta.eclipse.plugin.runallunittests.model_key";

  private final ITextEditor editor;
  
	public AnnotationModel(ITextEditor editor) {
		this.editor = editor;
	}

	/**
	 * Caches all not yet cached JUnit files to run them faster with one click
	 */
	public void updateAnnotations() {
		final AnnotationModel instance = this;
		Job updateJob = new Job("Update internal RunAll-JUnit Cache") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				EclipseFile file = new EclipseFile(editor);
				
				// Only process Test-Classes
				if (!file.isTestClassFile()) {
					return Status.OK_STATUS;
				}
				
				FileChangedEvent event = new FileChangedEvent(instance, file);
				instance.fireFileChangedEvent(event);
				
				return Status.OK_STATUS;
			}
		};
		
		updateJob.setPriority(Job.DECORATE);
    updateJob.schedule();
	}
	
	/**
	 * Register the File from the Event in {@link Activator}
	 * @param event
	 */
	void fireFileChangedEvent(FileChangedEvent event) {
		Activator.getPlugin().registerTestFile(event.getFile());
	}
	
	@Override
	public void addAnnotationModelListener(IAnnotationModelListener listener) {
		// Nothing to do here
	}

	@Override
	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
		// Nothing to do here
	}

	@Override
	public void connect(IDocument document) {
		// Nothing to do here
	}

	@Override
	public void disconnect(IDocument document) {
		// Nothing to do here

	}

	@Override
	public void addAnnotation(Annotation annotation, Position position) {
		// Nothing to do here

	}

	@Override
	public void removeAnnotation(Annotation annotation) {
		// Nothing to do here

	}

	@Override
	public Iterator<Annotation> getAnnotationIterator() {
		return Collections.emptyIterator();
	}

	@Override
	public Position getPosition(Annotation annotation) {
		return null;
	}

}
