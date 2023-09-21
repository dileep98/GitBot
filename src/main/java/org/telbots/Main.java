package org.telbots;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
public class Main {
    public static void main(String[] args) {


        try {

//            Update update = new Update();
//            var msg = new Message();
////            var url = "https://github.com/harikesh409/resume-builder/tree/master/angular";
//            var url = "https://github.com/dileep98/GitBot/tree/master/src/main";
//            msg.setText(url);
//            update.setMessage(msg);
//            new MyBot().onUpdateReceived(update);


            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MyBot());
        } catch (TelegramApiException e) {
            log.error("Error from telegram api", e);
        }
    }
}