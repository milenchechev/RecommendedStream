package com.chechev.phd.evaluation;

import com.chechev.phd.utils.GlobalConstants;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class MetricsWriter {

	/**
	 * {@value}
	 */
	private static final String TAG = MetricsWriter.class.getSimpleName();



	public void write(final String value) {
		try {
			final FileWriter writer = new FileWriter(GlobalConstants.FILE_PATH_RESULTS, true);
			writer.write(value);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			Logger.getLogger(TAG).severe(e.getMessage());
		}
	}
}
