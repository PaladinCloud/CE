package com.tmobile.pacman.api.admin.factory;

import com.tmobile.pacman.api.admin.repository.service.AccountsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountFactory {
    @Autowired
    private List<AccountsService> serviceImpl;

    private static final Map<String, AccountsService> cache = new HashMap<>();

    @PostConstruct
    public void initCache() {
        for(AccountsService service : serviceImpl) {
            cache.put(service.serviceType(), service);
        }
    }

    public static AccountsService getService(String type) {
        AccountsService accountServiceImpl = cache.get(type);
        if(accountServiceImpl == null){
            throw new RuntimeException("Unknown accountServiceImpl type: " + type);
        }
        return accountServiceImpl;
    }
}
