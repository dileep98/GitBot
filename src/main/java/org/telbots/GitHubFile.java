package org.telbots;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class GitHubFile {
    private String path;
    private String url;
}