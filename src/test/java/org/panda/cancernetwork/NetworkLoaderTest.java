package org.panda.cancernetwork;

import junit.framework.Assert;
import org.biopax.paxtools.pattern.miner.SIFEnum;
import org.junit.Test;
import org.panda.utility.graph.Graph;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Ozgun Babur
 */
public class NetworkLoaderTest
{
	@Test
	public void testLoad() throws Exception
	{
		Map<SIFEnum, Integer> types = new HashMap<>();
		types.put(SIFEnum.CONTROLS_STATE_CHANGE_OF, 0);
		types.put(SIFEnum.CONTROLS_EXPRESSION_OF, 0);
		types.put(SIFEnum.IN_COMPLEX_WITH, 1);
		types.put(SIFEnum.INTERACTS_WITH, 1);
		types.put(SIFEnum.CATALYSIS_PRECEDES, 2);

		NetworkLoader nl = new NetworkLoader(types);
		List<Map<String, Graph>> list = nl.load();

		Assert.assertEquals(true, list.get(0).containsKey(SIFEnum.CONTROLS_STATE_CHANGE_OF.getTag()));
		Assert.assertEquals(true, list.get(0).containsKey(SIFEnum.CONTROLS_EXPRESSION_OF.getTag()));
		Assert.assertEquals(true, list.get(1).containsKey(SIFEnum.IN_COMPLEX_WITH.getTag()));
		Assert.assertEquals(true, list.get(1).containsKey(SIFEnum.INTERACTS_WITH.getTag()));
		Assert.assertEquals(true, list.get(2).containsKey(SIFEnum.CATALYSIS_PRECEDES.getTag()));
	}
}