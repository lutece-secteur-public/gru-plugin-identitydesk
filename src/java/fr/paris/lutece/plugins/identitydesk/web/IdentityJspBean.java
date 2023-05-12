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
package fr.paris.lutece.plugins.identitydesk.web;

import fr.paris.lutece.plugins.identitydesk.business.IdentityDto;
import fr.paris.lutece.plugins.identitydesk.cache.ServiceContractCache;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AttributeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.AuthorType;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.common.RequestAuthor;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeDefinitionDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.AttributeRequirement;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.CertificationProcessus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.contract.ServiceContractDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.CertifiedAttribute;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.Identity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.crud.IdentityChangeStatus;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchRequest;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.IdentitySearchResponse;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.QualifiedIdentity;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchAttributeDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.SearchDto;
import fr.paris.lutece.plugins.identitystore.v3.web.rs.util.Constants;
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppLogService;
import fr.paris.lutece.portal.service.util.AppPropertiesService;
import fr.paris.lutece.portal.util.mvc.admin.annotations.Controller;
import fr.paris.lutece.portal.util.mvc.commons.annotations.Action;
import fr.paris.lutece.portal.util.mvc.commons.annotations.View;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class provides the user interface to manage Identity features ( manage, create, modify, remove )
 */
@Controller( controllerJsp = "ManageIdentities.jsp", controllerPath = "jsp/admin/plugins/identitydesk/", right = "IDENTITYDESK_MANAGEMENT" )
public class IdentityJspBean extends ManageIdentitiesJspBean
{
    private static final long serialVersionUID = 6053504380426222888L;
    // Templates
    private static final String TEMPLATE_SEARCH_IDENTITIES = "/admin/plugins/identitydesk/search_identities.html";
    private static final String TEMPLATE_CREATE_IDENTITY = "/admin/plugins/identitydesk/create_identity.html";
    private static final String TEMPLATE_MODIFY_IDENTITY = "/admin/plugins/identitydesk/modify_identity.html";

    // Messages
    private static final String MESSAGE_GET_IDENTITY_ERROR = "identitydesk.message.get_identity.error";
    private static final String MESSAGE_SEARCH_IDENTITY_NORESULT = "identitydesk.message.search_identity.noresult";
    private static final String MESSAGE_SEARCH_IDENTITY_ERROR = "identitydesk.message.search_identity.error";
    private static final String MESSAGE_IDENTITY_MUSTSELECTCERTIFICATION = "identitydesk.message.identity.mustselectcertification";
    private static final String MESSAGE_CREATE_IDENTITY_SUCCESS = "identitydesk.message.create_identity.success";
    private static final String MESSAGE_CREATE_IDENTITY_ERROR = "identitydesk.message.create_identity.error";
    private static final String MESSAGE_UPDATE_IDENTITY_NOCHANGE = "identitydesk.message.update_identity.nochange";
    private static final String MESSAGE_UPDATE_IDENTITY_SUCCESS = "identitydesk.message.update_identity.success";
    private static final String MESSAGE_UPDATE_IDENTITY_ERROR = "identitydesk.message.update_identity.error";
    private static final String MESSAGE_GET_SERVICE_CONTRACT_ERROR = "identitydesk.message.get_service_contract.error";
    private static final String MESSAGE_SEARCH_IDENTITY_REQUIREDFIELD = "identitydesk.message.search_identity.requiredfield";

    // Properties for page titles
    private static final String PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES = "identitydesk.manage_identities.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_MODIFY_IDENTITY = "identitydesk.modify_identity.pageTitle";
    private static final String PROPERTY_PAGE_TITLE_CREATE_IDENTITY = "identitydesk.create_identity.pageTitle";

    // Constants
    private static final String SEARCH_PARAMETER_PREFIX = "search_";
    private static final String ATTR_CERT_SUFFIX = "-certif";
    private static final String NEW_SEARCH_PARAMETER = "new_search";

    // Markers
    private static final String MARK_IDENTITY_LIST = "identity_list";
    private static final String MARK_IDENTITY = "identity";
    private static final String MARK_SERVICE_CONTRACT = "service_contract";
    private static final String MARK_QUERY_SEARCH_ATTRIBUTES = "query_search_attributes";
    private static final String JSP_MANAGE_IDENTITIES = "jsp/admin/plugins/identitydesk/ManageIdentities.jsp";
    private static final String MARK_AUTOCOMPLETE_CITY_ENDPOINT = "autocomplete_city_endpoint";
    private static final String MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT = "autocomplete_country_endpoint";
    private static final String MARK_RETURN_URL = "return_url";
    private static final String MARK_ATTRIBUTE_STATUSES = "attribute_statuses";

    // Views
    private static final String VIEW_MANAGE_IDENTITIES = "manageIdentitys";
    private static final String VIEW_CREATE_IDENTITY = "createIdentity";
    private static final String VIEW_MODIFY_IDENTITY = "modifyIdentity";

    // Actions
    private static final String ACTION_CREATE_IDENTITY = "createIdentity";
    private static final String ACTION_MODIFY_IDENTITY = "modifyIdentity";

    // Cache
    private static final ServiceContractCache _serviceContractCache = SpringContextService.getBean( "identitydesk.serviceContractCache" );

    // Session variable to store working values
    private final List<SearchAttributeDto> _searchAttributes = new ArrayList<>( );
    private ServiceContractDto _serviceContract;
    private List<AttributeStatus> _attributeStatuses = new ArrayList<>( );
    private String _currentClientCode = AppPropertiesService.getProperty( "identitydesk.default.client.code" );
    private String _currentReturnUrl = AppPropertiesService.getProperty( "identitydesk.default.returnUrl" );

    private final IdentityService _identityService = SpringContextService.getBean( "identityService.rest.httpAccess" );
    private final String _autocompleteCityEndpoint = AppPropertiesService.getProperty( "identitydesk.autocomplete.city.endpoint" );
    private final String _autocompleteCountryEndpoint = AppPropertiesService.getProperty( "identitydesk.autocomplete.country.endpoint" );
    private final List<String> _sortedAttributeKeyList = Arrays.asList( AppPropertiesService.getProperty( "identitydesk.attribute.order" ).split( "," ) );
    private final List<String> _searchAttributeKeyStrictList = Arrays
            .asList( AppPropertiesService.getProperty( "identitydesk.search.strict.attributes" ).split( "," ) );

    /**
     * Process the data to send the search request and returns the identity search form and results
     *
     * @param request
     *            The Http request
     * @return the html code of the identity form
     */
    @View( value = VIEW_MANAGE_IDENTITIES, defaultView = true )
    public String getSearchIdentities( HttpServletRequest request )
    {
        final List<QualifiedIdentity> qualifiedIdentities = new ArrayList<>( );

        initClientCode( request );
        initServiceContract( _currentClientCode );

        // Search
        if ( request.getParameter( NEW_SEARCH_PARAMETER ) != null )
        {
            // get search criterias
            collectSearchAttributes( request );

            // check
            checkSearchAttributes( );

            final String customerId = _searchAttributes.stream( ).filter( a -> a.getKey( ).equals( "customer_id" ) ).map( SearchAttributeDto::getValue )
                    .findFirst( ).orElse( null );
            if ( StringUtils.isNotBlank( customerId ) )
            {
                try
                {
                    final QualifiedIdentity identity = getQualifiedIdentityFromCustomerId( customerId );
                    qualifiedIdentities.add( identity );
                }
                catch( final IdentityStoreException e )
                {
                    AppLogService.error( "Error while retrieving the identity [customerId = " + customerId + "].", e );
                    addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                }
            }
            else
            {
                final IdentitySearchRequest searchRequest = new IdentitySearchRequest( );
                final SearchDto search = new SearchDto( );
                searchRequest.setSearch( search );
                search.setAttributes( _searchAttributes );
                try
                {
                    final IdentitySearchResponse searchResponse = _identityService.searchIdentities( searchRequest, _currentClientCode );
                    if ( Boolean.parseBoolean( request.getParameter( "ignore_approximate" ) ) )
                    {
                        qualifiedIdentities.addAll( searchResponse.getIdentities( ).stream( ).filter( i -> Math.round( i.getScoring( ) * 100 ) == 100 )
                                .collect( Collectors.toList( ) ) );
                    }
                    else
                    {
                        qualifiedIdentities.addAll( searchResponse.getIdentities( ) );
                    }
                    if ( qualifiedIdentities.isEmpty( ) )
                    {
                        addWarning( MESSAGE_SEARCH_IDENTITY_NORESULT, getLocale( ) );
                    }
                }
                catch( final IdentityStoreException e )
                {
                    AppLogService.error( "Error while searching identities [IdentitySearchRequest = " + searchRequest + "].", e );
                    addError( MESSAGE_SEARCH_IDENTITY_ERROR, getLocale( ) );
                }
            }
        }

        Map<String, Object> model = getPaginatedListModel( request, MARK_IDENTITY_LIST, qualifiedIdentities, JSP_MANAGE_IDENTITIES );
        model.put( MARK_QUERY_SEARCH_ATTRIBUTES, _searchAttributes );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        addReturnUrlMarker( request, model );

        return getPage( PROPERTY_PAGE_TITLE_MANAGE_IDENTITIES, TEMPLATE_SEARCH_IDENTITIES, model );
    }

    /**
     * Returns the form to create a identity
     *
     * @param request
     *            The Http request
     * @return the html code of the identity form
     */
    @View( VIEW_CREATE_IDENTITY )
    public String getCreateIdentity( HttpServletRequest request )
    {
        final Identity identity = getIdentityToUpdateFromRequest( request );

        Map<String, Object> model = getModel( );

        model.put( MARK_IDENTITY, IdentityDto.from( identity, _serviceContract ) );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );
        model.put( MARK_ATTRIBUTE_STATUSES, _attributeStatuses );
        addReturnUrlMarker( request, model );

        return getPage( PROPERTY_PAGE_TITLE_CREATE_IDENTITY, TEMPLATE_CREATE_IDENTITY, model );
    }

    /**
     * Process the data capture form of a new identity
     *
     * @param request
     *            The Http Request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_CREATE_IDENTITY )
    public String doCreateIdentity( HttpServletRequest request )
    {
        final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest( );
        _attributeStatuses.clear( );
        try
        {
            final Identity identity = getIdentityToUpdateFromRequest( request );
            if ( identity.getAttributes( ).stream( ).anyMatch( a -> StringUtils.isBlank( a.getCertificationProcess( ) ) ) )
            {
                addWarning( MESSAGE_IDENTITY_MUSTSELECTCERTIFICATION, getLocale( ) );
                return getCreateIdentity( request );
            }
            identityChangeRequest.setIdentity( identity );
            identityChangeRequest.setOrigin( this.getAuthor( ) );

            final IdentityChangeResponse response = _identityService.createIdentity( identityChangeRequest, _currentClientCode );
            if ( response.getStatus( ) != IdentityChangeStatus.CREATE_SUCCESS )
            {
                if ( response.getStatus( ) == IdentityChangeStatus.FAILURE )
                {
                    addError( response.getMessage( ) );
                }
                else
                {
                    addWarning( response.getMessage( ) );
                }
                if ( response.getAttributeStatuses( ) != null )
                {
                    _attributeStatuses.addAll( response.getAttributeStatuses( ) );
                }
                return getCreateIdentity( request );
            }
            else
            {
                addInfo( MESSAGE_CREATE_IDENTITY_SUCCESS, getLocale( ) );
                _searchAttributes.add( new SearchAttributeDto( "customer_id", response.getCustomerId( ), true ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while creating the identity [IdentityChangeRequest = " + identityChangeRequest + "].", e );
            addError( MESSAGE_CREATE_IDENTITY_ERROR, getLocale( ) );
            return getCreateIdentity( request );
        }
        return getSearchIdentities( request );
    }

    /**
     * Returns the form to update info about a identity
     *
     * @param request
     *            The Http request
     * @return The HTML form to update info
     */
    @View( VIEW_MODIFY_IDENTITY )
    public String getModifyIdentity( HttpServletRequest request )
    {
        final String customerId = request.getParameter( "customer_id" );
        final QualifiedIdentity qualifiedIdentity;
        try
        {
            qualifiedIdentity = getQualifiedIdentityFromCustomerId( customerId );
            if ( qualifiedIdentity == null )
            {
                addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                return getSearchIdentities( request );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while retrieving selected identity [customerId = " + customerId + "].", e );
            addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
            return getSearchIdentities( request );
        }

        final IdentityDto dto = IdentityDto.from( qualifiedIdentity, _serviceContract );

        Map<String, Object> model = getModel( );
        model.put( MARK_IDENTITY, dto );
        model.put( MARK_SERVICE_CONTRACT, _serviceContract );
        model.put( MARK_AUTOCOMPLETE_CITY_ENDPOINT, _autocompleteCityEndpoint );
        model.put( MARK_AUTOCOMPLETE_COUNTRY_ENDPOINT, _autocompleteCountryEndpoint );
        model.put( MARK_ATTRIBUTE_STATUSES, _attributeStatuses );
        addReturnUrlMarker( request, model );

        return getPage( PROPERTY_PAGE_TITLE_MODIFY_IDENTITY, TEMPLATE_MODIFY_IDENTITY, model );
    }

    /**
     * Process the change form of a identity
     *
     * @param request
     *            The Http request
     * @return The Jsp URL of the process result
     */
    @Action( ACTION_MODIFY_IDENTITY )
    public String doModifyIdentity( HttpServletRequest request )
    {
    	// get values to update
    	final Identity identityWithUpdates = getIdentityToUpdateFromRequest( request );
    	if ( identityWithUpdates.getCustomerId( ) == null )
    	{
    		 addError( MESSAGE_UPDATE_IDENTITY_ERROR, getLocale( ) );
             return getSearchIdentities( request );
    	}
    	
        final IdentityChangeRequest identityChangeRequest = new IdentityChangeRequest( );
        _attributeStatuses.clear( );

        try
        {
        	// get original Identity
            final Identity originalIdentity = this.getIdentityFromCustomerId( identityWithUpdates.getCustomerId( ) );
            if ( originalIdentity == null )
            {
                addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                return getSearchIdentities( request );
            }            
            
            // removal of attributes whose values are not modified
            identityWithUpdates.getAttributes( ).removeIf( updatedAttr -> !checkIfAttributeIsModified( originalIdentity, updatedAttr ) );
           
            // nothing to update
            if ( CollectionUtils.isEmpty( identityWithUpdates.getAttributes( ) ) )
            {
                addInfo( MESSAGE_UPDATE_IDENTITY_NOCHANGE, getLocale( ) );
                return getSearchIdentities( request );
            }
            
            // check if all attributes to update are certified
            final List<CertifiedAttribute> attributesWithoutCertif = identityWithUpdates.getAttributes( ).stream( )
                    .filter( a -> StringUtils.isBlank( a.getCertificationProcess( ) ) ).collect( Collectors.toList( ) );
            if ( CollectionUtils.isNotEmpty( attributesWithoutCertif ) )
            {
                addWarning( MESSAGE_IDENTITY_MUSTSELECTCERTIFICATION, getLocale( ) );
                return getModifyIdentity( request );
            }
            

            // Update API call
            identityChangeRequest.setIdentity( identityWithUpdates );
            identityChangeRequest.setOrigin( this.getAuthor( ) );
            
            final IdentityChangeResponse response = _identityService.updateIdentity( originalIdentity.getCustomerId( ), identityChangeRequest, _currentClientCode );

            // prepare response status message
            if ( response.getStatus( ) != IdentityChangeStatus.UPDATE_SUCCESS && response.getStatus( ) != IdentityChangeStatus.UPDATE_INCOMPLETE_SUCCESS )
            {
                if ( response.getStatus( ) == IdentityChangeStatus.FAILURE )
                {
                    addError( "Erreur lors de la mise à jour : " + response.getMessage( ) );
                }
                else
                {
                    addWarning( "Status de la mise à jour : " + response.getStatus( ).getLabel( ) + " : " + response.getMessage( ) );
                }
                if ( response.getAttributeStatuses( ) != null )
                {
                    _attributeStatuses.addAll( response.getAttributeStatuses( ) );
                }
                return getModifyIdentity( request );
            }
            else
            {
                addInfo( MESSAGE_UPDATE_IDENTITY_SUCCESS, getLocale( ) );
            }
        }
        catch( final IdentityStoreException e )
        {
            AppLogService.error( "Error while updating the identity [IdentityChangeRequest = " + identityChangeRequest + "].", e );
            addError( MESSAGE_UPDATE_IDENTITY_ERROR, getLocale( ) );
            return getModifyIdentity( request );
        }

        return getSearchIdentities( request );
    }

    /**
     * Check if there is a modification for the attribute (value or certification process)
     * 
     * Returns true if : 
     *  * attribute does not exists yet
     *  * value is updated
     *  * certification process is updated
     *  
     *  false otherwise
     *  
     * @param originalIdentity
     * @param updatedAttr
     * @return true if modified
     */
    private boolean checkIfAttributeIsModified(Identity originalIdentity, CertifiedAttribute updatedAttr) 
    {
    	CertifiedAttribute origAttr  = originalIdentity.getAttributes( ).stream( )
			.filter( a -> a.getKey( ).equals( updatedAttr.getKey( ) ) )
			.findFirst( ).orElse( null )  ;

    	if ( origAttr == null 
    			|| !origAttr.getValue( ).equals( updatedAttr.getValue( ) ) 
    			|| (!origAttr.getCertificationProcess( ).equals( updatedAttr.getCertificationProcess( ) ) && updatedAttr.getCertificationProcess( )!=null ) )
    	{
    		return true;
    	}
    	
    	// return false otherwise
    	return false;
    	
	}

	/**
     * collect search attributes from request
     * 
     * @param request
     * @return
     */
    private void collectSearchAttributes( final HttpServletRequest request )
    {

        final List<SearchAttributeDto> searchList = _serviceContract.getAttributeDefinitions( ).stream( ).map( AttributeDefinitionDto::getKeyName )
                .map( attrKey -> {
                    final String value = request.getParameter( SEARCH_PARAMETER_PREFIX + attrKey );
                    if ( value != null )
                    {
                        return new SearchAttributeDto( attrKey, value.trim( ), _searchAttributeKeyStrictList.contains( attrKey ) );
                    }
                    return null;
                } ).filter( searchAttribute -> searchAttribute != null && StringUtils.isNotBlank( searchAttribute.getValue( ) ) )
                .collect( Collectors.toList( ) );

        if ( searchList.isEmpty( ) )
        {
            // not a new search, keep existing criterias
            return;
        }

        _searchAttributes.clear( );
        _searchAttributes
                .addAll( searchList.stream( ).filter( s -> ( s != null && StringUtils.isNotBlank( s.getValue( ) ) ) ).collect( Collectors.toList( ) ) );

        if ( _searchAttributes.size( ) > 0 )
        {
            if ( _searchAttributes.stream( ).anyMatch( s -> s.getKey( ).equals( Constants.PARAM_BIRTH_PLACE ) ) )
            {
                final String value = request.getParameter( SEARCH_PARAMETER_PREFIX + Constants.PARAM_BIRTH_PLACE_CODE );
                _searchAttributes.add( new SearchAttributeDto( Constants.PARAM_BIRTH_PLACE_CODE, value, _searchAttributeKeyStrictList.contains( Constants.PARAM_BIRTH_PLACE_CODE ) ) );
            }
            if ( _searchAttributes.stream( ).anyMatch( s -> s.getKey( ).equals( Constants.PARAM_BIRTH_COUNTRY ) ) )
            {
                final String value = request.getParameter( SEARCH_PARAMETER_PREFIX + Constants.PARAM_BIRTH_COUNTRY_CODE );
                _searchAttributes.add( new SearchAttributeDto( Constants.PARAM_BIRTH_COUNTRY_CODE, value, _searchAttributeKeyStrictList.contains( Constants.PARAM_BIRTH_COUNTRY_CODE ) ) );
            }
        }

    }

    /**
     * check search criterias * email or lastname+firstname+birthdate are required
     * 
     * @return true if required values are present
     */
    private boolean checkSearchAttributes( )
    {
        final Optional<SearchAttributeDto> emailAttr = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( Constants.PARAM_EMAIL ) ).findFirst( );
        if ( !emailAttr.isPresent( ) || StringUtils.isBlank( emailAttr.get( ).getValue( ) ) )
        {
            final Optional<SearchAttributeDto> firstnameAttr = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( Constants.PARAM_FIRST_NAME ) ).findFirst( );
            final Optional<SearchAttributeDto> familynameAttr = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( Constants.PARAM_FAMILY_NAME ) ).findFirst( );
            final Optional<SearchAttributeDto> birthdateAttr = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( Constants.PARAM_BIRTH_DATE ) ).findFirst( );
            if ( ( !firstnameAttr.isPresent( ) || StringUtils.isBlank( firstnameAttr.get( ).getValue( ) ) )
                    || ( !familynameAttr.isPresent( ) || StringUtils.isBlank( familynameAttr.get( ).getValue( ) ) )
                    || ( !birthdateAttr.isPresent( ) || StringUtils.isBlank( birthdateAttr.get( ).getValue( ) ) ) )
            {
                addInfo( MESSAGE_SEARCH_IDENTITY_REQUIREDFIELD, getLocale( ) );
                return false;
            }
        }

        return true;
    }

    /**
     * init client code * get client code from request, * or keep default client code set in properties
     * 
     * @param request
     */
    private void initClientCode( final HttpServletRequest request )
    {
        String clientCode = request.getParameter( "client_code" );
        if ( !StringUtils.isBlank( clientCode ) )
        {
            _currentClientCode = clientCode;
        }
    }

    /**
     * fill model with return url marker
     * 
     * @param request
     * @param model
     */
    private void addReturnUrlMarker( final HttpServletRequest request, final Map<String, Object> model )
    {
        final String returnUrl = request.getParameter( "return_url" );
        if ( StringUtils.isNotBlank( returnUrl ) )
        {
            _currentReturnUrl = returnUrl;
        }

        model.put( MARK_RETURN_URL, _currentReturnUrl );
    }

    /**
     * get updated identity attributes from request
     * 
     * @param request
     * @return the identity with attributes to update
     */
    private Identity getIdentityToUpdateFromRequest( final HttpServletRequest request )
    {
        final Identity identity = new Identity( );
        
        if ( request.getParameter( Constants.PARAM_ID_CUSTOMER ) == null )
        {
        	return null;
        }
        else
        {
        	identity.setCustomerId( request.getParameter( Constants.PARAM_ID_CUSTOMER ) );
        	
	        // add attributes (and certification process) to identity if they are present in the request
	        _serviceContract.getAttributeDefinitions().stream()
	        	.filter( attr -> !StringUtils.isEmpty( request.getParameter( attr.getKeyName( ) ) ) )
	        	.forEach( attr -> identity.getAttributes( ).add( buildAttribute( attr.getKeyName( ), 
	        			request.getParameter( attr.getKeyName( ) ), 
	        			request.getParameter( attr.getKeyName( ) + ATTR_CERT_SUFFIX) ) ) );
	
	        return identity;
        }
        
    }

    /**
     * Build attribute 
     * 
     * @param key
     * @param value
     * @param certificationCode
     * @return the certifiedAttribute
     */
    private CertifiedAttribute buildAttribute( final String key, final String value, final String certificationCode )
    {
        final CertifiedAttribute attr = new CertifiedAttribute( );
        attr.setKey( key );
        attr.setValue( value );
        if ( StringUtils.isNotBlank( certificationCode ) )
        {
            attr.setCertificationProcess( certificationCode );
            attr.setCertificationDate( new Date( ) );
        }
        return attr;
    }

    /**
     * get Author
     * 
     * @return the author
     */
    private RequestAuthor getAuthor( )
    {
        RequestAuthor author = new RequestAuthor( );
        author.setName( getUser( ).getEmail( ) );
        author.setType( AuthorType.agent );
        return author;
    }

    /**
     * get QualifiedIdentity From CustomerId
     * 
     * @param customerId
     * @return the QualifiedIdentity , null otherwise
     * @throws IdentityStoreException
     */
    private QualifiedIdentity getQualifiedIdentityFromCustomerId( final String customerId ) throws IdentityStoreException
    {
        final IdentitySearchResponse identityResponse = _identityService.getIdentity( customerId, _currentClientCode );

        if ( identityResponse.getIdentities( ) != null && identityResponse.getIdentities( ).size( ) == 1 )
        {
            return identityResponse.getIdentities( ).get( 0 );
        }
        else
        {
            return null;
        }
    }

    /**
     * get Identity From CustomerId
     * 
     * @param customerId
     * @return the identity object
     * @throws IdentityStoreException
     */
    private Identity getIdentityFromCustomerId( final String customerId ) throws IdentityStoreException
    {
        QualifiedIdentity qualifiedIdentity = getQualifiedIdentityFromCustomerId( customerId );

        if ( qualifiedIdentity != null )
        {
            return castQualifiedIdentityToIdentity( qualifiedIdentity );
        }

        return null;

    }

    /**
     * cast QualifiedIdentity to Identity object
     * 
     * @param qualifiedIdentity
     * @return an Identity Object
     */
    private Identity castQualifiedIdentityToIdentity( QualifiedIdentity qualifiedIdentity )
    {
        final Identity identity = new Identity( );

        identity.setCustomerId( qualifiedIdentity.getCustomerId( ) );
        identity.setConnectionId( qualifiedIdentity.getConnectionId( ) );
        identity.setAttributes( qualifiedIdentity.getAttributes( ).stream( ).map( a -> {
            final CertifiedAttribute attribute = new CertifiedAttribute( );
            attribute.setKey( a.getKey( ) );
            attribute.setValue( a.getValue( ) );
            attribute.setCertificationProcess( a.getCertifier( ) );
            attribute.setCertificationDate( a.getCertificationDate( ) );
            return attribute;
        } ).collect( Collectors.toList( ) ) );

        return identity;
    }

    

        

    /**
     * init service contract
     * 
     * @param clientCode
     */
    private void initServiceContract( String clientCode )
    {
        if ( _serviceContract == null )
        {
            try
            {
                _serviceContract = _serviceContractCache.get( clientCode );
            }
            catch( final IdentityStoreException e )
            {
                AppLogService.error( "Error while retrieving service contract [client code = " + clientCode + "].", e );
                addError( MESSAGE_GET_SERVICE_CONTRACT_ERROR, getLocale( ) );
            }
        }
    }

}
