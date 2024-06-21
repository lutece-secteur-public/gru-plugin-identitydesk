package fr.paris.lutece.plugins.identitydesk.business;

import java.util.List;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.history.AttributeChange;

public class LocalHistoryDto {
    private long modificationTime;
    private IdentityDto identity;
    private List<AttributeChange> attributeChanges;

    public LocalHistoryDto(long modificationTime, IdentityDto identity, List<AttributeChange> attributeChanges) {
        this.modificationTime = modificationTime;
        this.identity = identity;
        this.attributeChanges = attributeChanges;
    }

    public long getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }

    public IdentityDto getIdentity() {
        return identity;
    }

    public void setIdentity(IdentityDto identity) {
        this.identity = identity;
    }

    public List<AttributeChange> getAttributeChanges() {
        return attributeChanges;
    }

    public void setAttributeChanges(List<AttributeChange> attributeChanges) {
        this.attributeChanges = attributeChanges;
    }
}