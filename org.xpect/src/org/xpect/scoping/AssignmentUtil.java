package org.xpect.scoping;

import java.util.List;
import java.util.Set;

import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmFeature;
import org.eclipse.xtext.common.types.JvmOperation;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.xpect.AbstractComponent;
import org.xpect.Assignment;
import org.xpect.Component;
import org.xpect.XpectTest;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@SuppressWarnings("restriction")
public class AssignmentUtil {

	private void collectAdders(JvmType type, List<JvmOperation> ops) {
		if (type instanceof JvmDeclaredType)
			for (JvmFeature feat : ((JvmDeclaredType) type).getAllFeatures())
				if (feat instanceof JvmOperation && "add".equals(feat.getSimpleName()))
					ops.add((JvmOperation) feat);
	}

	public Set<JvmType> getValidTypes(Assignment assignment) {
		List<JvmOperation> ops = Lists.newArrayList();
		JvmOperation operation = assignment.getDeclaredTarget();
		if (operation != null) {
			if (operation.eIsProxy())
				return null;
			ops.add(operation);
		} else {
			AbstractComponent container = assignment.getInstance();
			if (container instanceof Component) {
				JvmDeclaredType type = ((Component) container).getComponentClass();
				if (type != null && !type.eIsProxy())
					collectAdders(type, ops);
			} else if (container instanceof XpectTest) {
				XtextResource resource = (XtextResource) assignment.eResource();
				IJvmTypeProvider typeProvider = resource.getResourceServiceProvider().get(IJvmTypeProvider.Factory.class).findOrCreateTypeProvider(resource.getResourceSet());
				collectAdders(typeProvider.findTypeByName("org.xpect.xtext.lib.setup.emf.ResourceSetDefaultsSetup"), ops);
				collectAdders(typeProvider.findTypeByName("org.xpect.xtext.lib.setup.workspace.WorkspaceDefaultsSetup"), ops);
			} else
				return null;
		}
		Set<JvmType> result = Sets.newHashSet();
		for (JvmOperation op : ops)
			if (op.getParameters().size() == 1) {
				JvmTypeReference paramType = op.getParameters().get(0).getParameterType();
				if (paramType != null && !paramType.eIsProxy() && paramType.getType() != null && !paramType.getType().eIsProxy())
					result.add(paramType.getType());
			}
		return result;
	}
}
