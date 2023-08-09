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
package fr.paris.lutece.plugins.identitydesk.business;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;

/**
 * DTO used for the create and update UI.
 */
public class IdentityDto
{
    private final String customerId;
    private final Timestamp lastUpdateDate;
    private final List<AttributeDto> attributeList;

    private IdentityDto( )
    {
        this.customerId = null;
        this.attributeList = new ArrayList<>( );
        this.lastUpdateDate = null;
    }

    private IdentityDto( final String customerId, Timestamp lastUpdate )
    {
        this.customerId = customerId;
        this.attributeList = new ArrayList<>( );
        this.lastUpdateDate = lastUpdate;
    }

    public String getCustomerId( )
    {
        return customerId;
    }

    public List<AttributeDto> getAttributeList( )
    {
        return attributeList;
    }

    /**
     * Static builder for update UI for an IdentityDto, with allowed certification process
     * 
     * @param qualifiedIdentity
     * @param serviceContract
     * @return the IdentityDto
     */
    public static IdentityDto from( final QualifiedIdentity qualifiedIdentity, final ServiceContractDto serviceContract )
    {
        if ( qualifiedIdentity == null || serviceContract == null )
        {
            return null;
        }
        IdentityDto identityDto = new IdentityDto( qualifiedIdentity.getCustomerId( ), qualifiedIdentity.getLastUpdateDate( ) );

        // add allowed certification process from service contract
        serviceContract.getAttributeDefinitions( ).stream( ).filter( a -> a.getAttributeRight( ).isWritable( ) ).forEach( attrRef -> {
            final fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute identityAttr = qualifiedIdentity.getAttributes( ).stream( )
                    .filter( a -> a.getKey( ).equals( attrRef.getKeyName( ) ) ).findFirst( ).orElse( null );

            if ( CollectionUtils.isNotEmpty( attrRef.getAttributeCertifications( ) ) )
            {
                identityDto.getAttributeList( ).add( AttributeDto.from( identityAttr, attrRef ) );
            }
        } );

        return identityDto;
    }

    /**
     * Static builder for create UI.
     * 
     * get all available certification process filtered by the attributes rights and certificate min level requirements
     * 
     * @param identity
     * @param serviceContract
     * @return the IdentityDto
     */
    public static IdentityDto from( final Identity identity, final ServiceContractDto serviceContract )
    {
        if ( identity == null || serviceContract == null )
        {
            return null;
        }
        final IdentityDto identityDto = new IdentityDto( );
        serviceContract.getAttributeDefinitions( ).stream( ).filter( a -> a.getAttributeRight( ).isWritable( ) ).forEach( attrDef -> {
            final CertifiedAttribute identityAttr = identity.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( attrDef.getKeyName( ) ) ).findFirst( )
                    .orElse( null );
            if ( CollectionUtils.isNotEmpty( attrDef.getAttributeCertifications( ) ) )
            {
                identityDto.getAttributeList( ).add( AttributeDto.from( identityAttr, attrDef ) );
            }
        } );

        return identityDto;
    }

}
