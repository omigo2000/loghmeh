package loghmeh.repository.user;

import loghmeh.repository.mapper.IMapper;

import java.sql.SQLException;

public interface IUserMapper extends IMapper<UserDAO, String, String> {
    void addUserCredit(String userId, float amount) throws SQLException;
    boolean validateUser(String email, String password) throws SQLException;
}
