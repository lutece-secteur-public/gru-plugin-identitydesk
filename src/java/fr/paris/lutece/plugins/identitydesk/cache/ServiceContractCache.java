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
package fr.paris.lutece.plugins.identitydesk.cache;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.service.ServiceContractService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ServiceContractCache extends AbstractCacheableService
{
    private static final String SERVICE_NAME = "ServiceContractCache";

    private final String _currentClientCode = AppPropertiesService.getProperty( "identitydesk.default.client.code" );

    private final ServiceContractService _serviceContractService;
    private final List<String> _sortedAttributeKeyList = Arrays.asList( AppPropertiesService.getProperty( "identitydesk.attribute.order" ).split( "," ) );

    public ServiceContractCache( ServiceContractService scService )
    {
        _serviceContractService = scService;
        this.initCache( );
    }

    public void put( final String clientCode, final ServiceContractDto serviceContract )
    {
        if ( this.getKeys( ).contains( clientCode ) )
        {
            this.removeKey( clientCode );
        }
        this.putInCache( clientCode, serviceContract );
        AppLogService.debug( "ServiceContractDto added to cache: " + clientCode );
    }

    public void remove( final String clientCode )
    {
        if ( this.getKeys( ).contains( clientCode ) )
        {
            this.removeKey( clientCode );
        }

        AppLogService.debug( "ServiceContractDto removed from cache: " + clientCode );
    }

    public ServiceContractDto get( final String targetClientCode ) throws IdentityStoreException
    {
        ServiceContractDto serviceContract = (ServiceContractDto) this.getFromCache( targetClientCode );
        if ( serviceContract == null )
        {
            serviceContract = this.getFromAPI( targetClientCode );
            this.put( targetClientCode, serviceContract );
        }
        return serviceContract;
    }

    public ServiceContractDto getFromAPI( final String targetClientCode ) throws IdentityStoreException
    {
        final ServiceContractDto contract = _serviceContractService.getActiveServiceContract( targetClientCode, _currentClientCode,
                new RequestAuthor( "IdentityDesk_ServiceContractCache", AuthorType.application.name( ) ) ).getServiceContract( );
        sortServiceContractAttributes( contract );
        sortServiceContractAttributeCertifications( contract );
        return contract;
    }

    /**
     * sort attributes according to properties
     * 
     * @param contract
     */
    private void sortServiceContractAttributes( final ServiceContractDto contract )
    {
        if ( contract != null )
        {
            contract.getAttributeDefinitions( ).sort( ( a1, a2 ) -> {
                final int index1 = _sortedAttributeKeyList.indexOf( a1.getKeyName( ) );
                final int index2 = _sortedAttributeKeyList.indexOf( a2.getKeyName( ) );
                final Integer i1 = index1 == -1 ? 999 : index1;
                final Integer i2 = index2 == -1 ? 999 : index2;
                return i1.compareTo( i2 );
            } );
        }
    }

    /**
     * sort attributes certification process according by level
     * 
     * @param contract
     */
    private void sortServiceContractAttributeCertifications( final ServiceContractDto contract )
    {
        if ( contract != null )
        {
            contract.getAttributeDefinitions( )
                    .forEach( attr -> attr.getAttributeCertifications( ).sort( Comparator.comparing( c -> Integer.valueOf( c.getLevel( ) ) ) ) );
        }
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}
