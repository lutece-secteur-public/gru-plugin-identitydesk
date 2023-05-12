package fr.paris.lutece.plugins.identitydesk.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeDefinitionDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeRequirement;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.CertificationProcessus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;

public class AttributeDto
{
    private final String key;
    private final String name;
    private final String description;
    private final String value;
    private final String certifier;
    private final Integer certificationLevel;
    private final Date certificationDate;
    private final boolean mandatory;
    private final List<CertificationProcessus> allowedCertificationList;
    private boolean updatable = false;

    private AttributeDto( final String key, final String name, final String description, final boolean mandatory )
    {
        this.key = key;
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
        this.key = key;
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
        return key;
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
        return updatable;
    }

    public void setUpdatable( boolean updatable )
    {
        this.updatable = updatable;
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
        if ( certifiedAttribute == null )
        {
            return empty( attributeDefinition );
        }
        final AttributeDto attributeDto = new AttributeDto( certifiedAttribute.getKey( ), attributeDefinition.getName( ),
                attributeDefinition.getDescription( ), certifiedAttribute.getValue( ), certifiedAttribute.getCertifier( ),
                certifiedAttribute.getCertificationLevel( ), certifiedAttribute.getCertificationDate( ),
                attributeDefinition.getAttributeRequirement( ) != null );
        attributeDto.getAllowedCertificationList( ).addAll( attributeDefinition.getAttributeCertifications( ).stream( ).filter( cert -> {
            final int allowedLevel = cert.getLevel( ) != null ? Integer.parseInt( cert.getLevel( ) ) : 0;
            final int currentLevel = certifiedAttribute.getCertificationLevel( ) != null ? certifiedAttribute.getCertificationLevel( ) : 0;

            final AttributeRequirement requirement = attributeDefinition.getAttributeRequirement( );
            boolean meetRequirement = true;
            if ( requirement != null && requirement.getLevel( ) != null )
            {
                meetRequirement = allowedLevel >= Integer.parseInt( requirement.getLevel( ) );
            }
            attributeDto.setUpdatable( attributeDto.isUpdatable( ) || ( allowedLevel >= currentLevel && meetRequirement ) );
            return allowedLevel >= currentLevel && meetRequirement;
        } ).collect( Collectors.toList( ) ) );

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
        if ( certifiedAttribute == null )
        {
            return empty( attributeDefinition );
        }
        final AttributeDto attributeDto = new AttributeDto( certifiedAttribute.getKey( ), attributeDefinition.getName( ),
                attributeDefinition.getDescription( ), certifiedAttribute.getValue( ), null, null, null,
                attributeDefinition.getAttributeRequirement( ) != null );

        attributeDto.getAllowedCertificationList( ).addAll( attributeDefinition.getAttributeCertifications( ).stream( ).filter( cert -> {
            final int allowedLevel = cert.getLevel( ) != null ? Integer.parseInt( cert.getLevel( ) ) : 0;

            final AttributeRequirement requirement = attributeDefinition.getAttributeRequirement( );
            boolean meetRequirement = false;
            if ( requirement != null && requirement.getLevel( ) != null )
            {
                meetRequirement = allowedLevel >= Integer.parseInt( requirement.getLevel( ) );
            }
            attributeDto.setUpdatable( attributeDto.isUpdatable( ) && meetRequirement );
            return meetRequirement;
        } ).collect( Collectors.toList( ) ) );

        return attributeDto;
    }

    private static AttributeDto empty( final AttributeDefinitionDto attributeDefinition )
    {
        final AttributeDto attributeDto = new AttributeDto( attributeDefinition.getKeyName( ), attributeDefinition.getName( ),
                attributeDefinition.getDescription( ), attributeDefinition.getAttributeRequirement( ) != null );
        attributeDto.getAllowedCertificationList( ).addAll( attributeDefinition.getAttributeCertifications( ) );
        attributeDto.setUpdatable( !attributeDto.getAllowedCertificationList( ).isEmpty( ) );
        return attributeDto;
    }
}

