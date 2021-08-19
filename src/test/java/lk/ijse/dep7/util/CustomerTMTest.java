package lk.ijse.dep7.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class CustomerTMTest {

    @Test
    void compareTo() {
        List<CustomerTM> customers = new ArrayList<>();
        CustomerTM c001 = new CustomerTM("C001", "Kasun", "Galle");
        CustomerTM c001Cloned = new CustomerTM("C001", "ClonedKasun", "Galle");
        CustomerTM c005 = new CustomerTM("C005", "Nuwan", "Galle");
        CustomerTM c008 = new CustomerTM("C008", "Ruwan", "Galle");
        customers.add(c008);
        customers.add(c001);
        customers.add(c005);
        customers.add(c001Cloned);
        Collections.sort(customers);
        customers.forEach(System.out::println);
    }
}