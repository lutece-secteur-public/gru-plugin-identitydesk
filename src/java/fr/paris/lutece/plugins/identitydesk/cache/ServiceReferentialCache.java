package fr.paris.lutece.plugins.identitydesk.cache;

/*
 * Copyright (c) 2002-2023, City of Paris
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

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.referentiel.AttributeCertificationProcessusDto;
import fr.paris.lutece.plugins.identitystore.v3.web.service.ReferentialService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.cache.AbstractCacheableService;

import org.apache.log4j.Logger;
import java.util.List;

public class ServiceReferentialCache extends AbstractCacheableService
{
    private static final Logger LOGGER = Logger.getLogger( ServiceReferentialCache.class );
    private static final String SERVICE_NAME = "ReferentialCacheService";
    private ReferentialService _referentialService;

    public ServiceReferentialCache( ReferentialService srService )
    {
        _referentialService = srService;
        this.initCache( );
    }

    public void put( final String clientCode, final List<AttributeCertificationProcessusDto> referential )
    {
        if ( this.getKeys( ).contains( clientCode + "_referential" ) )
        {
            this.removeKey( clientCode + "_referential" );
        }
        this.putInCache( clientCode + "_referential", referential );
        LOGGER.info( "Referential added to cache: " + clientCode + "_referential" );
    }

    public void remove( final String clientCode )
    {
        if ( this.getKeys( ).contains( clientCode + "_referential" ) )
        {
            this.removeKey( clientCode + "_referential" );
        }

        LOGGER.info( "Referential removed from cache: " + clientCode + "_referential" );
    }

    public List<AttributeCertificationProcessusDto> get( final String clientCode ) throws IdentityStoreException
    {
        List<AttributeCertificationProcessusDto> processusDtos = _referentialService
                .getProcessList( clientCode, new RequestAuthor( "IdentityDesk_ServiceReferentialCache", AuthorType.application.name( ) ) ).getProcessus( );
        this.put( clientCode + "_referential", processusDtos );
        return processusDtos;
    }

    @Override
    public String getName( )
    {
        return SERVICE_NAME;
    }
}
