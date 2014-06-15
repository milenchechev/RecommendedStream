package com.chechev.phd.evaluation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class NormalizedDiscountedCumulativeGainMetric implements
		EvaluationMetric {

	private static final String NAME = "Normalized Discounted Cumulative Gain";

	@Override
	public double apply(List<String> feedItems, Collection<String> approvedItems) {
		final double discountedGain = getDiscountedCumulativeGain(feedItems,
				approvedItems);
		final double idealDiscountedGain = getIdealDiscountedCumulativeGain(
				feedItems, approvedItems);

		if (idealDiscountedGain == 0) {
			return 0;
		}

		return discountedGain / idealDiscountedGain;
	}

	private double getDiscountedCumulativeGain(List<String> feedItems,
			Collection<String> approvedItems) {
		double result = 0.0;
		for (int i = 0; i < feedItems.size(); ++i) {
			final String feedItem = feedItems.get(i);
			int itemRelevance = getItemRelevance(feedItem, approvedItems);
			if (i == 0) {
				result += itemRelevance;
			} else {
				result += itemRelevance / Math.log(i + 2);
			}
		}
		return result;
	}

	private double getIdealDiscountedCumulativeGain(
			final List<String> feedItems, final Collection<String> approvedItems) {
		final List<String> sortedFeed = new LinkedList<String>(feedItems);
		Collections.sort(sortedFeed, new Comparator<String>() {

			@Override
			public int compare(String first, String second) {
				return getItemRelevance(second, approvedItems)
						- getItemRelevance(first, approvedItems);
			}
		});
		return getDiscountedCumulativeGain(sortedFeed, approvedItems);
	}

	private int getItemRelevance(final String feedItem,
			final Collection<String> approvedItems) {
		return approvedItems.contains(feedItem) ? 1 : 0;
	}

	@Override
	public String name() {
		return NAME;
	}
}
