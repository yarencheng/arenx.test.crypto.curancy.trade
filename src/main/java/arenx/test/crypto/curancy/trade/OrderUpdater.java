package arenx.test.crypto.curancy.trade;

public interface OrderUpdater {
    public enum Action{
        REPLACE, UPDATE, REMOVE
    }

    public void update(String ex, Action action, OrderType type, double price, double volume);

    public void removeAll(String ex);
}
