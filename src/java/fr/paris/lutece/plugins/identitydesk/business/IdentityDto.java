package fr.paris.lutece.plugins.identitydesk.business;

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
    private final List<AttributeDto> attributeList;

    private IdentityDto( )
    {
        this.customerId = null;
        this.attributeList = new ArrayList<>( );
    }

    private IdentityDto( final String customerId )
    {
        this.customerId = customerId;
        this.attributeList = new ArrayList<>( );
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
        final IdentityDto identityDto = new IdentityDto( qualifiedIdentity.getCustomerId( ) );

        // add allowed certification process from service contract
        serviceContract.getAttributeDefinitions( ).stream( ).filter( a -> a.getAttributeRight( ).isWritable( ) )
        	.forEach( attrRef -> {
        		final fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute identityAttr 
            		= qualifiedIdentity.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( attrRef.getKeyName( ) ) ).findFirst( ).orElse( null );
	            
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
        serviceContract.getAttributeDefinitions( ).stream( ).filter( a -> a.getAttributeRight( ).isWritable( ) )
        .forEach( attrDef -> {
            final CertifiedAttribute identityAttr = identity.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( attrDef.getKeyName( ) ) )
                    .findFirst( ).orElse( null );
            if ( CollectionUtils.isNotEmpty( attrDef.getAttributeCertifications( ) ) )
            {
                identityDto.getAttributeList( ).add( AttributeDto.from( identityAttr, attrDef ) );
            }
        } );

        return identityDto;
    }

}
