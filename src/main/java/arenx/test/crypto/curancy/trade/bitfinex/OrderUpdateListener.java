package arenx.test.crypto.curancy.trade.bitfinex;

import arenx.test.crypto.curancy.trade.Order;

public interface OrderUpdateListener {
    public void OnUpdate(Order bean);
}
