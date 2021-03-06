package loghmeh.repository.cart;

import loghmeh.repository.mapper.IMapper;

import java.sql.SQLException;

public interface ICartMapper extends IMapper<CartDAO, Integer, String> {
    boolean checkRestaurantEqualityForCart(int cartId, String restaurantId) throws SQLException;
    int getMaxId() throws SQLException;
}