package org.xpect.services;

import org.eclipse.xtext.conversion.IValueConverter;
import org.eclipse.xtext.conversion.ValueConverterException;
import org.eclipse.xtext.nodemodel.INode;

public class IdentityValueConverter implements IValueConverter<String> {

	public String toValue(String string, INode node) throws ValueConverterException {
		return string;
	}

	public String toString(String value) throws ValueConverterException {
		return value;
	}

}
