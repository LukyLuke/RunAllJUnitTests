package ch.ranta.eclipse.plugin.runallunittests;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ch.ranta.eclipse.plugin.runallunittests.handler.JUnitLauncher;
import ch.ranta.eclipse.plugin.runallunittests.listener.UpdateListener;
import ch.ranta.eclipse.plugin.runallunittests.resources.EclipseFile;

public class Activator extends AbstractUIPlugin {
	private static BundleContext context;
	private static Activator plugin;
	
	private UpdateListener updateListener;
	private Set<EclipseFile> testFileCache = new HashSet<>();
	
	public Activator() {
		setInstance(this);
	}
	
	private static void setInstance(Activator instance) {
		plugin = instance;
	}

	public static BundleContext getContext() {
		return context;
	}
	
	public static Activator getPlugin() {
		return plugin;
	}

	/**
	 * This Method is invoked when the Plugin is loaded
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		context = bundleContext;
		updateListener = new UpdateListener();
	}

	/**
	 * Run after the Workbench is started to register the UpdateListener and search for all TestFiles
	 */
	void earlyStartup() {
		IPartService partService = getFirstPartServiceOrNull();
		if (partService != null) {
			partService.addPartListener(updateListener);
			updateListener.findAndRegisterTestFiles();
		}
	}
	
	/**
	 * Tries to get the first part service from the actual WorkSpace to register our "FileChanged" Listener
	 *  
	 * @return
	 */
	private IPartService getFirstPartServiceOrNull() {
    IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
    if(workbenchWindows.length > 0) {
    	return workbenchWindows[0].getPartService();
    }
    return null;
	}

	/**
	 * This Method is invoked when the Plugin is unloaded
	 * 
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		super.stop(bundleContext);
		
		IPartService partService = getFirstPartServiceOrNull();
		if (partService != null) {
			partService.removePartListener(updateListener);
		}
		
		context = null;
		plugin = null;
	}
	
	/**
	 * Insert or update a UnitTests File to the cached list
	 * @param file
	 */
	public void registerTestFile(EclipseFile file) {
		testFileCache.add(file);
	}

	/**
	 * Run all registered TestFiles as a JUnit Test
	 */
	public void runAllTests() {
		JUnitLauncher launcher = new JUnitLauncher();
		for (EclipseFile eclipseFile : testFileCache) {
			IClassFile classFile = eclipseFile.getTestClass();
			launcher.run(classFile);
			System.out.println(classFile.getPath().toString());
			System.out.println(eclipseFile.toString());
		}
	}
	
}
