package client.labafx;

import ticket.*;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class LocalTicketBuilder {
    private TicketBuilder ticketBuilder;
    private String id;
    private String x;
    private String y;
    private String price;
    private String capacity;
    private String creationDate;
    private String name;
    private String userName;
    private String addressStreet;
    private String addressZipCode;
    private TicketType type;
    private VenueType venueType;
    private ResourceBundle bundle;
    NumberFormat numberFormatter;

    public LocalTicketBuilder(TicketBuilder tb, ResourceBundle bundle) {
        numberFormatter = NumberFormat.getInstance(bundle.getLocale());
        this.bundle = bundle;
        ticketBuilder = tb;
        name = tb.getName();
        userName = tb.getUserName();
        addressStreet = tb.getAddressStreet();
        addressZipCode = tb.getAddressZipCode();
        type = tb.getType();
        venueType = tb.getVenueType();
        changeLocale(bundle);
    }

    public void setName(String name) {
        if (ticketBuilder.setName(name).equals("OK")) this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setX(String strX) {
        if (ticketBuilder.setX(strX).equals("OK")) x = numberFormatter.format(ticketBuilder.getX());
    }

    public String getX() {
        return this.x;
    }

    public void setY(String strY) {
        if (ticketBuilder.setY(strY).equals("OK")) y = numberFormatter.format(ticketBuilder.getY());
    }

    public String getY() {
        return this.y;
    }

    public void setPrice(String strPrice) {
        if (ticketBuilder.setPrice(strPrice).equals("OK")) this.price = numberFormatter.format(ticketBuilder.getPrice());
    }

    public String getPrice() {
        return this.price;
    }

    public void setAddressStreet(String addressStreet) {
        if (ticketBuilder.setAddressStreet(addressStreet).equals("OK")) this.addressStreet = addressStreet;
    }

    public String getAddressStreet() {
        return this.addressStreet;
    }

    public void setAddressZipCode(String addressZipCode) {
        if (ticketBuilder.setAddressZipCode(addressZipCode).equals("OK")) this.addressZipCode = addressZipCode;
    }

    public String getAddressZipCode() {
        return this.addressZipCode;
    }

    public void setType(String strType) {
        if (ticketBuilder.setType(strType).equals("OK")) this.type = TicketType.valueOf(strType);
    }

    public TicketType getType() {
        return this.type;
    }

    public void setVenueCapacity(String strVenueCapacity) {
        if (ticketBuilder.setVenueCapacity(strVenueCapacity).equals("OK")) this.capacity = numberFormatter.format(ticketBuilder.getVenueCapacity());
    }

    public String getVenueCapacity() {
        return this.capacity;
    }

    public void setVenueType(String strVenueType) {
        if (ticketBuilder.setVenueType(strVenueType).equals("OK")) this.venueType = VenueType.valueOf(strVenueType);
    }

    public VenueType getVenueType() {
        return this.venueType;
    }

    public void setCreationDate(LocalDateTime dt) {
        ticketBuilder.setCreationDate(dt);
        this.creationDate = dt.format(DateTimeFormatter.ofPattern(bundle.getString("date.format") + " " + bundle.getString("time.format")));
    }

    public String getCreationDate() {
        return this.creationDate;
    }

    public void setUserName(String userName) {
        this.userName = userName;
        ticketBuilder.setUserName(userName);
    }

    public String getUserName() {
        return this.userName;
    }

    public void setId(Long id) {
        ticketBuilder.setId(id);
        this.id = numberFormatter.format(id);
    }

    public String getId() {
        return this.id;
    }

    public int compareTo(TicketBuilder tb) {
        return ticketBuilder.compareTo(tb);
    }

    public TicketBuilder getTicketBuilder() {
        return ticketBuilder;
    }
    public void changeLocale(ResourceBundle bundle) {
        this.bundle = bundle;
        numberFormatter = NumberFormat.getInstance(bundle.getLocale());
        this.id = numberFormatter.format(ticketBuilder.getId());
        this.creationDate = ticketBuilder.getCreationDate().format(DateTimeFormatter.ofPattern(bundle.getString("date.format") + " " + bundle.getString("time.format")));
        this.capacity = numberFormatter.format(ticketBuilder.getVenueCapacity());
        this.price = numberFormatter.format(ticketBuilder.getPrice());
        x = numberFormatter.format(ticketBuilder.getX());
        y = numberFormatter.format(ticketBuilder.getY());
    }
}
