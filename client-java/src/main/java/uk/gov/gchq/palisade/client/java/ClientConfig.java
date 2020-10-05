package uk.gov.gchq.palisade.client.java;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("palisade.client")
public class ClientConfig {

    private String url = "http://localhost:8081";
    private Download download = new Download();

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Download getDownload() {
        return this.download;
    }

    public void setDownload(Download download) {
        this.download = download;
    }

    @ConfigurationProperties("download")
    public static class Download {

        private int threads = 1;

        public int getThreads() {
            return this.threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

    }
}