package org.panda.cancernetwork;

import junit.framework.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Ozgun Babur
 */
public class GeneFeatureTest
{
	@Test
	public void testToString() throws Exception
	{
		String letter = "m";
		String tooltip = "V600E";
		String bgColor = "255 255 255";
		String borderColor = "0 0 0";

		GeneFeature gf = new GeneFeature(letter, tooltip, bgColor, borderColor);

		Assert.assertEquals(true, gf.toString().equals(tooltip + "|" + letter + "|" + bgColor + "|" + borderColor));
	}
}