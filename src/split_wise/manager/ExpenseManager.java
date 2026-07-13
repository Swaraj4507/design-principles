package split_wise.manager;

import split_wise.entities.Expense;
import split_wise.entities.Group;
import split_wise.entities.Split;
import split_wise.entities.User;
import split_wise.strategy.SplitRequest;
import split_wise.strategy.SplitStrategy;
import split_wise.strategy.SplitType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExpenseManager {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Group> groups = new ConcurrentHashMap<>();
    private final Map<SplitType, SplitStrategy> strategies;

    public ExpenseManager(Map<SplitType, SplitStrategy> strategies) {
        this.strategies = strategies;
    }

    public void addUser(User user) {
        users.put(user.getId(), user);
    }

    public void createGroup(Group group) {
        groups.put(group.getId(), group);
    }

    public void addUserToGroup(String groupId, String userId) {
        groups.get(groupId).addMember(users.get(userId));
    }

    public Expense createExpense(String groupId, String description, double amount, String paidByUserId,
                                  SplitType splitType, List<String> participantUserIds, Map<String, Double> shareData) {
        Group group = groups.get(groupId);
        User paidBy = users.get(paidByUserId);

        List<User> participants = new ArrayList<>();
        for (String userId : participantUserIds) {
            participants.add(users.get(userId));
        }

        Map<User, Double> resolvedShareData = null;
        if (shareData != null) {
            resolvedShareData = new HashMap<>();
            for (Map.Entry<String, Double> entry : shareData.entrySet()) {
                resolvedShareData.put(users.get(entry.getKey()), entry.getValue());
            }
        }

        SplitStrategy strategy = strategies.get(splitType);
        List<Split> splits = strategy.calculateSplit(new SplitRequest(amount, participants, resolvedShareData));

        Expense expense = new Expense(UUID.randomUUID().toString(), description, amount, paidBy, splitType, splits);
        addExpense(group, expense);
        return expense;
    }

    public void addExpense(Group group, Expense expense) {
        group.addExpense(expense);

        User payer = expense.getPaidBy();
        for (Split split : expense.getSplits()) {
            User participant = split.getUser();
            if (participant.equals(payer)) {
                continue;
            }
            settle(payer, participant, split.getAmountOwed());
        }
    }

    private void settle(User payer, User participant, double amount) {
        User first = payer.getId().compareTo(participant.getId()) < 0 ? payer : participant;
        User second = (first == payer) ? participant : payer;

        synchronized (first) {
            synchronized (second) {
                payer.adjustBalance(participant, amount);
                participant.adjustBalance(payer, -amount);
            }
        }
    }
}
