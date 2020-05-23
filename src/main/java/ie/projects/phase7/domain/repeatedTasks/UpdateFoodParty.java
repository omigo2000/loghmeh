package ie.projects.phase7.domain.repeatedTasks;

import ie.projects.phase7.domain.FoodpartyManager;
import ie.projects.phase7.domain.LoghmehManger;
import ie.projects.phase7.domain.foreignServiceObjects.Restaurant;
import ie.projects.phase7.repository.food.FoodRepository;
import ie.projects.phase7.repository.foodparty.FoodpartyRepository;
import ie.projects.phase7.repository.restaurant.RestaurantRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TimerTask;

public class UpdateFoodParty extends TimerTask {
    public void run() {
        try {
            FoodpartyManager foodpartyManager = FoodpartyManager.getInstance();
            foodpartyManager.deleteTable();
            foodpartyManager.createTable();
            ArrayList<Restaurant> restaurants = LoghmehManger.getInstance().getNewRestaurants(true);
            RestaurantRepository.getInstance().addRestaurants(restaurants);
            FoodRepository.getInstance().addFoods(restaurants, true);
            FoodpartyRepository.getInstance().addParty(restaurants);
            foodpartyManager.setLastFoodpartyUpdateTime(System.currentTimeMillis());
        }
        catch (SQLException e1){
            System.out.println("Can't update foodparty table");
            e1.printStackTrace();
        }
    }
}