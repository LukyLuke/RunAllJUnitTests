package ch.ranta.eclipse.plugin.runallunittests.handler;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.junit.launcher.JUnitLaunchShortcut;

public class JUnitLauncher extends JUnitLaunchShortcut {
	
	/**
	 * Run the Test
	 * @param classFile
	 */
	public void run(IClassFile classFile) {
		try {
			// JUnit cannot be run on the IClassFile, we need an IType as the IJavaElement
			launch(classFile.getType());
		} catch (CoreException | InterruptedException e) {
			System.err.println(e);
		}
	}
	
	/**
	 * Launch a Test in background mode with a temporary configuration
	 * @param element
	 * @throws InterruptedException
	 * @throws CoreException
	 */
	protected void launch(IJavaElement element) throws InterruptedException, CoreException {
		ILaunchConfigurationWorkingCopy temporary = createLaunchConfiguration(element);
		temporary.setAttribute(IDebugUIConstants.ATTR_LAUNCH_IN_BACKGROUND, true);
		
		DebugUITools.launch(temporary, ILaunchManager.RUN_MODE);
	}
	
	/**
	 * Searching for already existing Run-Configs and return the first matching one
	 * @param template
	 * @return
	 * @throws CoreException
	 */
	protected ILaunchConfiguration findExistingLaunchConfiguration(ILaunchConfigurationWorkingCopy template) throws CoreException {
      List<ILaunchConfiguration> candidates = findExistingLaunchConfigurations(template);
      return candidates.isEmpty() ? null : candidates.get(0);
  }
	
	/**
	 * CopyPaste from {@link JUnitLaunchShortcut}} because it's "private" :(
	 * 
	 * @param temporary
	 * @return
	 * @throws CoreException
	 */
  protected List<ILaunchConfiguration> findExistingLaunchConfigurations(ILaunchConfigurationWorkingCopy temporary) throws CoreException {
      ILaunchConfigurationType configType = temporary.getType();
      ILaunchConfiguration[] configs = getLaunchManager().getLaunchConfigurations(configType);
      String[] attributeToCompare = getAttributeNamesToCompare();
      ArrayList<ILaunchConfiguration> candidateConfigs = new ArrayList<ILaunchConfiguration>(configs.length);
      
      for (ILaunchConfiguration config : configs) {
          if (hasSameAttributes(config, temporary, attributeToCompare)) {
              candidateConfigs.add(config);
          }
      }
      return candidateConfigs;
  }

  /**
   * CopyPaste from {@link JUnitLaunchShortcut}} because it's "private" :(
   * 
   * @param config1
   * @param config2
   * @param attributeToCompare
   * @return
   */
  protected boolean hasSameAttributes(ILaunchConfiguration config1, ILaunchConfiguration config2, String[] attributeToCompare) {
      try {
          for (String element : attributeToCompare) {
              String val1 = config1.getAttribute(element, "");
              String val2 = config2.getAttribute(element, "");
              if(!val1.equals(val2)) {
                  return false;
              }
          }
          return true;
      } catch (CoreException e) {
          // ignore access problems here, return false
      }
      return false;
  }
	
  /**
   * CopyPaste from {@link JUnitLaunchShortcut}} because it's "private" :(
   * 
   * @return
   */
  protected ILaunchManager getLaunchManager() {
      return DebugPlugin.getDefault().getLaunchManager();
  }
}
