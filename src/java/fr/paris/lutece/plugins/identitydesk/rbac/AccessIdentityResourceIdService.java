package fr.paris.lutece.plugins.identitydesk.rbac;

import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.util.ReferenceList;

import java.util.Locale;

public class AccessIdentityResourceIdService extends ResourceIdService {

    private static final String PLUGIN_NAME = "identitydesk";
    private static final String PROPERTY_LABEL_RESOURCE_TYPE = "identitydesk.rbac.access.identity.label";
    private static final String PROPERTY_LABEL_READ = "identitydesk.rbac.access.identity.permission.read";
    private static final String PROPERTY_LABEL_WRITE = "identitydesk.rbac.access.identity.permission.write";

    /**
     * {@inheritDoc}
     */
    @Override
    public void register() {
        ResourceType rt = new ResourceType(  );
        rt.setResourceIdServiceClass( AccessIdentityResourceIdService .class.getName(  ) );
        rt.setPluginName(PLUGIN_NAME);
        rt.setResourceTypeKey( AccessIdentityResource.RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );

        Permission permRead = new Permission(  );
        permRead.setPermissionKey( AccessIdentityResource.PERMISSION_READ );
        permRead.setPermissionTitleKey( PROPERTY_LABEL_READ );
        rt.registerPermission( permRead );

        Permission permWrite = new Permission(  );
        permWrite.setPermissionKey( AccessIdentityResource.PERMISSION_WRITE );
        permWrite.setPermissionTitleKey( PROPERTY_LABEL_WRITE );
        rt.registerPermission( permWrite );

        ResourceTypeManager.registerResourceType(rt);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceList getResourceIdList(final Locale locale) {
        // No resources to control : return an empty list
        return new ReferenceList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle(final String s, final Locale locale) {
        // No resources to control : return an empty String
        return "";
    }
}
