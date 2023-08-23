package nfelis.budget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Category {
    /** all categories of the database or only the ones in the budget if the percentage view is set */
    public static HashMap<Integer,Category> categoryMap = new HashMap<>();
    public static HashMap<Integer,Category> categoryNonMap = new HashMap<>();
    // this map should be non empty only if the percentage view is on
    public final static String CATEGORY_EDIT_EXTRA = "categoryEdit";
    // value to use if one wants to modify a category
    private int id;             //id de reconnaissance de la catégorie
    private String name;        // nom de la catégorie, sert de clé
    private int amount;         // amount in cents
    private String color;       // couleur de la catégorie en HEX
    private boolean visible;    // si la catégorie est dans les graphes
    private boolean inBudget;   // si la catégorie est a compter dans le budget
   // private Date dateBegin,dateEnd;     // si la catégorie est pour un budget sur une période

    /** Constructor of the category */
    public Category(int id, String name, int amount, String color, boolean visible, boolean inBudget) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.color = color;
        this.visible = visible;
        this.inBudget = inBudget;
    }

    /** Returns the category with corresponding ID, returns null if none can be found
     * @param   passedCategoryID    the id we try to find the corresponding category for
     * @return                      the category at corresponding id
     * */
    public static Category getCategoryForID(int passedCategoryID) {
        // the algorithm goes through all the maps and stops when it finds the right id
        for (Category category : categoryMap.values())
        {
            if(category.getId() == passedCategoryID)
                return category;
        }
        for (Category category : categoryNonMap.values()) {
            if(category.getId() == passedCategoryID)
                return category;
        }
        //otherwise it returns a null
        return null;
    }

    /** Returns a list of all the categories that are set as visible
     * @return      the list of categories that have the attribute isVisible and isInBudget at true*/
    public static List<Category> getVisibleCategories() {
        //initialise the array list
        List<Category> visibleCategories = new ArrayList<>();
        for (Category category : categoryMap.values()) {
            // if the category is visible and in the budget it is added
            if (category.isVisible() && category.isInBudget()) {
                visibleCategories.add(category);
            }
        }
        //should not be usefull but in case
        for (Category category : categoryNonMap.values()) {
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

    /** returns the id corresponding to the "Autre" category. It shouldn't change, but in case
     * @return      the id of the category "Autre", it should always exist considering you can't delete it
     * */
    public static int getIdAutre() {
        // go through all the categories until find one called "Autre"
        for (Category category: categoryMap.values()) {
                if (category.getName().compareTo("Autre")==0) {
                return category.getId();
            }
        }
        for (Category category: categoryNonMap.values()) {
            if (category.getName().compareTo("Autre")==0) {
                return category.getId();
            }
        }
        //return -1 if doesn't find
        return -1;
    }
}
