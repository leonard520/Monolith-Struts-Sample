package com.skishop.service.address;

import com.skishop.dao.address.UserAddressDao;
import com.skishop.domain.address.Address;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock private UserAddressDao userAddressDao;
    @InjectMocks private AddressService addressService;

    @Test
    void listByUserId_delegatesToDao() {
        Address addr = new Address();
        addr.setId("a-1");
        when(userAddressDao.listByUserId("u-1")).thenReturn(List.of(addr));

        List<Address> result = addressService.listByUserId("u-1");
        assertEquals(1, result.size());
        assertEquals("a-1", result.get(0).getId());
        verify(userAddressDao).listByUserId("u-1");
    }

    @Test
    void findById_delegatesToDao() {
        Address addr = new Address();
        addr.setId("a-1");
        when(userAddressDao.findById("a-1")).thenReturn(addr);

        Address result = addressService.findById("a-1");
        assertNotNull(result);
        assertEquals("a-1", result.getId());
        verify(userAddressDao).findById("a-1");
    }

    @Test
    void save_delegatesToDao() {
        Address addr = new Address();
        addr.setId("a-1");
        addressService.save(addr);
        verify(userAddressDao).save(addr);
    }

    @Test
    void countByUserId_returnsCorrectCount() {
        when(userAddressDao.listByUserId("u-1")).thenReturn(List.of(new Address(), new Address(), new Address()));
        int count = addressService.countByUserId("u-1");
        assertEquals(3, count);
    }

    @Test
    void countByUserId_returnsZeroWhenNull() {
        when(userAddressDao.listByUserId("u-1")).thenReturn(null);
        int count = addressService.countByUserId("u-1");
        assertEquals(0, count);
    }
}
