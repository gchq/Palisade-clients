package uk.gov.gchq.palisade.client.java.job;

import uk.gov.gchq.palisade.client.java.download.DownloadTracker;

public interface Job {

    /**
     * <p>
     * This method will start the job. The following steps are performed:
     * <ul>
     * <li>Creates a new Download manager</li>
     * <li>Creates a new ResourceClient (web socket client)</li>
     * <li>Registers the download manager and the resource client with the provided
     * eventbus</li>
     * <li>Starts a new web socket container with the websocket client and the
     * provided url</li>
     * </ul>
     * </p>
     *
     * @return A {@code Publisher} instance that emits Downloads for consumption.
     *         Each emitted download instance will contain an InputStream
     */
    void start();

    /**
     * Returns the id of this job
     *
     * @return the id of this job
     */
    String getId();

    /**
     * Returns the Download tracker
     *
     * @return the Download tracker
     */
    DownloadTracker getDownLoadTracker();

}