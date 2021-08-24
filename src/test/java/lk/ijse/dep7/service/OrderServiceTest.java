package lk.ijse.dep7.service;

import lk.ijse.dep7.dbUtils.SingleConnectionDataSource;
import lk.ijse.dep7.exception.FailedOperationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.sql.Connection;
import java.sql.SQLException;

class OrderServiceTest {

    private OrderService orderService;
    private Connection connection;

    @BeforeEach
    void setUp() {
        SingleConnectionDataSource.init("jdbc:mysql://localhost:3306/dep7_backup_pos", "root", "mysql");
        Connection connection = SingleConnectionDataSource.getInstance().getConnection();
        this.orderService = new OrderService(connection);
    }

    @AfterEach
    void tearDown() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"OD001", "2021-08-23", "C002", "Dinusha", "Ar"})
    void searchOrders(String source) throws FailedOperationException {
        orderService.searchOrders(source).forEach(System.out::println);
        System.out.println("---------------------------------");
    }
}