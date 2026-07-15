package split_wise.entities;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class User {
    private static final double EPSILON = 0.01;

    private final String id;
    private final String name;
    private final String email;
    private final Map<User, Double> balanceSheet = new ConcurrentHashMap<>();
    private final List<Payment> payments = new CopyOnWriteArrayList<>();

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public void adjustBalance(User other, double amount) {
        balanceSheet.merge(other, amount, Double::sum);
    }

    public double getBalance(User other) {
        return balanceSheet.getOrDefault(other, 0.0);
    }

    public Map<User, Double> getAllBalances() {
        Map<User, Double> nonZeroBalances = new HashMap<>();
        for (Map.Entry<User, Double> entry : balanceSheet.entrySet()) {
            if (Math.abs(entry.getValue()) > EPSILON) {
                nonZeroBalances.put(entry.getKey(), entry.getValue());
            }
        }
        return nonZeroBalances;
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    public List<Payment> getPayments() {
        return Collections.unmodifiableList(payments);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        return id.equals(((User) o).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
