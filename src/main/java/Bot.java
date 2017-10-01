import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.awt.image.ImagingOpException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Bot extends TelegramLongPollingBot {

    // Uploads document:
    private void sendDocUploadingAFile(Long chatId, java.io.File save,String caption) throws TelegramApiException {

        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        sendDocumentRequest.setNewDocument(save);
        sendDocumentRequest.setCaption(caption);
        sendDocument(sendDocumentRequest);
    }

    private void handleFile(String filename, String record) {

        String fileContent = "";
        try {
            fileContent = Fileio.readfile(filename);
            fileContent = fileContent + record;
            Fileio.writefile(filename, fileContent);
        }
        catch(IOException e) {
            e.printStackTrace();
            try {
                Fileio.writefile(filename, record);
            }
            catch(IOException exc) {
                exc.printStackTrace();
            }
        }
    }

    public float getDaySum(String filename) {

        float day_total = 0;
        try {
            String content = Fileio.readfile(filename);
            String[] rows = content.split("\n");

            for(String s : rows)
            {
                // TODO: change column-take method:
                String[] cols = s.split(";");

                // TODO: remove hardcode 'cols.length == 4'
                // TODO: and cols[2]
                if(cols.length == 4) {
                    float value = UpdateParser.parsePrice(cols[2]);
                    if(value != -1) {
                        day_total += value;
                    }
                }
            }

            return day_total;
        }
        catch(IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private String getFileForUpload(String dateValue, long chat_id) {

        String response = "";
        File file = new File(dateValue + ".csv");
        if(file.exists()) {
            try {
                sendDocUploadingAFile(chat_id, file, "Статистика за " + dateValue + ":\n ");
            }
            catch(TelegramApiException e) { e.printStackTrace(); }
        }
        else {
            response = "На " + dateValue + " файла не существует";
        }

        return response;
    }

    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String username = update.getMessage().getChat().getUserName();
            String request = update.getMessage().getText();
            long userId   = update.getMessage().getChat().getId();
            long chat_id = update.getMessage().getChatId();

            String response = "";

            // Reading associated file:
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            DateFormat dateFormatDateOnly = new SimpleDateFormat("dd-MM-yyyy");
            Date date = new Date();

            String table_filename_noext = dateFormatDateOnly.format(date);

            if(request.equals("/start")) {
                response = "Приветствую!\n" +
                        "Я собираю информацию по заказам на текущий день.\n" +
                        "Просто пишите сюда сумму продажи, например '250'";
            }
            else if(request.equals("/help")) {

            }
            else if(request.equals("/settings")) {

            }
            else if(request.equals("/daysum")) {
                response = Float.toString(getDaySum(table_filename_noext + ".csv"));
            }
            else if(request.contains("/getfile"))
            {
                String[] words = request.split("\\s+");
                if(words.length == 1) {
                    response = getFileForUpload(table_filename_noext, chat_id);
                }
                else if(words.length == 2) {

                    if(words[1].equals("вчера")) {
                        Date d = new Date(date.getTime() - 24 * 3600 * 1000);
                        String filename = dateFormatDateOnly.format(d);
                        response = getFileForUpload(filename, chat_id);
                    }
                    else {
                        SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
                        try {
                            Date d = parser.parse(words[1]);
                            response = getFileForUpload(words[1], chat_id);
                        }
                        catch(ParseException e) {
                            e.printStackTrace();
                            response =
                                    "Неправильный формат даты\n" +
                                    "Необходимо: dd-MM-yyyy\n" +
                                    "Например: /getfile 01-01-2017";
                        }
                    }
                }
            }
            else {

                // make record into csv table:
                String record = userId + ";" + username + ";" + request + ";" + dateFormat.format(date) + "\n";
                handleFile(table_filename_noext + ".csv", record);
                response = "Записал";
            }


            // send response to user:
            SendMessage message = new SendMessage()
                    .setChatId(chat_id)
                    .setText(response);
            try {
                sendMessage(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

    }

    public String getBotUsername() {
        return "OrderTrackerBot_bot";
    }

    public String getBotToken() {
        return "445107190:AAGSZJHeTLrzcq2AAFGVuMn20C1xEFU6A5U";
    }
}