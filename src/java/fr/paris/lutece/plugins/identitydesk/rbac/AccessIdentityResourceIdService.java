/*
 * Copyright (c) 2002-2024, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.identitydesk.rbac;

import fr.paris.lutece.portal.service.rbac.Permission;
import fr.paris.lutece.portal.service.rbac.ResourceIdService;
import fr.paris.lutece.portal.service.rbac.ResourceType;
import fr.paris.lutece.portal.service.rbac.ResourceTypeManager;
import fr.paris.lutece.util.ReferenceList;

import java.util.Locale;

public class AccessIdentityResourceIdService extends ResourceIdService
{

    private static final String PLUGIN_NAME = "identitydesk";
    private static final String PROPERTY_LABEL_RESOURCE_TYPE = "identitydesk.rbac.access.identity.label";
    private static final String PROPERTY_LABEL_READ = "identitydesk.rbac.access.identity.permission.read";
    private static final String PROPERTY_LABEL_WRITE = "identitydesk.rbac.access.identity.permission.write";
    private static final String PROPERTY_LABEL_CREATE = "identitydesk.rbac.access.identity.permission.create";

    /**
     * {@inheritDoc}
     */
    @Override
    public void register( )
    {
        ResourceType rt = new ResourceType( );
        rt.setResourceIdServiceClass( AccessIdentityResourceIdService.class.getName( ) );
        rt.setPluginName( PLUGIN_NAME );
        rt.setResourceTypeKey( AccessIdentityResource.RESOURCE_TYPE );
        rt.setResourceTypeLabelKey( PROPERTY_LABEL_RESOURCE_TYPE );

        Permission permRead = new Permission( );
        permRead.setPermissionKey( AccessIdentityResource.PERMISSION_READ );
        permRead.setPermissionTitleKey( PROPERTY_LABEL_READ );
        rt.registerPermission( permRead );

        Permission permWrite = new Permission( );
        permWrite.setPermissionKey( AccessIdentityResource.PERMISSION_WRITE );
        permWrite.setPermissionTitleKey( PROPERTY_LABEL_WRITE );
        rt.registerPermission( permWrite );

        Permission permCreate = new Permission( );
        permCreate.setPermissionKey( AccessIdentityResource.PERMISSION_CREATE );
        permCreate.setPermissionTitleKey( PROPERTY_LABEL_CREATE );
        rt.registerPermission( permCreate );

        ResourceTypeManager.registerResourceType( rt );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReferenceList getResourceIdList( final Locale locale )
    {
        // No resources to control : return an empty list
        return new ReferenceList( );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle( final String s, final Locale locale )
    {
        // No resources to control : return an empty String
        return "";
    }
}
