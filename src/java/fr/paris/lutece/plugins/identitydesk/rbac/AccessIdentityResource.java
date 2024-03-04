package fr.paris.lutece.plugins.identitydesk.rbac;

import fr.paris.lutece.portal.service.rbac.RBACResource;

public class AccessIdentityResource implements RBACResource {

    // RBAC management
    public static final String RESOURCE_TYPE = "ACCESS_IDENTITY";

    // Perimissions
    public static final String PERMISSION_READ = "READ";
    public static final String PERMISSION_WRITE = "WRITE";

    @Override
    public String getResourceTypeCode() {
        return RESOURCE_TYPE;
    }

    @Override
    public String getResourceId() {
        return null;
    }
}
