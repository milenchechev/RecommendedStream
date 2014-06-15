package com.chechev.phd.evaluation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PrecisionEvaluationMetric implements EvaluationMetric {

	private static final String NAME = "Precision";

	@Override
	public double apply(List<String> feedIds,
			Collection<String> approvedItemsIds) {

		final List<String> feed = new LinkedList<String>(feedIds);

		if (feed.isEmpty()) {
			return 0.0;
		}

		final int allItemsCount = feed.size();
		feed.retainAll(approvedItemsIds);
		final int returnedApprovedItemsCount = feed.size();
		return (double) returnedApprovedItemsCount / allItemsCount;
	}

	@Override
	public String name() {
		return NAME;
	}
}
