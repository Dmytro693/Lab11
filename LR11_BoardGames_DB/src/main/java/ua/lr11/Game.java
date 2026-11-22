package ua.lr11;

public class Game {
    private final int id;
    private final String name;
    private final int count;
    private final double price;
    private final String type;
    private final String extra;

    public Game(int id, String name, int count, double price, String type, String extra) {
        this.id = id; this.name = name; this.count = count; this.price = price;
        this.type = type; this.extra = extra;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public int getCount() { return count; }
    public double getPrice() { return price; }
    public String getType() { return type; }
    public String getExtra() { return extra; }
}
