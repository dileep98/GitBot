package org.telbots;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.HttpEntity;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class MyBot extends TelegramLongPollingBot {

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

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
        String dir = urlParts[7];


        System.out.println(user+"="+repository+"="+ref+"="+dir);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            System.out.println(user+"="+repository+"="+ref+"=="+dir);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                System.out.println(user+"="+repository+"="+ref+"==="+dir);
                if (entity != null) {
                    InputStream zipStream = entity.getContent();
                    System.out.println(user+"="+repository+"="+ref+"===="+dir);
                    List<GitHubFile> files = parseZipContents(zipStream, user, repository, ref,dir);

                    System.out.println(files);
                    if (!files.isEmpty()) {
                        String zipFileName = "github_files.zip";
                        byte[] zipFileContent = downloadAndZipFiles(files);

                        sendDocument(chatId, zipFileContent, zipFileName);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<GitHubFile> parseZipContents(InputStream zipStream, String user, String repository, String ref, String dir) throws IOException {
        List<GitHubFile> files = new ArrayList<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(zipStream)) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String fileName = entry.getName();
                    System.out.println(fileName);
                    String url = String.format("https://raw.githubusercontent.com/%s/%s/%s/%s",
                            user, repository, ref, dir, fileName);
                    files.add(new GitHubFile(fileName, url));
                }
            }
        }

        return files;
    }

    private byte[] downloadAndZipFiles(List<GitHubFile> files) throws IOException {
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
            ByteArrayInputStream inputStream = new ByteArrayInputStream(documentContent);

            SendDocument document = new SendDocument();
                    document.setChatId(chatId);
                    document.setDocument(new InputFile(inputStream, fileName));

            execute(document);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotUsername() {
        return "git_folder_bot";
    }

    @Override
    public String getBotToken() {
        return "6559941276:AAEXfjqXu2N-g5ZZon5H3J32sJV3svBIwv4";
    }


}
