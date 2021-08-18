package lk.ijse.dep7.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DBConnectionTest {

    @Test
    void getInstance() {
        assertEquals(DBConnection.getInstance(), DBConnection.getInstance());
    }

    @Test
    void getConnection() {
        assertNotNull(DBConnection.getInstance().getConnection());
    }
}