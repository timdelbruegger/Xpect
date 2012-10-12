package org.eclipse.xpect.xtext.lib.setup;

import org.eclipse.xpect.setup.IGuiceModuleProvider;

public class FileCtx {
	private IGuiceModuleProvider moduleProvider;

	private ResourceSet resourceSet = null;

	private AssertingValidator validate;

	public AssertingValidator getValidate() {
		if (validate == null)
			validate = new AssertingValidator();
		return validate;
	}

	public void setValidate(AssertingValidator validate) {
		this.validate = validate;
	}

	public void add(ResourceSet resourceSet) {
		if (this.resourceSet != null)
			throw new IllegalStateException("Only one ResourceSet per test is supported.");
		this.resourceSet = resourceSet;
	}

	public IGuiceModuleProvider getModuleProvider() {
		return moduleProvider;
	}

	public ResourceSet getResourceSet() {
		return resourceSet;
	}

	public void setModuleProvider(IGuiceModuleProvider moduleProvider) {
		this.moduleProvider = moduleProvider;
	}
}