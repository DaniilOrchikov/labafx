package client.labafx.command;

import client.labafx.ClientLogic;
import client.labafx.ErrorWindow;
import client.labafx.ExplanationPopup;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ticket.TicketBuilder;
import ticket.TicketType;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Paths;
import java.util.*;

public class ExecuteScript extends Command {
    private ResourceBundle bundle;
    private FileChooser fileChooser;
    private Scanner in = new Scanner(System.in);
    /**
     * Поле стек сканеров.
     * Используется для организации рекурсивного чтения из файлов.
     * Когда в файле встречается команда исполнения нового файла, сюда помещается старый сканер
     */
    private final Stack<Scanner> scannerStack = new Stack<>();
    /**
     * Поле стек имен файлов.
     * Используется для организации рекурсивного чтения из файлов.
     * Когда начинается чтение из файла сюда заносится его имя. При каждой попытке открытия нового потока считывания файла проверяется не занесено ли сюда его имя. Если это так - запрещается считывание.
     * Это исключает ситуации бесконечной рекурсии при чтении файлов.
     */
    private final Stack<String> fileNamesStack = new Stack<>();
    private Show showCommand;

    public ExecuteScript(ClientLogic clientLogic, Show showCommand) {
        super("execute_script", "ExecuteScript", clientLogic);
        this.showCommand = showCommand;
        fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt"));
    }

    @Override
    public void pushButton(ActionEvent event) {
        File selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            ArrayList<utility.Command> commands = new ArrayList<>();
            threadPool.execute(() -> {
                try {
                    scannerStack.add(in);
                    fileNamesStack.add(selectedFile.getPath());
                    in = new Scanner(selectedFile);
                    if(!run(commands)) {
                        Platform.runLater(() -> {
                            try {
                                ErrorWindow.show("Ошибка в файле (команды не будут исполняться)", "Ошибка при выполнении команды " + getCommandName());
                            } catch (IOException ignored) {
                            }
                        });
                    }else{
                        for (utility.Command command:commands){
                            String[] req = clientLogic.communicatingWithServer(command).text().split("/");
                            if(!req[0].equals("OK")){
                                String text = req[0];
                                ArrayList<TicketBuilder> tickets = new ArrayList<>();
                                if (text.trim().equals(""))
                                    continue;
                                if (!clientLogic.isStringParsableToJson(text.split("\n")[0]) || text.equals("error")) {
                                    Platform.runLater(() -> {
                                        try {
                                            ErrorWindow.show(text, "Ошибка при выполнении команды " + command.getCommand()[0]);
                                        } catch (IOException ignored) {
                                        }
                                    });
                                    return;
                                }
                                for (String s : text.split("\n")) {
                                    TicketBuilder tb = new TicketBuilder(s);
                                    tickets.add(tb);
                                }
                                showCommand.fromArray(tickets);
                            }
                        }
                    }
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        try {
                            ErrorWindow.show("Не удалось прочитать файл", "Ошибка при выполнении команды " + getCommandName());
                        } catch (IOException ignored) {
                        }
                    });
                }
            });
        }
    }

    @Override
    public void setButtonsActions() {
        button.addEventHandler(ActionEvent.ACTION, this::pushButton);
    }

    @Override
    public void changeLocale(ResourceBundle bundle) {
        this.bundle = bundle;
        super.changeLocale(bundle);
        fileChooser.setTitle(bundle.getString("selectFile"));
    }

    public boolean run(ArrayList<utility.Command> commands) {
        while (!scannerStack.empty()) {
            String[] command = read();
            if (command[0].equals("exit"))return true;
            try {
                if (!nextCommand(command, commands)) return false;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }

    public String[] read() {
        checkingScanner();
        if (scannerStack.empty()) return new String[]{"exit"};
        return nextInput().split(" ");
    }

    private String nextInput() {
        return in.nextLine().trim();
    }

    private void checkingScanner() {
        while (!in.hasNext()) {
            in = scannerStack.pop();
            fileNamesStack.pop();
            if (scannerStack.empty()) {
                return;
            }
        }
    }

    public boolean nextCommand(String[] command, ArrayList<utility.Command> commands) throws IOException {
        TicketBuilder tb = new TicketBuilder();
        try {
            if (!checkingCompositeCommands(command)) return false;
            switch (command[0]) {
                case ("update"):
                case ("add_if_max"):
                case ("add_if_min"):
                case ("add"):
                case ("remove_lower"):
                    tb.clear();
                    if (!enteringField("name", tb)) return false;
                    if (!enteringField("x", tb)) return false;
                    if (!enteringField("y", tb)) return false;
                    if (!enteringField("price", tb)) return false;
                    if (!enteringField("tType", tb)) return false;
                    if (!enteringField("capacity", tb)) return false;
                    if (!enteringField("vType", tb)) return false;
                    if (!enteringField("street", tb)) return false;
                    if (!enteringField("zip", tb)) return false;
                    tb.setUserName(clientLogic.userName);
                    commands.add(new utility.Command(command, tb, clientLogic.userName, clientLogic.userPassword));
                    return true;
                case ("info"):
                case ("show"):
                case ("remove_by_id"):
                case ("remove_at"):
                case ("clear"):
                case ("remove_first"):
                case ("min_by_venue"):
                case ("filter_contains_name"):
                case ("filter_less_than_price"):
                case ("filter_by_price"):
                case ("count_greater_than_type"):
                case ("print_field_ascending_type"):
                    commands.add(new utility.Command(command, clientLogic.userName, clientLogic.userPassword));
                    return true;
                case ("execute_script"):
                    try {
                        new Scanner(Paths.get(command[1]));
                    } catch (AccessDeniedException e) {
                        return false;
                    }
                    scannerStack.add(in);
                    fileNamesStack.add(command[1]);
                    in = new Scanner(Paths.get(command[1]));
                    return true;
                default:
                    return false;
            }
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private boolean checkingCompositeCommands(String[] command) {
        switch (command[0]) {
            case ("update"):
            case ("remove_by_id"):
                try {
                    Long.parseLong(command[1]);
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    return false;
                }
                break;
            case ("count_greater_than_type"):
                try {
                    TicketType.valueOf(command[1]);
                } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                    return false;
                }
                break;
            case ("filter_less_than_price"):
            case ("filter_by_price"):
                try {
                    if (Long.parseLong(command[1]) <= 0) {
                        return false;
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    return false;
                }
                break;
            case ("filter_contains_name"):
                break;
            case ("execute_script"):
                if (command.length < 2) {
                    return false;
                }
                for (String fileName : fileNamesStack) {
                    if (fileName.equals(command[1])) {
                        return false;
                    }
                }
                File f = new File(command[1]);
                if (!f.exists()) {
                    return false;
                } else if (f.isDirectory()) {
                    return false;
                }
                break;
            case ("remove_at"):
                try {
                    int index = Integer.parseInt(command[1]);
                    if (index < 0) {
                        return false;
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    return false;
                }
                break;
            default:
                if (command.length > 1) {
                    return false;
                }
        }
        return true;
    }

    public boolean enteringField(String command, TicketBuilder tb) {
        String status = switch (command) {
            case ("name") -> tb.setName(nextInput().trim());
            case ("x") -> tb.setX(nextInput().trim());
            case ("y") -> tb.setY(nextInput().trim());
            case ("price") -> tb.setPrice(nextInput().trim());
            case ("tType") -> tb.setType(nextInput().trim());
            case ("capacity") -> tb.setVenueCapacity(nextInput().trim());
            case ("vType") -> tb.setVenueType(nextInput().trim());
            case ("street") -> tb.setAddressStreet(nextInput().trim());
            case ("zip") -> tb.setAddressZipCode(nextInput().trim());
            default -> "error";
        };
        if (!status.equals("OK")) {
            fileNamesStack.pop();
            in = scannerStack.pop();
            tb.clear();
            return false;
        }
        return true;
    }
}
