package loghmeh.domain;


import loghmeh.configs.HashingConfig;
import loghmeh.domain.exceptions.*;
import loghmeh.repository.cart.CartDAO;
import loghmeh.repository.cart.CartRepository;
import loghmeh.repository.finalizedCart.FinalizedCartDAO;
import loghmeh.repository.finalizedCart.FinalizedCartRepository;
import loghmeh.repository.food.FoodDAO;
import loghmeh.repository.order.OrderDAO;
import loghmeh.repository.restaurant.RestaurantDAO;
import loghmeh.repository.user.UserDAO;
import loghmeh.repository.user.UserRepository;
import loghmeh.service.authentication.Authentication;
import loghmeh.service.authentication.GoogleAuthentication;
import loghmeh.utilities.JsonStringCreator;

import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.ArrayList;

public class UserManager {
    private static UserManager instance;

    private static final String SALT_FOR_HASH = HashingConfig.SALT_FOR_HASH;

    private UserRepository userRepository;

    private int cartIdGenerator;

    private UserManager() throws SQLException {
        this.userRepository = UserRepository.getInstance();
        this.cartIdGenerator = Math.max(FinalizedCartRepository.getInstance().getLastId(), CartRepository.getInstance().getLastId()) +1;
    }

    public static UserManager getInstance() throws SQLException {
        if (instance == null)
            instance = new UserManager();
        return instance;
    }

    private String hashGenerator(String name){
        try{
            MessageDigest digest = MessageDigest.getInstance(HashingConfig.HASH_FUNCTION);
            byte[] hash = digest.digest(name.getBytes(HashingConfig.HASH_CHARSET));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public void registerUser(String firstName, String lastName, String email, String password) throws DuplicateEmail {
        String hashedPassword = hashGenerator(SALT_FOR_HASH + password);
        this.userRepository.insertUser(firstName, lastName, email, hashedPassword);
    }

    private boolean validateUser(String email, String password){
        String hashedPassword = hashGenerator(SALT_FOR_HASH + password);
        return this.userRepository.validateUser(email, hashedPassword);
    }

    public String authenticateUser(String email, String password, boolean isGoogleAuth, String googleToken) throws SQLException, LoginFailure {
        if(!isGoogleAuth) {
            if (!UserManager.getInstance().validateUser(email, password)) {
                throw new LoginFailure(JsonStringCreator.msgCreator("رمز یا ایمیل وارد شده نادرست است"));
            }
            return Authentication.createToken(email);
        }
        String fetchedEmail = GoogleAuthentication.verifyGoogleToken(googleToken);
        if((fetchedEmail == null) || (!fetchedEmail.equals(email))){
            throw new LoginFailure(JsonStringCreator.msgCreator("توکن ارسالی درست نمی‌باشد"));
        }
        try {
            this.userRepository.findUser(fetchedEmail);
        }
        catch (SQLException e1){
            throw new LoginFailure(JsonStringCreator.msgCreator("کاربری با ایمیل درخواست شده ثبت‌نام نکرده‌است"));
        }
        return Authentication.createToken(fetchedEmail);
    }

    public UserDAO getUserById(String id) throws SQLException{
        return this.userRepository.findUser(id);
    }

    public void addCredit(String userId, float amount) throws NegativeCreditAmount, SQLException{
        if(amount <= 0)
            throw new NegativeCreditAmount(JsonStringCreator.msgCreator("برای افزایش اعتبار مقدار مثبتی را وارد نمایید"));
        this.userRepository.updateUserCredit(userId, amount);
    }

    public void addToCart(String userId, String foodName, int foodNum, String restaurantId, boolean isParty, boolean isNew) throws CartValidationException, RestaurantNotFound, FoodPartyExpiration, SQLException {

        CartManager cartManager = CartManager.getInstance();
        OrderManager.getInstance();
        float price;
        if(isParty){
            FoodDAO foodDAO = FoodpartyManager.getInstance().findFoodpartyById(restaurantId, foodName);
            if(foodDAO == null) {
                cartManager.deleteCartBeforeParty(restaurantId, foodName, this.cartIdGenerator);
                throw new RestaurantNotFound(JsonStringCreator.msgCreator("رستوران با شناسه درخواست‌شده، شامل غذای درخواست‌شده برای جشن غذا نمی‌باشد"));
            }
            if(foodDAO.getCount() < foodNum)
                throw new CartValidationException(JsonStringCreator.msgCreator("غذای درخواست‌شده از جشن غذا موجودی خواسته‌شده را ندارد."));
            price = foodDAO.getPrice();
        }
        else {
            RestaurantDAO restaurant = RestaurantManager.getInstance().getRestaurantById(restaurantId);
            if (restaurant == null)
                throw new RestaurantNotFound(JsonStringCreator.msgCreator("رستورانی با شناسه درخواست‌شده موجود نمی‌باشد"));
            FoodDAO food = FoodManager.getInstance().findFood(restaurantId, foodName);
            if(food == null)
                throw new CartValidationException(JsonStringCreator.msgCreator("رستوران مدنظر، شامل غذای درخواست‌شده نمی‌باشد"));
            price = food.getPrice();
        }
        CartDAO cart = cartManager.getCartByUserId(userId);
        if(cart != null) {
            cartManager.addToCart(cart.getCartId(), userId, restaurantId, foodName, foodNum, price, isParty, isNew);
        }
        else cartManager.addToCart(this.cartIdGenerator++, userId, restaurantId, foodName, foodNum, price, isParty, isNew);
    }

    public Object[] getCart(String userId) throws SQLException{
        CartDAO cart = CartManager.getInstance().getCartByUserId(userId);
        ArrayList<OrderDAO> orders = new ArrayList<>();
        if(cart != null)
            orders = OrderManager.getInstance().getOrdersOfCart(cart.getCartId());
        Object[] resultCart = new Object[2];
        resultCart[0] = cart;
        resultCart[1] = orders;
        return resultCart;
    }


    private boolean validateUserCredit(String userId, float price) throws SQLException{
        return this.userRepository.findUser(userId).getCredit() >= price;
    }

    public void finalizeOrder(String userId) throws SQLException, CartValidationException, FoodPartyExpiration{
        CartDAO cart = CartManager.getInstance().getCartByUserId(userId);
        if(cart == null)
            throw new CartValidationException(JsonStringCreator.msgCreator("سبد خریدی برای ثبت نهایی موجود نمی‌باشد"));

        ArrayList<OrderDAO> orders = CartManager.getInstance().finalizeOrder(cart.getCartId(), cart.getRestaurantId());


        float price = CartManager.getInstance().getCartPrice(orders);

        if(validateUserCredit(userId, price)){
            this.userRepository.updateUserCredit(userId, -price);
            this.cartIdGenerator++;
            FoodpartyManager.getInstance().updateFoodpartyCount(cart.getRestaurantId(), orders);
            FinalizedCartRepository.getInstance().addNewCart(cart);
            CartManager.getInstance().clearCart(cart.getCartId());
        }
        else
            throw new CartValidationException(JsonStringCreator.msgCreator("موجودی برای نهایی کردن سفارش کافی نمی‌باشد"));
    }

    public void deleteCart(String userId) throws SQLException, CartValidationException{
        CartManager cartManager = CartManager.getInstance();
        CartDAO cart = cartManager.getCartByUserId(userId);
        if(cart == null)
            throw new CartValidationException(JsonStringCreator.msgCreator("سبد خریدی برای جذف موجود نمی‌باشد"));
        cartManager.clearCartAndOrders(cart.getCartId());
    }

    public ArrayList<FinalizedCartDAO> getAllOrders(String userId) throws SQLException{
        ArrayList<FinalizedCartDAO> deliveredOrders = FinalizedCartRepository.getInstance().getDeliveredOrders(userId);
        ArrayList<FinalizedCartDAO> undeliveredOrders = FinalizedCartRepository.getInstance().getUndeliveredOrders(userId);
        deliveredOrders.addAll(undeliveredOrders);
        return deliveredOrders;
    }
}
