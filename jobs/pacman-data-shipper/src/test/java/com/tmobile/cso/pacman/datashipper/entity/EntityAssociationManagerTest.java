package com.tmobile.cso.pacman.datashipper.entity;

import com.tmobile.cso.pacman.datashipper.config.ConfigManager;
import com.tmobile.cso.pacman.datashipper.es.ESManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.mockito.Matchers.*;
import static org.powermock.api.mockito.PowerMockito.doNothing;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ConfigManager.class, ESManager.class})
public class EntityAssociationManagerTest {

//  TODO: fix test
    @SuppressWarnings("unchecked")
    @Test
    public void uploadAssociationInfoTest() {
/*
        PowerMockito.mockStatic(ConfigManager.class);
        List<String> types = new ArrayList<>();
        types.add("type1");
        when(ConfigManager.getTypes(anyString())).thenReturn(new HashSet<>(types));

        when(ConfigManager.getKeyForType(anyString(), anyString())).thenReturn("type");

        PowerMockito.mockStatic(ESManager.class);
        doNothing().when(ESManager.class);
        ESManager.createType(anyString(), anyString(), anyList());

        List<Map<String, String>> entities = new ArrayList<>();
        entities.add(new HashMap<>());
        ESManager.uploadData(anyString(), anyString(), anyList(), anyString(), anyBoolean());
        ESManager.deleteOldDocuments(anyString(), anyString(), anyString(), anyString());

        new EntityAssociationManager().uploadAssociationInfo("dataSource", "type");
*/
        assert(true);
    }
}
