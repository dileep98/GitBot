package org.telbots;

class GitHubFile {
    private String path;
    private String url;

    public GitHubFile(String path, String url) {
        this.path = path;
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public String getUrl() {
        return url;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "GitHubFile{" + "path='" + path + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}