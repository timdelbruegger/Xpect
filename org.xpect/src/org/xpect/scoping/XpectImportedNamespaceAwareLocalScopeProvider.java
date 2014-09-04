package org.xpect.scoping;

import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.common.types.JvmDeclaredType;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.ImportNormalizer;
import org.eclipse.xtext.scoping.impl.ImportedNamespaceAwareLocalScopeProvider;
import org.xpect.Assignment;
import org.xpect.XpectPackage;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class XpectImportedNamespaceAwareLocalScopeProvider extends ImportedNamespaceAwareLocalScopeProvider {
	protected static class IsAssigneableTo implements Predicate<IEObjectDescription> {

		private Set<JvmType> invalid = Sets.newHashSet();

		private Set<JvmType> validTypes;

		public IsAssigneableTo(Set<JvmType> validTypes) {
			super();
			this.validTypes = validTypes;
		}

		private boolean apply(EObject obj) {
			if (obj instanceof JvmTypeReference)
				obj = ((JvmTypeReference) obj).getType();
			if (obj instanceof JvmDeclaredType) {
				JvmDeclaredType type = (JvmDeclaredType) obj;
				if (invalid.contains(type))
					return false;
				if (validTypes.contains(type))
					return true;
				for (JvmTypeReference sup : type.getSuperTypes())
					if (apply(sup))
						return true;
				invalid.add(type);
			}
			return false;
		}

		public boolean apply(IEObjectDescription arg0) {
			return apply(EcoreUtil.resolve(arg0.getEObjectOrProxy(), validTypes.iterator().next()));

		}

	}

	@Inject
	private AssignmentUtil assignmentUtil;

	protected Predicate<IEObjectDescription> getFilter(EObject context, EReference reference) {
		if (reference == XpectPackage.Literals.COMPONENT__COMPONENT_CLASS) {
			Assignment assignment = EcoreUtil2.getContainerOfType(context, Assignment.class);
			if (assignment != null && !assignment.eIsProxy()) {
				Set<JvmType> validTypes = assignmentUtil.getValidTypes(assignment);
				if (validTypes != null && !validTypes.isEmpty())
					return new IsAssigneableTo(validTypes);
			}
		}
		return null;
	}

	public IScope getScope(EObject context, EReference reference) {
		if (context == null)
			throw new NullPointerException("context");
		Predicate<IEObjectDescription> filter = getFilter(context, reference);
		IScope globalScope = getGlobalScope(context.eResource(), reference, filter);
		List<ImportNormalizer> normalizers = getImplicitImports(isIgnoreCase(reference));
		if (!normalizers.isEmpty()) {
			globalScope = createImportScope(globalScope, normalizers, null, reference.getEReferenceType(), isIgnoreCase(reference));
		}
		return getResourceScope(globalScope, context, reference);
	}

}
