package sneiger.budget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Category {
    public static HashMap<Integer,Category> categoryMap = new HashMap<>();
    public static String CATEGORY_EDIT_EXTRA = "categoryEdit";
    private int id;             //id de reconnaissance de la catégorie
    private String name;        // nom de la catégorie, sert de clé
    private int amount;         // amount in cents
    private String color;       // couleur de la catégorie en HEX
    private boolean visible;    // si la catégorie est dans les graphes
    private boolean inBudget;  // si la catégorie est a compter dans le budget

    public Category(int id, String name, int amount, String color, boolean visible, boolean inBudget) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.color = color;
        this.visible = visible;
        this.inBudget = inBudget;
    }

    public static Category getCategoryForID(int passedCategoryID) {
        for (Category category : categoryMap.values())
        {
            if(category.getId() == passedCategoryID)
                return category;
        }
        return null;
    }

    public static List<Category> getVisibleCategories() {
        List<Category> visibleCategories = new ArrayList<>();
        for (Category category : categoryMap.values()) {
            if (category.isVisible() && category.isInBudget()) {
                visibleCategories.add(category);
            }
        }
        return visibleCategories;
    }

    public int getId() {return id;}

    public void setId(int id) {this.id = id;}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isVisible() {return visible;}

    public void setVisible(boolean visible) {this.visible = visible;}

    public boolean isInBudget() {return inBudget;}

    public void setInBudget(boolean inBudget) {this.inBudget = inBudget;}

    public static int getIdAutre() {
        for (Category category: categoryMap.values()) {
                if (category.getName().compareTo("Autre")==0) {
                return category.getId();
            }
        }
        return -1;
    }
}
