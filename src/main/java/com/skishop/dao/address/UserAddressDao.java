package com.skishop.dao.address;

import com.skishop.domain.address.Address;
import java.util.List;

public interface UserAddressDao {
  List<Address> listByUserId(String userId);

  Address findById(String id);

  void save(Address address);
}
