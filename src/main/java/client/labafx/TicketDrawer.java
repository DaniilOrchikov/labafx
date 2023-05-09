package client.labafx;

import client.labafx.command.Update;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import ticket.TicketBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class TicketDrawer {
    private final Canvas canvas;
    private Map<Long, TicketPicture> ticketPicturesMap = new HashMap();
    private double width;
    private double height;
    private double minX = Double.POSITIVE_INFINITY;
    private double maxX = Double.NEGATIVE_INFINITY;
    private double minY = Double.POSITIVE_INFINITY;
    private double maxY = Double.NEGATIVE_INFINITY;
    private final GraphicsContext graphicsContext;
    private Set<Long> idSet = new HashSet<>();
    private final Update updateCommad;
    private final String userName;

    public TicketDrawer(ArrayList<TicketBuilder> tickets, Canvas canvas, Update updateCommand, String userName) {
        this.userName = userName;
        this.updateCommad = updateCommand;
        this.graphicsContext = canvas.getGraphicsContext2D();
        this.canvas = canvas;
        for (TicketBuilder tb : tickets) {
            add(tb);
        }
    }

    public void draw() {
        for (TicketPicture ticketPicture : ticketPicturesMap.values().stream().sorted(Comparator.comparingInt(x -> x.getTicketBuilder().getY())).toList()) {
            double x = ticketPicture.getTicketBuilder().getX();
            double y = ticketPicture.getTicketBuilder().getY();
            x = ((x - minX) / (maxX - minX)) * width * 0.8 + width * 0.1;
            y = ((y - minY) / (maxY - minY)) * height * 0.65 + height * 0.2;
            ticketPicture.draw(x, y);
        }
        List<TicketPicture> arrayList = ticketPicturesMap.values().stream().filter(TicketPicture::isSelected).sorted((x, y) -> Integer.compare(y.getTicketBuilder().getY(), x.getTicketBuilder().getY())).toList();
        if (arrayList.size() > 0) {
            TicketPicture selectedTicket = arrayList.get(0);
            double x = selectedTicket.getTicketBuilder().getX();
            double y = selectedTicket.getTicketBuilder().getY();
            x = ((x - minX) / (maxX - minX)) * width * 0.8 + width * 0.1;
            y = ((y - minY) / (maxY - minY)) * height * 0.65 + height * 0.2;
            selectedTicket.draw(x, y);
            if (arrayList.size() > 1)
                for (TicketPicture ticketPicture : arrayList.subList(1, arrayList.size()))
                    ticketPicture.setSelected(false);
        }
    }

    private void add(TicketBuilder ticketBuilder) {
        idSet.add(ticketBuilder.getId());
        ticketPicturesMap.put(ticketBuilder.getId(), new TicketPicture(ticketBuilder, canvas, updateCommad, userName));
        setMinMax(ticketBuilder);
    }

    private void setMinMax(TicketBuilder ticketBuilder) {
        minX = Math.min(minX, ticketBuilder.getX() - 1);
        maxX = Math.max(maxX, ticketBuilder.getX() + 1);
        minY = Math.min(minY, ticketBuilder.getY() - 1);
        maxY = Math.max(maxY, ticketBuilder.getY() + 1);
    }

    public void update(ArrayList<TicketBuilder> tickets) {
        boolean update = false;
        for (TicketBuilder tb : tickets) {
            if (idSet.contains(tb.getId())) {
                TicketBuilder ticketBuilder = ticketPicturesMap.get(tb.getId()).getTicketBuilder();
                if (!Objects.equals(ticketBuilder.getX(), tb.getX())) {
                    update = true;
                    ticketBuilder.setX(tb.getX().toString());
                }
                if (!Objects.equals(ticketBuilder.getY(), tb.getY())) {
                    update = true;
                    ticketBuilder.setY(tb.getY().toString());
                }
                if (!Objects.equals(ticketBuilder.getName(), tb.getName())) {
                    update = true;
                    ticketBuilder.setName(tb.getName());
                }
                if (!Objects.equals(ticketBuilder.getType(), tb.getType())) {
                    update = true;
                    ticketBuilder.setType(tb.getType().toString());
                }
            } else {
                add(tb);
                update = true;
            }
        }
        Set<Long> set = new HashSet<>(tickets.stream().map(TicketBuilder::getId).toList());
        Set<Long> ketSet = new HashSet<>(ticketPicturesMap.keySet());
        for (Long id : ketSet)
            if (!set.contains(id)) {
                ticketPicturesMap.remove(id).remove();
                idSet.remove(id);
                update = true;
            }
        if (update) {
            minX = Double.POSITIVE_INFINITY;
            maxX = Double.NEGATIVE_INFINITY;
            minY = Double.POSITIVE_INFINITY;
            maxY = Double.NEGATIVE_INFINITY;
            for (TicketPicture ticketPicture : ticketPicturesMap.values())
                setMinMax(ticketPicture.getTicketBuilder());
        }
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }
}
