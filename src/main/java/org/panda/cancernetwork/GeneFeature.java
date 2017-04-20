package org.panda.cancernetwork;

import org.panda.utility.ArrayUtil;

/**
 * @author Ozgun Babur
 */
public class GeneFeature
{
	public static final String DEFAULT_BACKGROUND_COLOR = "255 255 255";
	public static final String DEFAULT_BORDER_COLOR = "0 0 0";
	public static final String DEFAULT_TOOLTIP = "";

	public GeneFeature(String letter, String tooltip, String bgColor, String borderColor)
	{
		this.letter = letter;
		this.tooltip = tooltip;
		this.bgColor = bgColor;
		this.borderColor = borderColor;

		if (tooltip == null) this.tooltip = DEFAULT_TOOLTIP;
		if (bgColor == null) this.bgColor = DEFAULT_BACKGROUND_COLOR;
		if (borderColor == null) this.borderColor = DEFAULT_BORDER_COLOR;
	}

	String letter;
	String bgColor;
	String borderColor;
	String tooltip;

	@Override
	public String toString()
	{
		return ArrayUtil.getString("|", tooltip, letter, bgColor, borderColor);
	}

	public String getDocText()
	{
		if (letter.equals("m"))
		{
			if (tooltip.toLowerCase().endsWith("mutation"))
			{
				return tooltip;
			}
			else
			{
				return tooltip + " Mutation";
			}
		}
		else if (tooltip.contains("-")) return "Copy number loss";
		else return "Copy number gain";
	}
}
