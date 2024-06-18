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
package fr.paris.lutece.plugins.identitydesk.business;

import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.IdentityDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import org.apache.commons.collections.CollectionUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO used for the create and update UI.
 */
public class LocalIdentityDto
{
    private final String customerId;
    private final Timestamp lastUpdateDate;
    private final List<LocalAttributeDto> attributeList;
    private AccountCreationTask accountCreationTask;

    private LocalIdentityDto( )
    {
        this.customerId = null;
        this.attributeList = new ArrayList<>( );
        this.lastUpdateDate = null;
    }

    private LocalIdentityDto( final String customerId, Timestamp lastUpdate )
    {
        this.customerId = customerId;
        this.attributeList = new ArrayList<>( );
        this.lastUpdateDate = lastUpdate;
    }

    public String getCustomerId( )
    {
        return customerId;
    }

    public List<LocalAttributeDto> getAttributeList( )
    {
        return attributeList;
    }

    public AccountCreationTask getAccountCreationTask( )
    {
        return accountCreationTask;
    }

    public void setAccountCreationTask( AccountCreationTask accountCreationTask )
    {
        this.accountCreationTask = accountCreationTask;
    }

    /**
     * Static builder for update UI for an IdentityDto, with allowed certification process
     * 
     * @param identityDto
     * @param serviceContract
     * @return the IdentityDto
     */
    public static LocalIdentityDto from( final IdentityDto identityDto, final ServiceContractDto serviceContract )
    {
        if ( identityDto == null || serviceContract == null )
        {
            return null;
        }
        final LocalIdentityDto localIdentityDto = new LocalIdentityDto( identityDto.getCustomerId( ), identityDto.getLastUpdateDate( ) );

        // add allowed certification process from service contract
        serviceContract.getAttributeDefinitions( ).stream( ).filter( a -> a.getAttributeRight( ).isWritable( ) ).forEach( attrRef -> {
            final AttributeDto identityAttr = identityDto.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( attrRef.getKeyName( ) ) ).findFirst( )
                    .orElse( null );

            if ( CollectionUtils.isNotEmpty( attrRef.getAttributeCertifications( ) ) )
            {
                localIdentityDto.getAttributeList( ).add( LocalAttributeDto.from( identityAttr, attrRef ) );
            }
        } );

        localIdentityDto.setAccountCreationTask( AccountCreationTaskHome.get( identityDto.getCustomerId( ) ) );

        return localIdentityDto;
    }
}
