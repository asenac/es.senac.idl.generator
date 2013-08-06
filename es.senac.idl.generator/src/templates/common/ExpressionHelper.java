package templates.common;

import java.math.BigInteger;
import java.text.DecimalFormatSymbols;

/**
 * Helping class for converting decimal string numbers to integer string numbers
 *
 */
public class ExpressionHelper {
	private static final String exponentSeparator = DecimalFormatSymbols.getInstance().getExponentSeparator();
	private static final String decimalSeparator = "\\."; // "\\" + DecimalFormatSymbols.getInstance().getDecimalSeparator();

	public static String valueAsInteger(String value) {
		if (!value.contains(exponentSeparator))
			return value.split(decimalSeparator)[0];
		else
		{	// Interpret <n>E[-]<exp> format
			Integer i = (new Double(value)).intValue();
			return i.toString();
		} 
	}
	
	public static String valueAsLong(String value) {
		if (!value.contains(exponentSeparator))
			return value.split(decimalSeparator)[0];
		else
		{	// Interpret <n>E[-]<exp> format
			Long l = (new Double(value)).longValue();
			return l.toString();
		} 
	}
	
	public static String valueAsBigInteger(String value) {
		if (!value.contains(exponentSeparator))
			return value.split(decimalSeparator)[0];
		else
		{	// Interpret <n>E[-]<exp> format
			String v[] = value.split(exponentSeparator);
			BigInteger bi = new BigInteger(v[0]);
			Long exp = new Long(v[1]);
			bi.multiply(new BigInteger(new Double(Math.pow(10, exp)).toString()));
			return bi.toString();
		} 
	}
}
