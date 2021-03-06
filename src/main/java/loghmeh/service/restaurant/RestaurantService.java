package loghmeh.service.restaurant;

import com.fasterxml.jackson.databind.ObjectMapper;
import loghmeh.domain.FoodManager;
import loghmeh.domain.FoodpartyManager;
import loghmeh.domain.RestaurantManager;
import loghmeh.domain.exceptions.RestaurantNotFound;
import loghmeh.repository.food.FoodDAO;
import loghmeh.repository.restaurant.RestaurantDAO;
import loghmeh.service.restaurant.response.RestaurantDTO;
import loghmeh.utilities.ConvertDAOToDTO;
import loghmeh.utilities.JsonStringCreator;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

@RestController
public class RestaurantService {
    ObjectMapper mapper = new ObjectMapper();

    @RequestMapping(value = "/v1/restaurants", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String getRestaurants(@RequestParam(value = "pageNum") int pageNumber, @RequestParam(value = "pageSize") int pageSize) throws SQLException, IOException {
        RestaurantManager restaurantManager = RestaurantManager.getInstance();
        ArrayList<RestaurantDAO> restaurants = restaurantManager.getRestaurants(pageNumber, pageSize);
        return mapper.writeValueAsString(ConvertDAOToDTO.restaurantDAO_DTO(restaurants));
    }

    @RequestMapping(value = "/v1/restaurants/{restaurantId}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String getRestaurant(@PathVariable(value = "restaurantId") String restaurantId) throws IOException, SQLException, RestaurantNotFound {
        RestaurantManager restaurantManager = RestaurantManager.getInstance();
        FoodManager foodManager = FoodManager.getInstance();
        RestaurantDAO restaurant = restaurantManager.getRestaurantById(restaurantId);
        ArrayList<FoodDAO> foods = foodManager.getFoods(restaurantId);
        return mapper.writeValueAsString(ConvertDAOToDTO.singleRestaurantDAO_DTO(restaurant, foods));
    }

    @RequestMapping(value = "/v1/foodparty", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String getFoodParty() throws IOException, SQLException {
        FoodpartyManager foodpartyManager = FoodpartyManager.getInstance();
        foodpartyManager.setupScheduler();
        try{
            TimeUnit.SECONDS.sleep(5);
        }catch (InterruptedException e1){
            e1.printStackTrace();
        }
        double remainingTime = foodpartyManager.getFoodpartyRemainingTime();
        ArrayList<FoodDAO> foods = foodpartyManager.getParty();
        String foodpartyStr = mapper.writeValueAsString(ConvertDAOToDTO.foodpartyDAO_DTO(foods));
        return JsonStringCreator.foodpartyJson(mapper.readTree(foodpartyStr), remainingTime);
    }

    @RequestMapping(value = "/v1/search", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public String searchRestaurants(@RequestParam(value = "restaurantName", required = false) String restaurantName, @RequestParam(value = "foodName", required = false) String foodName,
                                    @RequestParam(value = "pageNum") int pageNumber, @RequestParam(value = "pageSize") int pageSize) throws IOException, SQLException {
        if((restaurantName == null) && (foodName == null))
            return JsonStringCreator.msgCreator("هیچ‌کدام از فیلد‌های جست‌و‌جو تکمیل نشده‌اند");
        ArrayList<RestaurantDAO> restaurants = RestaurantManager.getInstance().searchRestaurants(restaurantName, foodName, pageNumber, pageSize);
        return mapper.writeValueAsString(ConvertDAOToDTO.restaurantDAO_DTO(restaurants));
    }

}