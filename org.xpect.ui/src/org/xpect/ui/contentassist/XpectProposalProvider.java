/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.xpect.ui.contentassist;

import java.util.Set;

import org.eclipse.emf.ecore.EReference;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.CrossReference;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.common.types.xtext.ui.ITypesProposalProvider;
import org.eclipse.xtext.common.types.xtext.ui.ITypesProposalProvider.Filter;
import org.eclipse.xtext.common.types.xtext.ui.TypeMatchFilters;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor;
import org.xpect.Assignment;
import org.xpect.XpectJavaModelPackage;
import org.xpect.XpectPackage;
import org.xpect.scoping.AssignmentUtil;
import org.xpect.services.IdentityValueConverter;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.inject.Inject;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class XpectProposalProvider extends AbstractXpectProposalProvider {
	@Inject
	private ITypesProposalProvider typeProposalProvider;

	@Inject
	private IdentityValueConverter identityValueConverter;

	@Inject
	private AssignmentUtil assignmentUtil;

	protected void lookupCrossReference(CrossReference crossReference, EReference reference, ContentAssistContext context, ICompletionProposalAcceptor acceptor,
			Predicate<IEObjectDescription> filter) {
		String ruleName = null;
		if (crossReference.getTerminal() instanceof RuleCall) {
			ruleName = ((RuleCall) crossReference.getTerminal()).getRule().getName();
		}
		Function<IEObjectDescription, ICompletionProposal> proposalFactory = getProposalFactory(ruleName, context);
		// lookupCrossReference(contentAssistContext.getCurrentModel(), reference, acceptor, filter, proposalFactory);
		if (reference == XpectPackage.Literals.XPECT_TEST__DECLARED_SUITE)
			reference = XpectJavaModelPackage.Literals.XJM_CLASS__JVM_CLASS;
		if (reference == XpectPackage.Literals.COMPONENT__COMPONENT_CLASS) {
			Filter f = new TypeMatchFilters.All(IJavaSearchConstants.CLASS);
			Assignment assignment = EcoreUtil2.getContainerOfType(context.getCurrentModel(), Assignment.class);
			if (assignment != null) {
				Set<JvmType> types = assignmentUtil.getValidTypes(assignment);
				if (types != null) {
					for (JvmType type : types) {
						System.out.println(type.getQualifiedName());
						typeProposalProvider.createSubTypeProposals(type, this, context, reference, f, acceptor);
					}
				}
				return;
			}
			typeProposalProvider.createTypeProposals(this, context, reference, f, acceptor);
			return;
		}
		super.lookupCrossReference(context.getCurrentModel(), reference, acceptor, filter, proposalFactory);
	}

}
