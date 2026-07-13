package split_wise.entities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class User {
    private final String id;
    private final String name;
    private final String email;
    private final Map<User, Double> balanceSheet = new ConcurrentHashMap<>();

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
