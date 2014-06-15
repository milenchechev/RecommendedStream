package com.chechev.phd.evaluation;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FeedEvaluator {

	private static final ScheduledExecutorService executorService = Executors
			.newSingleThreadScheduledExecutor();

	public static void scheduleEvaluation() {
		executorService.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				evaluateAndSave();
			}
		}, 30, 12 * 60, TimeUnit.MINUTES);
	}

	public static void stopEvaluation() {
		executorService.shutdownNow();
	}

	private static void evaluateAndSave() {
		final EvaluationMetric precision = new PrecisionEvaluationMetric();
		final EvaluationMetric normalizedDiscountedCumulativeGain = new NormalizedDiscountedCumulativeGainMetric();
		final MongoRetriever mongoRetriever = new MongoRetriever();
		final MetricsWriter writer = new MetricsWriter();

		final Map<String, String> users = mongoRetriever.getUsersInfo();

		for (final Map.Entry<String, String> entry : users.entrySet()) {
			final String userId = entry.getKey();
			final List<LinkedList<String>> feeds = mongoRetriever
					.getDisplayedFeeds(userId);
			final Set<String> approvedIds = mongoRetriever
					.getUserApprovedItemIds(userId);

			computeAndWriteMetric(precision, userId, feeds, approvedIds, writer);
			computeAndWriteMetric(normalizedDiscountedCumulativeGain, userId,
					feeds, approvedIds, writer);
		}
	}

	private static void computeAndWriteMetric(final EvaluationMetric metric,
			final String userId, final List<LinkedList<String>> feeds,
			final Set<String> approvedIds, final MetricsWriter writer) {
		double total = 0.0;
		final StringBuilder builder = new StringBuilder();

		builder.append(metric.name());
		builder.append(" for user ");
		builder.append(userId);
		builder.append(" computed at ");
		builder.append(Calendar.getInstance().getTime());
		builder.append(" is ");

		if (feeds.isEmpty()) {
			builder.append("0.0");
			builder.append(" ");
		}

		for (final LinkedList<String> feedIds : feeds) {
			final double precisionRate = metric.apply(feedIds, approvedIds);
			total += precisionRate;
			builder.append(precisionRate);
			builder.append(" ");
		}

		builder.append("and average is ");
		builder.append(feeds.isEmpty() ? 0 : total / feeds.size());
		builder.append(System.getProperty("line.separator"));

		writer.write(builder.toString());
	}
}
