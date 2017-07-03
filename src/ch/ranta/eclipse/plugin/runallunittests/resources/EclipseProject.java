package ch.ranta.eclipse.plugin.runallunittests.resources;

import org.eclipse.core.resources.IProject;

public class EclipseProject {
	private final IProject project;
	
	public EclipseProject(IProject project) {
		this.project = project;
	}
	
	public boolean exists() {
		return project.exists() && project.isOpen();
	}
	
	public String getName() {
		return project.getName();
	}
	
	public String getPath() {
		return project.getProjectRelativePath().toString();
	}
}
