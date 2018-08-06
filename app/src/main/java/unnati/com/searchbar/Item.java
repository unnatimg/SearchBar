package unnati.com.searchbar;

public class Item {
    private long itemId;
    private String title;
    private String image;
    private double price;
    private String description;

    public Item(long itemId, String title, String image, double price, String description) {
        this.itemId = itemId;
        this.title = title;
        this.image = image;
        this.price = price;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    @Override
    public String toString() {
        return title +" "+price + " " + description;
    }
}
