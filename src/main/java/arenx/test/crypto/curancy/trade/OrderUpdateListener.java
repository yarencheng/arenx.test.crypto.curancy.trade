package arenx.test.crypto.curancy.trade;

public interface OrderUpdateListener {
    public enum Action{
        REPLACE, UPDATE, REMOVE
    }

    public enum Type{
        ASK, BID
    }

    public void update(String ex, Action action, Type type, double price, double volume);

    public void removeAll(String ex);
}
