package arenx.test.crypto.curancy.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jdo.PersistenceManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("singleton")
public class DbOrderUpdateListener implements OrderUpdateListener{

    private enum TaskType{
        SIMPLE, REMOVE_ALL
    }

    private static class Task{
        public Task(TaskType taskType, String ex){
            this.taskType = taskType;
            this.ex = ex;
        }
        public Task(TaskType taskType, String ex, Action action, OrderType type, double price, double volume) {
            this.taskType = taskType;
            this.ex = ex;
            this.action = action;
            this.type = type;
            this.price = price;
            this.volume = volume;
        }
        public TaskType taskType;
        public String ex;
        public Action action;
        public OrderType type;
        public double price;
        public double volume;
    }

    private static Logger logger = LoggerFactory.getLogger(DbOrderUpdateListener.class);

    @Autowired
    private PersistenceManager pm;

    private Object tasksLock = new Object();
    private List<Task> tasks = new ArrayList<>();
    private Order.OrderKey key = new Order.OrderKey(); // reuse
    private Map<Order.OrderKey, Order> orders = new TreeMap<>();
    private AtomicBoolean isRun = new AtomicBoolean(true);
    private Thread workerThread;

    private Runnable worker = ()->{
        while (isRun.get()) {

            List<Task> todoTasks = null;

            synchronized (tasksLock) {
                if (!tasks.isEmpty()) {
                    todoTasks = tasks;
                    tasks = new ArrayList<>();
                }
            }

            if (null == todoTasks) {
                try {
                    Thread.sleep(1);
                    continue;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

//            Transaction tx = pm.currentTransaction();
//            tx.begin();

            logger.info("todoTasks [{}], orders [{}]", todoTasks.size(), orders.size());

            for (Task task: todoTasks) {
                key.exchange = task.ex;
                key.type = task.type;
                key.price = task.price;

                Order order = null;

                switch (task.taskType) {
                case REMOVE_ALL:

                    orders = orders.entrySet().stream()
                        .filter(e->!e.getKey().exchange.equals(task.ex))
                        .collect(Collectors.toMap(e->e.getKey(), e->e.getValue(), (a,b)->a, TreeMap::new))
                        ;

                    long numDelete = pm.newQuery(Order.class, "exchange == '" + task.ex + "'").deletePersistentAll();
                    logger.info("delete [{}] records from [{}]", numDelete, task.ex);

                    break;
                case SIMPLE:
                    switch (task.action) {
                    case REMOVE:

                        order = orders.remove(key);

                        if (logger.isDebugEnabled()) {
                            logger.debug("remove {}", order);
                        }

                        if (null != order) {
                            pm.deletePersistent(order);
                        }

                        break;
                    case REPLACE:

                        order = orders.get(key);

                        if (null == order) {
                            order = new Order(task.ex, task.type, 0.0, task.volume, 0l);
                            orders.put(key.copy(), order);
                        }

                        order.setPrice(task.price);
                        order.setUpdateMilliSeconds(System.currentTimeMillis());

                        if (logger.isDebugEnabled()) {
                            logger.debug("replace {}", order);
                        }

                        pm.makePersistent(order);

                        break;
                    case UPDATE:

                        order = orders.get(key);

                        if (null == order) {
                            order = new Order(task.ex, task.type, 0.0, task.volume, 0l);
                            orders.put(key.copy(), order);
                        }

                        order.setPrice(order.getPrice() + task.price);
                        order.setUpdateMilliSeconds(System.currentTimeMillis());

                        if (logger.isDebugEnabled()) {
                            logger.debug("update {}", order);
                        }

                        pm.makePersistent(order);

                        break;
                    default:
                        break;

                    }
                    break;
                default:
                    throw new RuntimeException("unknown type");

                }
            }

//            tx.commit();
        }
    };

    @Override
    public void update(String ex, Action action, OrderType type, double price, double volume) {
//        logger.info("{} {} {} {} {}", ex, action, type, price, volume);

        Task task = new Task(TaskType.SIMPLE, ex, action, type, price, volume);

        synchronized (tasksLock) {
            tasks.add(task);
        }
    }

    @Override
    public void removeAll(String ex){
        logger.info("{}", ex);

        Task task = new Task(TaskType.REMOVE_ALL, ex);

        synchronized (tasksLock) {
            tasks.add(task);
        }
    }

    @PostConstruct
    private void start() {
        workerThread = new Thread(worker, "db.writer");
        workerThread.start();
    }

    @PreDestroy
    private void stop() throws InterruptedException{
        logger.info("join db write thread");
        isRun.set(false);
        workerThread.join();

        pm.close();
    }

}
