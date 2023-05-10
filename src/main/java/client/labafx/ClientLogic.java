package client.labafx;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import lombok.SneakyThrows;
import ticket.TicketBuilder;
import utility.Answer;
import utility.Command;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import static java.lang.Thread.sleep;

public class ClientLogic {
    private Long ticketsId = 0L;
    private volatile ArrayList<TicketBuilder> tickets = new ArrayList<>();
    private final TicketBuilder tb = new TicketBuilder();
    public String userName;
    public String userPassword;
    private String color;
    LocalDateTime creationTime;

    public void exit() {
        System.exit(0);
    }

    public String registration(String[] command) {
        return communicatingWithServer(new Command(new String[]{"sign_up"}, command[0], command[1])).text();
    }

    public String authorization(String[] command) {
        return communicatingWithServer(new Command(new String[]{"sign_in"}, command[0], command[1])).text();
    }

    private boolean isStringParsableToLong(String s) {
        try {
            Long.parseLong(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean isStringParsableToJson(String s) {
        try {
            JsonParser.parseString(s);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }

    public boolean isCollectionUpdated() throws IOException, InterruptedException, ClassNotFoundException {
        if (!authorizationVerification()) return false;
        String req = communicatingWithServer(new Command(new String[]{"is_collection_updated", ticketsId.toString()}, userName, userPassword)).text();
        if (isStringParsableToLong(req))
            ticketsId = Long.parseLong(req);
        return !(req.equals("NO") || req.equals("Не удалось получить ответ от сервера"));
    }

    public void updateMyCollection() throws IOException, InterruptedException, ClassNotFoundException {
        String text = communicatingWithServer(new Command(new String[]{"show"}, userName, userPassword)).text();
        synchronized (tickets) {
            if (text.trim().isBlank()) tickets.clear();
            if (text.trim().equals("") || text.equals("error") || !text.trim().matches("\\d+(?:\\s+\\d+)*")) return;
            ValueSumCounter counter = new ValueSumCounter(Arrays.stream(text.split(" ")).map(Long::parseLong).toArray(Long[]::new), this);
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            tickets = new ArrayList<>();
            for (String s : forkJoinPool.invoke(counter).split("\n")) {
                if (!s.isBlank()) {
                    TicketBuilder tb = new TicketBuilder(s);
                    tickets.add(tb);
                }
            }
        }
    }

    public ArrayList<TicketBuilder> getTickets() {
        return tickets;
    }

    public Answer communicatingWithServer(Command command) {
        try {
            if (command.getTicketBuilder() != null) {
                command.getTicketBuilder().setUserColor(color);
            }
            int port = 5473;
            SocketChannel sock = SocketChannel.open(new InetSocketAddress("localhost", port));
            sock.configureBlocking(false);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(command);
            oos.close();
            ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());
            sock.write(buf);
            buf.clear();
            byte[] buffer = new byte[131072];
            ByteBuffer buff = ByteBuffer.wrap(buffer);
            ObjectInputStream ois = null;
            Answer answer;
            long startTime = System.currentTimeMillis();
            while (true) {
                if (System.currentTimeMillis() - startTime > 1200) {
                    answer = new Answer("Не удалось получить ответ от сервера", false);
                    break;
                }
                sleep(30);
                try {
                    sock.read(buff);
                    ois = new ObjectInputStream(new ByteArrayInputStream(buff.array()));
                    answer = (Answer) ois.readObject();
                    break;
                } catch (StreamCorruptedException | ClassNotFoundException ignored) {
                }
            }
            String answerText = answer.text();
            if ((command.getCommand()[0].equals("sign_up") || command.getCommand()[0].equals("sign_in"))) {
                if (!answerText.split("/")[0].equals("OK")) {
                    userName = null;
                    userPassword = null;
                } else if (answerText.split("/").length > 1 && answerText.split("/")[1].length() == 6) {
                    color = answerText.split("/")[1];
                    userName = command.getName();
                    userPassword = command.getPassword();
                    creationTime = LocalDateTime.parse(communicatingWithServer(new Command(new String[]{"get_creation_date"}, userName, userPassword)).text(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss ZZ"));
                }
            }
            if (ois != null)
                ois.close();
            sock.close();
            return answer;
        } catch (IOException | InterruptedException e) {
            return new Answer("error", true);
        }
    }

    private boolean authorizationVerification() {
        return userName != null && userPassword != null;
    }

    public Integer getTicketArraySize() {
        return tickets.size();
    }

    public TicketBuilder getTBFromId(Long id) {
        return tickets.stream().filter(t -> Objects.equals(t.getId(), id)).toList().get(0);
    }

    public String getColor() {
        return color;
    }

    public ZonedDateTime getCreationTime() {
        return ZonedDateTime.of(creationTime, ZoneId.of("Europe/Moscow"));
    }
}