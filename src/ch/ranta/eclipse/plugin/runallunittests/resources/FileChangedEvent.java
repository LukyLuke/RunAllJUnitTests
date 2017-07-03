package ch.ranta.eclipse.plugin.runallunittests.resources;

import ch.ranta.eclipse.plugin.runallunittests.listener.AnnotationModel;

public class FileChangedEvent {

	private final AnnotationModel instance;
	private final EclipseFile file;

	public FileChangedEvent(AnnotationModel instance, EclipseFile file) {
		this.instance = instance;
		this.file = file;
	}

	public EclipseFile getFile() {
		return file;
	}

	public AnnotationModel getInstance() {
		return instance;
	}
}
