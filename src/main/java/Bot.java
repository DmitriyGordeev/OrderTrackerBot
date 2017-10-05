import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.api.methods.send.SendDocument;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class Bot extends TelegramLongPollingBot {

    private DatabaseHandler database;
    private DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private DateFormat dateFormatMonth = new SimpleDateFormat("MM-yyyy");

    public Bot(String tablename) {

        String url = "jdbc:mysql://mysql5.gear.host:3306/orderbot";
        String user = "orderbot";
        String pass = "Cc2-_M6KqMWH";
        database = new DatabaseHandler(url, user, pass, tablename);
    }

    // Uploads document:
    private void sendDocUploadingAFile(Long chatId, java.io.File save,String caption) throws TelegramApiException {

        SendDocument sendDocumentRequest = new SendDocument();
        sendDocumentRequest.setChatId(chatId);
        sendDocumentRequest.setNewDocument(save);
        sendDocumentRequest.setCaption(caption);
        sendDocument(sendDocumentRequest);
    }

    /* Updated methods: */
    public float getDaySum_db(String date) throws SQLException {

        float outputValue = 0;
        ArrayList<SaleRecord> saleRecords = database.getRecords(date);
        for(SaleRecord s : saleRecords) {
            outputValue += UpdateParser.parsePrice(s.message);
        }

        return outputValue;
    }
    public String daysumCommand_db(String request) {

        String[] words = request.split("\\s+");
        if(words.length == 1)
        {
            Date today = new Date();
            try {
                return Float.toString(getDaySum_db(dateFormat.format(today)));
            }
            catch(SQLException e) {
                e.printStackTrace();
                return "Проверьте подключение к базе данных";
            }
        }
        else if(words.length == 2) {

            try {
                dateFormat.parse(words[1]);
                try {
                    return "Выручка за " + words[1] + " : " + Float.toString(getDaySum_db(words[1]));
                }
                catch(SQLException e) {
                    e.printStackTrace();
                    return "Проверьте подключение к базе данных";
                }
            }
            catch(ParseException e) {
                return "Неверный формат даты\n" +
                        "Необходимо: dd-MM-yyyy\n" +
                        "Например: /command 01-01-2017";
            }
        }
        return "";
    }

    public float getMonthSum_db(String date) throws SQLException {

        float outputValue = 0;
        ArrayList<SaleRecord> saleRecords = database.getRecordsMonth(date);
        for(SaleRecord s : saleRecords) {
            outputValue += UpdateParser.parsePrice(s.message);
        }

        return outputValue;
    }
    public String monthsumCommand_db(String request) {

        String[] words = request.split("\\s+");
        if(words.length == 1)
        {
            Date today = new Date();
            try {
                return Float.toString(getMonthSum_db(dateFormatMonth.format(today)));
            }
            catch(SQLException e) {
                e.printStackTrace();
                return "Проверьте подключение к базе данных";
            }
        }
        else if(words.length == 2) {

            try {
                dateFormatMonth.parse(words[1]);
                try {
                    return "Выручка за " + words[1] + " : " + Float.toString(getMonthSum_db(words[1]));
                }
                catch(SQLException e) {
                    e.printStackTrace();
                    return "Проверьте подключение к базе данных";
                }
            }
            catch(ParseException e) {
                return "Неверный формат даты\n" +
                        "Необходимо: MM-yyyy\n" +
                        "Например: /command 01-2017";
            }
        }
        return "";
    }

    public void createDayFile(String date) throws IOException, SQLException {

        ArrayList<SaleRecord> records = database.getRecords(date);
        String fileContent = "";
        for(SaleRecord s : records) {
            fileContent += s.csv("dd-MM-yyyy") + "\n";
        }

        Fileio.writefile("dayfile.csv", fileContent);
    }
    public String prepareForUpload(Date date, long chat_id) {

        String response = "";
        try { createDayFile(dateFormat.format(date)); }
        catch(IOException e) {
            e.printStackTrace();
            response = "Не удалось создать файл выгрузки\n";
            return response;
        }
        catch(SQLException e) {
            e.printStackTrace();
            response = "Проверьте подключение к базе данных\n";
            return response;
        }


        File file = new File("dayfile.csv");
        if(!file.exists()) {
            return "На " + dateFormat.format(date) + " записей нет";
        }

        try {
            sendDocUploadingAFile(chat_id, file, "Отчет за " + dateFormat.format(date));
        }
        catch(TelegramApiException e) {
            response = "На " + dateFormat.format(date) + " записей нет";
        }

        return response;
    }
    public String dayFileCommand_db(String request, long chat_id) {

        String response = "";
        String[] words = request.split("\\s+");
        if(words.length == 1) {
            Date today = new Date();
            response = prepareForUpload(today, chat_id);
        }
        else if(words.length == 2)
        {
            try {
                Date date = dateFormat.parse(words[1]);
                response = prepareForUpload(date, chat_id);
            }
            catch(ParseException e) {
                return "Неверный формат даты\n" +
                        "Необходимо: dd-MM-yyyy\n" +
                        "Например: /command 01-01-2017";
            }
        }

        return response;
    }

    public void createMonthFile(String date) throws IOException, SQLException {
        ArrayList<SaleRecord> records = database.getRecordsMonth(date);
        String fileContent = "";
        for(SaleRecord s : records) {
            fileContent += s.csv("dd-MM-yyyy") + "\n";
        }

        Fileio.writefile("monthfile.csv", fileContent);
    }
    public String prepareMonthForUpload(Date date, long chat_id) {

        String response = "";
        try { createMonthFile(dateFormatMonth.format(date)); }
        catch(IOException e) {
            response = "Не удалось создать файл выгрузки\n";
            return response;
        }
        catch(SQLException e) {
            e.printStackTrace();
            response = "Проверьте подключение к базе данных\n";
            return response;
        }

        File file = new File("monthfile.csv");
        if(!file.exists()) {
            return "На " + dateFormatMonth.format(date) + " записей нет";
        }


        try {
            sendDocUploadingAFile(chat_id, file, "Отчет за " + dateFormatMonth.format(date));
        }
        catch(TelegramApiException e) {
            return "На " + dateFormatMonth.format(date) + " записей нет";
        }

        return response;
    }
    public String monthFileCommand_db(String request, long chat_id) {

        String response = "";
        String[] words = request.split("\\s+");
        if(words.length == 1) {
            Date today = new Date();
            response = prepareMonthForUpload(today, chat_id);
        }
        else if(words.length == 2)
        {
            try {
                Date date = dateFormatMonth.parse(words[1]);
                response = prepareMonthForUpload(date, chat_id);
            }
            catch(ParseException e) {
                return "Неверный формат даты\n" +
                        "Необходимо: MM-yyyy\n" +
                        "Например: /command 01-01-2017";
            }
        }

        return response;
    }

    /* -------------------------------------------------------------------- */

    private SendMessage setupKeyboard(long chat_id) {

        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        // Создаем клавиуатуру
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        // Создаем список строк клавиатуры
        ArrayList<KeyboardRow> keyboard = new ArrayList<KeyboardRow>();

        // Первая строчка клавиатуры
        KeyboardRow keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Файл-отчет за сегодня");

        // Вторая строчка клавиатуры
        KeyboardRow keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add("Выручка за сегодня");

        // Третья строчка клавиатуры
        KeyboardRow keyboardThirdRow = new KeyboardRow();
        keyboardThirdRow.add("Выручка за этот месяц");

        KeyboardRow keyboardFourthRow = new KeyboardRow();
        keyboardFourthRow.add("Файл-отчет за этот месяц");

        // Добавляем все строчки клавиатуры в список
        keyboard.add(keyboardFirstRow);
        keyboard.add(keyboardSecondRow);
        keyboard.add(keyboardThirdRow);
        keyboard.add(keyboardFourthRow);

        // и устанваливаем этот список нашей клавиатуре
        replyKeyboardMarkup.setKeyboard(keyboard);
        sendMessage.setChatId(chat_id);
//        sendMessage.setReplyToMessageId(message.getMessageId());

        return sendMessage;
    }


    // transforms human-readable button caption into /command
    public String messageCommand(String message) {

        if(message.equals("Файл-отчет за сегодня")) {
            return "/getfile";
        }
        else if(message.equals("Выручка за сегодня")) {
            return "/daysum";
        }
        else if(message.equals("Выручка за этот месяц")) {
            return "/monthsum";
        }
        else if(message.equals("Файл-отчет за этот месяц")) {
            return "/monthfile";
        }

        return message;
    }


    public void onUpdateReceived(Update update) {

        if (update.hasMessage() && update.getMessage().hasText()) {

            String username = update.getMessage().getChat().getUserName();
            String request = update.getMessage().getText();
            long userId   = update.getMessage().getChat().getId();
            long chat_id = update.getMessage().getChatId();

            SendMessage sendMessage = setupKeyboard(chat_id);
            request = messageCommand(request);

            String response = "";
            Date date = new Date();

            // TODO: refactor to switch - case:
            if(request.equals("/start")) {
                response =
                        "Приветствую!\n" +
                        "Я собираю информацию по заказам на текущий день.\n" +
                        "Просто пишите сюда информацию о продаже, например 'Название услуги 250'";
            }
            else if(request.equals("/help")) {
                response = "Доступны следующие команды:\n\n" +
                        "/daysum - выручка за сегодня\n\n" +
                        "/daysum 01-09-2017 - выручка за указаную дату\n\n" +
                        "/getfile - файл-отчет по сегодняшним продажам\n\n" +
                        "/getfile 01-09-2017 - файл-отчет за указаную дату, например 1 сентября\n\n" +
                        "/monthsum - выручка за текущий месяц\n\n" +
                        "/monthsum 09-2017 - выручка за указаный месяц\n\n" +
                        "/monthfile - файл-отчет по текущему месяцу\n\n" +
                        "/monthfile 09-2017 - файл-отчет за указаный месяц\n\n";
            }
            else if(request.equals("/settings")) {

            }
            else if(request.contains("/monthfile")) {
                // response = monthFileCommand(request, folderName, chat_id);
                response = monthFileCommand_db(request, chat_id);
            }
            else if(request.contains("/daysum")) {
                // response = daysumCommand(request, folderName, table_filename_noext);
                response = daysumCommand_db(request);
            }
            else if(request.contains("/monthsum")) {
                // response = monthSumCommand(request, folderName);
                response = monthsumCommand_db(request);
            }
            else if(request.contains("/getfile")) {
                // response = dayFileCommand(request, date, chat_id);
                response = dayFileCommand_db(request, chat_id);
            }
            else {

                SaleRecord userInput = new SaleRecord();
                userInput.userId = userId;
                userInput.username = username;
                userInput.message = request;
                userInput.date = date;

                try {
                    response = database.insertRecord(userInput);
                }
                catch(SQLException e) {
                    e.printStackTrace();
                    response = "Ввести значение не удалось, нужно проверить подключение к базе данных";
                }

            }


            sendMessage.setText(response);
            try {
                sendMessage(sendMessage);
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


    /* transforms dateString into newFormat style: */
    public String reformateDate(String dateString, String inputFormat, String newformat) throws ParseException {

        DateFormat dateFormat = new SimpleDateFormat(inputFormat);
        Date date = dateFormat.parse(dateString);

        dateFormat = new SimpleDateFormat(newformat);
        return dateFormat.format(date);
    }


}