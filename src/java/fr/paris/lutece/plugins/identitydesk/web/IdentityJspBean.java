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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

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
import fr.paris.lutece.plugins.identitystore.v3.web.service.IdentityService;
import fr.paris.lutece.plugins.identitystore.web.exception.IdentityStoreException;
import fr.paris.lutece.portal.service.spring.SpringContextService;
import fr.paris.lutece.portal.service.util.AppException;
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
    private static final String SEARCH_PARAMETER_SUFFIX = "search_";
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
        final Identity identity = initNewIdentity( request );

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
            final Identity identity = initNewIdentity( request );
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
        final String customerId = request.getParameter( "customer_id" );
        final IdentityChangeRequest changeRequest = new IdentityChangeRequest( );
        _attributeStatuses.clear( );

        try
        {
            final Identity identityToUpdate = this.getIdentityFromCustomerId( customerId );
            if ( identityToUpdate == null )
            {
                addError( MESSAGE_GET_IDENTITY_ERROR, getLocale( ) );
                return getSearchIdentities( request );
            }
            final Identity identityFromParams = initNewIdentity( request );

            // check if all attributes to update are certified
            final List<CertifiedAttribute> attributesWithoutCertif = identityFromParams.getAttributes( ).stream( )
                    .filter( a -> StringUtils.isBlank( a.getCertificationProcess( ) ) ).collect( Collectors.toList( ) );
            if ( CollectionUtils.isNotEmpty( attributesWithoutCertif ) )
            {
                addWarning( MESSAGE_IDENTITY_MUSTSELECTCERTIFICATION, getLocale( ) );
                return getModifyIdentity( request );
            }

            // check for updates
            final List<CertifiedAttribute> updatedAttributes = identityFromParams.getAttributes( ).stream( ).map( newAttr -> {
                final CertifiedAttribute oldAttr = identityToUpdate.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( newAttr.getKey( ) ) )
                        .findFirst( ).orElse( null );
                if ( oldAttr != null && StringUtils.isBlank( newAttr.getCertificationProcess( ) ) )
                {
                    if ( oldAttr.getValue( ).equals( newAttr.getValue( ) ) && oldAttr.getCertificationProcess().equals(newAttr.getCertificationProcess()) )
                    {
                        return null;
                    }
                }
                return newAttr;
            } ).filter( Objects::nonNull ).collect( Collectors.toList( ) );

            if ( CollectionUtils.isEmpty( updatedAttributes ) )
            {
                addInfo( MESSAGE_UPDATE_IDENTITY_NOCHANGE, getLocale( ) );
                return getSearchIdentities( request );
            }
            else
            {
                identityToUpdate.setAttributes( updatedAttributes );
            }

            // Update API call
            changeRequest.setIdentity( identityToUpdate );
            changeRequest.setOrigin( this.getAuthor( ) );
            final IdentityChangeResponse response = _identityService.updateIdentity( identityToUpdate.getCustomerId( ), changeRequest, _currentClientCode );

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
            AppLogService.error( "Error while updating the identity [IdentityChangeRequest = " + changeRequest + "].", e );
            addError( MESSAGE_UPDATE_IDENTITY_ERROR, getLocale( ) );
            return getModifyIdentity( request );
        }

        return getSearchIdentities( request );
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
                    final String value = request.getParameter( SEARCH_PARAMETER_SUFFIX + attrKey );
                    if ( value != null )
                    {
                        return new SearchAttributeDto( attrKey, value.trim( ), _searchAttributeKeyStrictList.contains( attrKey ) );
                    }
                    return null;
                } ).filter( searchAttribute -> searchAttribute != null && !searchAttribute.getValue( ).isBlank( ) ).collect( Collectors.toList( ) );

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
            if ( _searchAttributes.stream( ).anyMatch( s -> s.getKey( ).equals( "birthplace" ) ) )
            {
                final String value = request.getParameter( SEARCH_PARAMETER_SUFFIX + "birthplace_code" );
                _searchAttributes.add( new SearchAttributeDto( "birthplace_code", value, _searchAttributeKeyStrictList.contains( "birthplace_code" ) ) );
            }
            if ( _searchAttributes.stream( ).anyMatch( s -> s.getKey( ).equals( "birthcountry" ) ) )
            {
                final String value = request.getParameter( SEARCH_PARAMETER_SUFFIX + "birthcountry_code" );
                _searchAttributes.add( new SearchAttributeDto( "birthcountry_code", value, _searchAttributeKeyStrictList.contains( "birthcountry_code" ) ) );
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
        final Optional<SearchAttributeDto> login = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( "login" ) ).findFirst( );
        if ( !login.isPresent( ) || StringUtils.isBlank( login.get( ).getValue( ) ) )
        {
            final Optional<SearchAttributeDto> firstname = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( "first_name" ) ).findFirst( );
            final Optional<SearchAttributeDto> familyname = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( "family_name" ) ).findFirst( );
            final Optional<SearchAttributeDto> birthdate = _searchAttributes.stream( ).filter( s -> s.getKey( ).equals( "birthdate" ) ).findFirst( );
            if ( ( !firstname.isPresent( ) || StringUtils.isBlank( firstname.get( ).getValue( ) ) )
                    || ( !familyname.isPresent( ) || StringUtils.isBlank( familyname.get( ).getValue( ) ) )
                    || ( !birthdate.isPresent( ) || StringUtils.isBlank( birthdate.get( ).getValue( ) ) ) )
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

    private Identity initNewIdentity( final HttpServletRequest request )
    {
        final Identity identity = new Identity( );
        request.getParameterMap( ).entrySet( ).stream( )
                .filter( entry -> !entry.getKey( ).startsWith( "action_" ) && !entry.getKey( ).startsWith( "view_" ) && !entry.getKey( ).endsWith( "-certif" )
                        && !entry.getKey( ).startsWith( "birthplace" ) && !entry.getKey( ).startsWith( "birthcountry" )
                        && !entry.getKey( ).equals( "customer_id" ) && !entry.getKey( ).equals( "client_code" ) && !entry.getKey( ).equals( "return_url" ) )
                .filter( entry -> entry.getValue( ) != null && entry.getValue( ).length == 1 && StringUtils.isNotBlank( entry.getValue( ) [0] ) )
                .forEach( entry -> identity.getAttributes( )
                        .add( initAttribute( entry.getKey( ), entry.getValue( ) [0], request.getParameter( entry.getKey( ) + "-certif" ) ) ) );

        final String birthplace = request.getParameter( "birthplace" );
        final String birthplace_code = request.getParameter( "birthplace_code" );
        final String birthcountry = request.getParameter( "birthcountry" );
        final String birthcountry_code = request.getParameter( "birthcountry_code" );

        if ( StringUtils.isNotBlank( birthplace ) )
        {
            identity.getAttributes( ).add( initAttribute( "birthplace", birthplace, request.getParameter( "birthplace-certif" ) ) );
            identity.getAttributes( ).add( initAttribute( "birthplace_code", birthplace_code, request.getParameter( "birthplace-certif" ) ) );
        }
        if ( StringUtils.isNotBlank( birthcountry ) )
        {
            identity.getAttributes( ).add( initAttribute( "birthcountry", birthcountry, request.getParameter( "birthcountry-certif" ) ) );
            identity.getAttributes( ).add( initAttribute( "birthcountry_code", birthcountry_code, request.getParameter( "birthcountry-certif" ) ) );
        }

        return identity;
    }

    private CertifiedAttribute initAttribute( final String key, final String value, final String certificationCode )
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
        author.setType( AuthorType.application );
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
     * DTO used for the create and update UI.
     */
    public static class IdentityDto
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
         * Static builder for update UI.
         * 
         * @param qualifiedIdentity
         * @param serviceContract
         * @return
         */
        public static IdentityDto from( final QualifiedIdentity qualifiedIdentity, final ServiceContractDto serviceContract )
        {
            if ( qualifiedIdentity == null || serviceContract == null )
            {
                return null;
            }
            final IdentityDto identityDto = new IdentityDto( qualifiedIdentity.getCustomerId( ) );

            serviceContract.getAttributeDefinitions( ).stream( ).filter( a -> a.getAttributeRight( ).isWritable( ) ).forEach( attrRef -> {
                final fr.paris.lutece.plugins.identitystore.v3.web.rs.dto.search.CertifiedAttribute identityAttr = qualifiedIdentity.getAttributes( ).stream( )
                        .filter( a -> a.getKey( ).equals( attrRef.getKeyName( ) ) ).findFirst( ).orElse( null );
                if ( CollectionUtils.isNotEmpty( attrRef.getAttributeCertifications( ) ) )
                {
                    identityDto.getAttributeList( ).add( AttributeDto.from( identityAttr, attrRef ) );
                }
            } );

            /*
             * 
             * ServiceContractService.prepareAttributesForUpdate( identityDto, serviceContract ); ...
             * 
             * >>> qualifiedIdentity.getAttributes( ).stream( ) .filter( attr -> ( serviceContract.getAttribute( attr.getKey( ) ).isWritable( ) ) .forEach( ...
             * );
             * 
             */

            return identityDto;
        }

        /**
         * Static builder for create UI.
         * 
         * @param identity
         * @param serviceContract
         * @return
         */
        public static IdentityDto from( final Identity identity, final ServiceContractDto serviceContract )
        {
            if ( identity == null || serviceContract == null )
            {
                return null;
            }
            final IdentityDto identityDto = new IdentityDto( );
            serviceContract.getAttributeDefinitions( ).stream( ).filter( a -> a.getAttributeRight( ).isWritable( ) ).forEach( attrDef -> {
                final CertifiedAttribute identityAttr = identity.getAttributes( ).stream( ).filter( a -> a.getKey( ).equals( attrDef.getKeyName( ) ) )
                        .findFirst( ).orElse( null );
                if ( CollectionUtils.isNotEmpty( attrDef.getAttributeCertifications( ) ) )
                {
                    identityDto.getAttributeList( ).add( AttributeDto.from( identityAttr, attrDef ) );
                }
            } );

            return identityDto;
        }

        public static class AttributeDto
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
