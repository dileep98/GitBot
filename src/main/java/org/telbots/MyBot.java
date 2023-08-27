package org.telbots;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
public class MyBot extends TelegramLongPollingBot {

    public MyBot() {
        super("6559941276:AAHUONO2_xG0Ofd6tddqWFw4ry6YEzwoJCo");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
//            long chatId = 1L;
            // Assuming the user sends a message with a GitHub URL
            if (messageText.startsWith("https://github.com/")) {
                sendGitHubFilesAsZip(chatId, messageText);
            }
        }
    }

    private void sendGitHubFilesAsZip(long chatId, String url) {
        String[] urlParts = url.split("/");
        String user = urlParts[3];
        String repository = urlParts[4];
        String ref = urlParts[6];
        StringBuilder sb = new StringBuilder();
        for (int i = 7; i < urlParts.length; i++) {
            sb.append(urlParts[i]);
            sb.append(File.separatorChar);
        }
        String dir = sb.toString();

        log.info("User: {}, Repo: {}, Ref: {}, Dir: {}", user, repository, ref, dir);

        var zipUrl = String.format("https://api.github.com/repos/%s/%s/zipball/%s", user, repository, ref);
        log.info("Input URL: {} ZipUrl: {}", url, zipUrl);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(zipUrl);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                log.info("Http response status: {}", response.getStatusLine());
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream zipStream = entity.getContent();
                    List<GitHubFile> files = parseZipContents(zipStream, user, repository, ref, dir);

                    log.debug("Files: {}", files);
                    if (!files.isEmpty()) {
                        String zipFileName = "github_files.zip";
                        byte[] zipFileContent = downloadAndZipFiles(files);

                        sendDocument(chatId, zipFileContent, zipFileName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception in sendGitHubFilesAsZip", e);
        }
    }

    private List<GitHubFile> parseZipContents(InputStream zipStream, String user, String repository, String ref, String dir) throws IOException {
        log.info("Parsing zip contents");
        List<GitHubFile> files = new ArrayList<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                String fileName = entry.getName();
                if (!entry.isDirectory() && fileName.contains(dir)) {
                    fileName = fileName.substring(fileName.indexOf(File.separatorChar));
                    String url = String.format("https://raw.githubusercontent.com/%s/%s/%s%s",
                            user, repository, ref, fileName);
                    files.add(new GitHubFile(fileName, url));
                }
            }
        }

        return files;
    }

    private byte[] downloadAndZipFiles(List<GitHubFile> files) throws IOException {
        log.info("Zipping {} files", files.size());
        ByteArrayOutputStream zipByteArrayOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(zipByteArrayOutputStream)) {
            byte[] buffer = new byte[4096];

            for (GitHubFile file : files) {
                try (InputStream inputStream = new URL(file.getUrl()).openStream()) {
                    String zipEntryPath = file.getPath();
                    ZipEntry zipEntry = new ZipEntry(zipEntryPath);
                    zipOutputStream.putNextEntry(zipEntry);

                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        zipOutputStream.write(buffer, 0, bytesRead);
                    }
                }
            }
        }

        return zipByteArrayOutputStream.toByteArray();
    }

    private void sendDocument(long chatId, byte[] documentContent, String fileName) {
        try {
            log.info("Sending document");
            ByteArrayInputStream inputStream = new ByteArrayInputStream(documentContent);

            SendDocument document = new SendDocument();
            document.setChatId(chatId);
            document.setDocument(new InputFile(inputStream, fileName));

            execute(document);
        } catch (TelegramApiException e) {
            log.error("Error while sending document", e);
        }
    }

    @Override
    public String getBotUsername() {
        return "git_folder_bot";
    }

}
