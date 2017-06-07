package org.panda.cancernetwork;

import org.panda.utility.ArrayUtil;

/**
 * A gene feature is something that needs to be represented using small circles (info box) over the gene nodes.
 *
 * @author Ozgun Babur
 */
public class GeneFeature
{
	// define default values
	public static final String DEFAULT_BACKGROUND_COLOR = "255 255 255";
	public static final String DEFAULT_BORDER_COLOR = "0 0 0";
	public static final String DEFAULT_TOOLTIP = "";

	/**
	 * The letter displayed in info box.
	 */
	String letter;

	/**
	 * The tooltip text that is displayed on mouse over.
	 */
	String bgColor;

	/**
	 * Background color of the info box.
	 */
	String borderColor;

	/**
	 * The border color of the info box.
	 */
	String tooltip;

	/**
	 * Constructor with all necessary information
	 * @param letter the letter displayed in info box
	 * @param tooltip the tooltip text that is displayed on mouse over
	 * @param bgColor background color of the info box
	 * @param borderColor the border color of the info box
	 */
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

	@Override
	public String toString()
	{
		return ArrayUtil.getString("|", tooltip, letter, bgColor, borderColor);
	}
}
