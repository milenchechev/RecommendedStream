package com.chechev.phd.evaluation;

import java.util.Collection;
import java.util.List;

public interface EvaluationMetric {

	double apply(final List<String> feedIds,
			final Collection<String> approvedItemsIds);

	String name();
}
