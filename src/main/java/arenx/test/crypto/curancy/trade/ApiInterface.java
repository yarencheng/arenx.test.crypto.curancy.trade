package arenx.test.crypto.curancy.trade;

import java.util.Set;

public interface ApiInterface {

    public void subscribeOrder(Set<Currency> currencies);

    public void setOrderUpdateListener(OrderUpdateListener listener);

    public void setReconnectListener(Runnable listener);
}
