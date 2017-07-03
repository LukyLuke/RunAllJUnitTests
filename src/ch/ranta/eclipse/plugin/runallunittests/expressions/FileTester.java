package ch.ranta.eclipse.plugin.runallunittests.expressions;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFile;

import ch.ranta.eclipse.plugin.runallunittests.resources.EclipseFile;

public class FileTester extends PropertyTester {
	private static final String HAS_DEFAULT_SUPPORT = "hasDefaultSupport";

	public FileTester() {
		super();
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (!(receiver instanceof IFile)) {
        return false;
    }

    if (HAS_DEFAULT_SUPPORT.equals(property)) {
        EclipseFile file = new EclipseFile((IFile) receiver);
        return "false".equals(expectedValue) || file.hasDefaultSupport();
    }

    return false;
	}

}
