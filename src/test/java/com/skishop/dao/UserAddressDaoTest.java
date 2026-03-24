package com.skishop.dao;

import com.skishop.dao.address.UserAddressDao;
import com.skishop.domain.address.Address;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.junit.jupiter.api.Assertions.*;

class UserAddressDaoTest extends DaoTestBase {

    @Autowired
    private UserAddressDao userAddressDao;

    @Test
    void testListByUserId() {
        List<Address> addresses = userAddressDao.listByUserId("u-1");
        assertNotNull(addresses);
    }
}
