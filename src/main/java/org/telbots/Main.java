package org.telbots;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {


        try {
//            Update update = new Update();
//            var msg = new Message();
//            var url = "https://github.com/dileep98/GitBot/tree/master/src/main";
//            msg.setText(url);
//            update.setMessage(msg);
//            new MyBot().onUpdateReceived(update);
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new MyBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


        System.out.println("Hello world!");
    }
}