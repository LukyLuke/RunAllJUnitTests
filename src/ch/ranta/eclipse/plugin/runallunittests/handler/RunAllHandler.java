package ch.ranta.eclipse.plugin.runallunittests.handler;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

import ch.ranta.eclipse.plugin.runallunittests.Activator;

public class RunAllHandler {
	
	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		String title = "E4 Information Dialog";
		String message = "Hello world from a pure Eclipse 4 plug-in";
		
		Activator plugin = Activator.getPlugin();
		plugin.runAllTests();
		
		// org.eclipse.ui.edit.text.actionSet.annotationNavigation
		// org.eclipse.jdt.junit.jJUiitActionSet
		
		showDialog(shell, title, message);
	}

	void showDialog(Shell s, String title, String message) {
		MessageDialog.openInformation(s, title, message);
	}

	
	
}
