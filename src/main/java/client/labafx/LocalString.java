package client.labafx;

public class LocalString {
    private String name;
    private String localName;

    public LocalString(String name, String localName) {
        this.name = name;
        this.localName = localName;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return localName;
    }
}
