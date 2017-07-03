package ch.ranta.eclipse.plugin.runallunittests;

import org.eclipse.ui.IStartup;

public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		Activator.getPlugin().earlyStartup();
	}
	
}
