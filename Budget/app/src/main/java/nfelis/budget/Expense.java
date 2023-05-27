package nfelis.budget;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

public class Expense {
    public static LinkedHashMap<Integer,Expense> expenseMap = new LinkedHashMap<>();
    public static String EXPENSE_EDIT_EXTRA =  "expenseEdit";
    private int id;             // id de reconnaissance de la dépense
    private String title;       // titre/descriptif de la dépense
    private int category;    // id de la catégorie de la dépense (santé, loisir, alimentaire, etc.)
    private int amount;         // valeur de la dépense, elle sera stockée en entiers
    private Date date;          // date de la dépense
    private boolean cash, retrait, rembourse, rembourseCash;       // si la dépense a été faite par cash ou pas (carte)
    public Expense(int id, String title, int category, int amount, Date date, boolean cash, boolean withdrawal,boolean rembourse,boolean rembourseCash) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.cash = cash;
        this.retrait = withdrawal;
        this.rembourse = rembourse;
        this.rembourseCash = rembourseCash;
    }
    public static Expense getExpenseForID(int passedExpenseID) {
        for (Expense expense : expenseMap.values())
        {
            if(expense.getId() == passedExpenseID)
                return expense;
        }
        return null;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getCategory() {
        return category;
    }
    public void setCategory(int category) {
        this.category = category;
    }
    public int getAmount() {
        return amount;
    }
    public void setAmount(int amount) {
        this.amount = amount;
    }
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public boolean isCash() {
        return cash;
    }
    public void setCash(boolean cash) {
        this.cash = cash;
    }
    public boolean isRetrait() {
        return retrait;
    }
    public void setRetrait(boolean retrait) {
        this.retrait = retrait;
    }
    public void setRembourse(boolean rembourse) {this.rembourse = rembourse;}
    public boolean isRembourse() {return rembourse;}
    public boolean isRembourseCash() {
        return rembourseCash;
    }
    public void setRembourseCash(boolean rembourseCash) {
        this.rembourseCash = rembourseCash;
    }

    public static List<Integer> getProgress(List<Category> categories) {
        List<Integer> progress = new ArrayList<>();
        List<Integer> ids = new ArrayList<>();
        for (Category category : categories) {
            ids.add(category.getId());
            progress.add(0);
        }

        for (Expense expense : expenseMap.values()) {
            int index = ids.indexOf(expense.getCategory());
            if (index != -1) {
                int amount = (expense.isRembourse()) ? 0 : expense.getAmount();
                progress.set(index,progress.get(index)+amount);
            }
        }
        return progress;
    }

    public static int getTotal() {
        int totalExpense = 0;
        for (Expense expense : expenseMap.values()) {
            Category category = Category.getCategoryForID(expense.getCategory());
            if(!expense.isCash() && category.isInBudget() && (!expense.isRembourse() || expense.isRembourseCash())) {
                totalExpense += expense.getAmount();
            }
        }
        return totalExpense;
    }
}
