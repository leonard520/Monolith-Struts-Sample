package com.skishop.service.address;

import com.skishop.dao.address.UserAddressDao;
import com.skishop.domain.address.Address;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AddressService {
    private final UserAddressDao userAddressDao;

    public AddressService(UserAddressDao userAddressDao) {
        this.userAddressDao = userAddressDao;
    }

    public List<Address> listByUserId(String userId) {
        return userAddressDao.listByUserId(userId);
    }

    public Address findById(String id) {
        return userAddressDao.findById(id);
    }

    public void save(Address address) {
        userAddressDao.save(address);
    }

    public int countByUserId(String userId) {
        List<Address> list = userAddressDao.listByUserId(userId);
        return list != null ? list.size() : 0;
    }
}
