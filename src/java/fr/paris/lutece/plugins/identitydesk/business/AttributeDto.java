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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeDefinitionDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.CertificationProcessus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;

public class AttributeDto
{
    private String _strKey;
    private String name;
    private String description;
    private String value;
    private String certifier;
    private Integer certificationLevel;
    private Date certificationDate;
    private boolean mandatory;
    private List<CertificationProcessus> allowedCertificationList;

    private AttributeDto( final String key, final String name, final String description, final boolean mandatory )
    {
        this._strKey = key;
        this.name = name;
        this.description = description;
        this.value = null;
        this.certifier = null;
        this.certificationLevel = null;
        this.certificationDate = null;
        this.mandatory = mandatory;
        this.allowedCertificationList = new ArrayList<>( );
    }

    private AttributeDto( final String key, final String name, final String description, final String value, final String certifier,
            final Integer certificationLevel, final Date certificationDate, final boolean mandatory )
    {
        this._strKey = key;
        this.value = value;
        this.name = name;
        this.description = description;
        this.certifier = certifier;
        this.certificationLevel = certificationLevel;
        this.certificationDate = certificationDate;
        this.mandatory = mandatory;
        this.allowedCertificationList = new ArrayList<>( );
    }

    public String getKey( )
    {
        return _strKey;
    }

    public String getName( )
    {
        return name;
    }

    public String getDescription( )
    {
        return description;
    }

    public String getValue( )
    {
        return value;
    }

    public String getCertifier( )
    {
        return certifier;
    }

    public Integer getCertificationLevel( )
    {
        return certificationLevel;
    }

    public Date getCertificationDate( )
    {
        return certificationDate;
    }

    public boolean isMandatory( )
    {
        return mandatory;
    }

    public List<CertificationProcessus> getAllowedCertificationList( )
    {
        return allowedCertificationList;
    }

    public boolean isUpdatable( )
    {
        return !allowedCertificationList.isEmpty( );
    }

    public void setValue( String strValue )
    {
        this.value = strValue;
    }

    public void setKey( String strKey )
    {
        this._strKey = strKey;
    }

    public void setCertifier( String strCert )
    {
        this.certifier = strCert;
    }

    public void setCertificationLevel( int ilevel )
    {
        this.certificationLevel = ilevel;
    }

    public void setCertificationDate( Date dateCert )
    {
        this.certificationDate = dateCert;
    }

    /**
     * Static builder for update UI.
     * 
     * @param certifiedAttribute
     * @param attributeDefinition
     * @return
     */
    public static AttributeDto from( final fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute certifiedAttribute,
            final AttributeDefinitionDto attributeDefinition )
    {
        final AttributeDto attributeDto = buildEmptyGenericAttributeDto( attributeDefinition );

        if ( certifiedAttribute == null )
        {
            return attributeDto;
        }

        attributeDto.setKey( certifiedAttribute.getKey( ) );
        attributeDto.setValue( certifiedAttribute.getValue( ) );
        attributeDto.setCertifier( certifiedAttribute.getCertifier( ) );
        attributeDto.setCertificationLevel( certifiedAttribute.getCertificationLevel( ) );
        attributeDto.setCertificationDate( certifiedAttribute.getCertificationDate( ) );

        // filter allowed certification list according to requirement min level
        filterAllowedCertificates( attributeDto, attributeDefinition );

        return attributeDto;
    }

    /**
     * Static builder for create UI.
     * 
     * @param certifiedAttribute
     * @param attributeDefinition
     * @return
     */
    public static AttributeDto from( final CertifiedAttribute certifiedAttribute, final AttributeDefinitionDto attributeDefinition )
    {

        final AttributeDto attributeDto = buildEmptyGenericAttributeDto( attributeDefinition );

        if ( certifiedAttribute == null )
        {
            return attributeDto;
        }
        attributeDto.setCertifier( certifiedAttribute.getCertificationProcess( ) );
        attributeDto.setValue( certifiedAttribute.getValue( ) );

        // filter allowed certification list according to requirement min level
        filterAllowedCertificates( attributeDto, attributeDefinition );

        return attributeDto;
    }

    /**
     * Build generic AttributeDto from the AttributeDefinition of current service contract : - get attribute Name, key, description, and isMandatory property -
     * add available certification process list - make updatable the AttributeDto if there is at least one certification process available
     * 
     * @param attributeDefinition
     * @return the AttributeDto
     */
    private static AttributeDto buildEmptyGenericAttributeDto( final AttributeDefinitionDto attributeDefinition )
    {
        final AttributeDto attributeDto = new AttributeDto( attributeDefinition.getKeyName( ), attributeDefinition.getName( ),
                attributeDefinition.getDescription( ), attributeDefinition.getAttributeRight( ).isMandatory( ) );
        attributeDto.getAllowedCertificationList( ).addAll( attributeDefinition.getAttributeCertifications( ) );

        return attributeDto;
    }

    /**
     * filter allowed certification list according to current certification and requirement min level
     * 
     * @param attributeDto
     * @param attributeDefinition
     */
    private static void filterAllowedCertificates( AttributeDto attributeDto, AttributeDefinitionDto attributeDefinition )
    {

        if ( CollectionUtils.isEmpty( attributeDto.getAllowedCertificationList( ) ) )
            return;

        attributeDto.getAllowedCertificationList( ).removeIf( cert -> {

            // certification process of level lower than current certification are not allowed
            if ( attributeDto.getCertificationLevel( ) != null && attributeDto.getCertificationLevel( ) > 0
                    && Integer.parseInt( cert.getLevel( ) ) < attributeDto.getCertificationLevel( ) )
                return true;

            // certification process of level lower than min level required are not allowed
            if ( attributeDefinition.getAttributeRequirement( ) != null
                    && Integer.parseInt( cert.getLevel( ) ) < Integer.parseInt( attributeDefinition.getAttributeRequirement( ).getLevel( ) ) )
                return true;

            // otherwise keep the process
            return false;
        } );

    }

}
