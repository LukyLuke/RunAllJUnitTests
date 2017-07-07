package ch.ranta.eclipse.plugin.runallunittests.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.swt.widgets.Shell;

import ch.ranta.eclipse.plugin.runallunittests.Activator;

public class RunAllHandler {
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		Activator plugin = Activator.getPlugin();
		plugin.runAllTests();
	}
	
}
